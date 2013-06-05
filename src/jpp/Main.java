package jpp;

import java.io.*;
import java.util.*;
import com.sun.org.apache.bcel.internal.generic.*;
import base.*;

public class Main implements IJavaTokens, IJPPTokens {

  public static void main(String[] args) {
    new Main().doMain(args);
  }

  private void doMain(String[] args) {
    // We must call this in case we are not running as an applet
    Streams.loadResources(this);

    try {
      CmdArgs ca = new CmdArgs(
          args,
          " == --help -h --echo -e  --verbose -v" + " !!  ",
          "\njpp : Java preprocessor\n"
              + "Usage: jpp [<options>] <file>*\n"
              + "Options: \n"
              + " --verbose, -v       : verbose output\n"
              + " --echo, -e          : echo input to stdout\n"
              + " --trace, -T         : trace scanner\n"
              + " -s                  : simulation only; don't modify any files\n"
              + " -x <ext>            : change extension (default=java)\n"
              + " -b                  : don't save backups of originals\n"
              + " -d [<path>]         : process every .java file in <path> (or current directory)\n"
              + " -c                  : don't expect special end-of-line comment marking removable code\n"
              + " --help, -h          : help\n");

      while (ca.hasNext()) {

        if (ca.nextIsValue()) {
          fileList.add(ca.nextValue());
          continue;
        }

        switch (ca.nextChar()) {
        case 'd':
          {
            File dir = new File(Path.getUserDir());
            if (ca.nextIsValue()) {
              dir = ca.nextPath();
            }
            if (!dir.isDirectory())
              ca.exception("not a directory: " + dir);
            dirList.push(dir);
          }
          break;
        case 's':
          simulationMode = true;
          break;
        //        case 'c':
        //          lookForMarker = false;
        //          break;
        case 'x':
          String newExt = ca.nextValue();
          if (!newExt.startsWith("."))
            newExt = "." + newExt;
          ext = newExt;
          break;
        case 'h':
          ca.help();
        case 'e':
          echo = true;
          break;
        case 't':
          trace = true;
          break;
        case 'v':
          verbose = true;
          break;
        case 'b':
          backups = false;
          break;
        default:
          ca.unsupported();
        }
      }
      ca.done();

      if (backups)
        constructBackupDir();

      if (fileList.isEmpty() && dirList.isEmpty())
        ca.help();

      // process each input file
      while (!dirList.isEmpty()) {
        File d = (File) dirList.pop();

        if (verbose)
          Streams.out.println("Searching " + d);

        File[] list = d.listFiles();
        for (int i = 0; i < list.length; i++) {
          File f = list[i];
          if (f.isFile()) {
            if (f.getName().endsWith(ext))
              fileList.add(f.getPath());
          } else if (f.isDirectory()) {
            dirList.push(f);
          }
        }
      }

      for (int i = 0; i < fileList.size(); i++) {
        process(new File(fileList.getString(i)));
      }
    } catch (Throwable e) {
      //             System.err.println(Tools.stackTrace(0, 20, e));
      System.err.println(e.toString());
      if (lastSrcToken != null)
        System.err.println(lastSrcToken.display());
    }
  }

  /**
   * Construct backup dir to hold backup of old .java files.
   * 
   * All processed files must be within the user's home directory,
   * and if such a file has the path home/xxx/yyy/file.java,
   * the backup file is stored in home/_jppbackups_/xxx/yyy/file.java_d.tmp.
   */
  private void constructBackupDir() {

    boolean db = false;

    Properties p = System.getProperties();

    // Get user's home directory
    homeDir = p.getProperty("user.home");
    if (db)
      Streams.out.println("user.home = " + homeDir);
    if (homeDir == null)
      throw new RuntimeException("no user.home property defined");

    File homeDirBackup = new File(homeDir, ".jppbackups");
    if (db)
      Streams.out.println("homeDirWithBackup="
          + homeDirBackup.getAbsolutePath());

    if (!homeDirBackup.isDirectory()) {
      if (homeDirBackup.mkdir())
        throw new RuntimeException("Failed to make backup dir: "
            + homeDirWithBackup);
    }
    homeDirWithBackup = homeDirBackup.getAbsolutePath();
  }

  private String homeDir;

  private String homeDirWithBackup;

  private File getBackupDir(File orig) {

    //    Streams.out.println("orig="+orig);
    //    Streams.out.println("parent file="+orig.getParentFile());

    orig = orig.getAbsoluteFile();
    String fullPath = orig.getParentFile().getAbsolutePath();

    if (!fullPath.startsWith(homeDir))
      throw new RuntimeException("file path not descendent of home path:\n"
          + fullPath + "\n" + homeDir);

    File bkupDir = new File(homeDirWithBackup, fullPath.substring(homeDir
        .length()));

    return bkupDir;
  }

  /**
   * Process java source file
   * 
   * @param javaSourceFile
   * @throws IOException
   */
  private void process(File javaSourceFile) throws IOException {
    if (verbose)
      Streams.out.println("Processing " + javaSourceFile);

    // Read in file to compare later to see if it actually changes
    String originalFile = Streams2.readTextFile(javaSourceFile.getPath());

    // Construct scanner for file
    TextScanner sc = new TextScanner(javaDFA(), -1);
    sc.setTrace(trace);
    sc.setEcho(echo);
    sc.include(new StringReader(originalFile), javaSourceFile.toString());

    // Write modified file to string
    StringWriter modifiedSrcFile = new StringWriter(10000);

    while (!sc.eof()) {

      Token tk = sc.read();

      if (tk.eof())
        break;

//      if (verbose) Streams.out.println(" token="+tk.debug());

      if (tk.unknown())
        tk.exception("unknown token: " + tk.debug());

      String tx = tk.text();

      // if token starts with '/*!!', perform special preformatting
      if (tx.startsWith("/*!!")) {
        tx = preformatComment(tx);
        tk.setText(tx);
        //Streams.out.println("Orig comment=\n"+tx+"\npreformatted=\n"+tx2);
      }

     // modifiedSrcFile.write("(token "+tk.debug()+"), tx=["+tx+"]\n ");
      modifiedSrcFile.write(tx);

      // Is this a preprocessor command?
      if (tk.id(T_COMMENTML)) {

        // remove the /* */ characters
        String pcmd = tx.substring(2, tx.length() - 2);
        lastSrcToken = tk;

        if (preProcess(pcmd, sc, modifiedSrcFile))
          readPastOldCode(sc);
      }

    }

    String modified = modifiedSrcFile.toString();

    if (!originalFile.equals(modified)) {
      if (simulationMode) {
        if (verbose)
          Streams.out.println(" modified, now:\n" + modified);
      } else {
        try {
          if (backups) {
            File bkupDir = getBackupDir(javaSourceFile);
            File backup = Streams2.moveFileToTemp(javaSourceFile, bkupDir, 5);
            if (verbose)
              Streams.out.println(" modified, saving original in " + backup);
            Streams2.writeTextFile(javaSourceFile, modified);
          } else {
            File tmp = File.createTempFile("_jpp", ".tmp");
            Streams2.writeTextFile(tmp, modified);
            Streams2.delete(javaSourceFile);
            Streams2.renameTo(tmp, javaSourceFile);
          }
        } catch (Throwable e) {
          Streams.out.println("caught: " + e + ", "
              + Tools.stackTrace(0, 20, e));
        }
      }
    } else {
      if (verbose) {
        Streams.out.println(" no changes");
      }
    }

  }

  private Token lastSrcToken;

  /**
   * Preformat a multiline comment by prefixing each line with '*' if necessary
   */
  private static String preformatComment(String s) {
    // the comment starts with /*!! , so remove the !! first of all.

    StringBuilder sb = new StringBuilder();
    sb.append("/*");
    boolean first = true;
    TextScanner sc = new TextScanner(s.substring(4));

    while (!sc.eof()) {
      String line = sc.readLine();

      if (!first && !line.trim().startsWith("*")) {
        line = "*  " + line;
      }
      first = false;

      sb.append(line);
      if (!sc.eof())
        sb.append('\n');
    }
    return sb.toString();
  }

  /**
   * Read past old code that should not be written out, since it's being
   * regenerated. 
   * 
   * (Not any more: All lines containing code or comments must end with a comment
   * "// !", or an error is generated. )
   * 
   * @param sc
   */
  private void readPastOldCode(TextScanner sc) {
    
    final boolean db =  false;
//    
//    if (db) 
//      pr("readPastOldCode...");
          
//    final boolean NEED_MARKER = false;
//
//    boolean inCode = false;
    Token tLineStart = Token.eofToken();
    
    while (true) {
      Token t = sc.peek();
      
      
      if (db) 
        pr(" peek="+t.debug());
            
//      if (!inCode && sc.peek(T_COMMENTML)) {
//        if (db) 
//          pr(" commentML, stopping");
//              
//        break;
//      }

//      Token t = sc.read();
      
      if (t.eof()) {
//        if (NEED_MARKER) {
//          if (lookForMarker)
            tLineStart
                .exception("missing end of section comment"); //lines to be removed don't end with /* ... */ ");
//        }
        break;
      }

      
//      if (t.id(T_CR)) {
////        if (NEED_MARKER) {
////          if (lookForMarker) {
////            if (inCode)
////              tLineStart
////                  .exception("line to be removed doesn't have //! comment");
////          }
////        }
//        tLineStart = null;
//        continue;
//      }

//      if (tLineStart == null)
//        tLineStart = t;
//      if (t.id(T_WS))
//        continue;
      if (t.id(T_COMMENTSL)) {
        String tx = t.text().substring(2).trim();
        if (tx.equals("!")) {
          break;
//          inCode = false;
        }
        
//        continue;
      }
      sc.read();
//      inCode = true;
    }
  }

  private static final String[] visNames = { "public ", "private ",
      "protected ", "", };

  private StringBuilder lb = new StringBuilder();

  /**
   * Preprocess possible command
   * @param command : text extracted from multiline comment
   * @param input : java source file tokenizer
   * @param writer : modified source file output
   * @return
   * @throws IOException
   */
  private boolean preProcess(String command, TextScanner input, Writer writer)
      throws IOException {

    boolean wasPreProc = false;

    // Set up a scanner for preprocessor (note that it uses a different DFA)

    TextScanner sc = new TextScanner(jppDFA(), J_WHITESPACE);
    sc.setTrace(trace);

    sc.include(new StringReader(command), command);

    // Does text start with special character '!' ?

    if (sc.readIf(J_CMDSTART) && !sc.eof()) {

      wasPreProc = true;

      // If next source token is a newline, copy it to the output
      while (input.readIf(T_WS))
        ;
      if (input.readIf(T_CR)) {
        writer.write('\n');
      }

      if (sc.readIf(J_ENUM)) {
        doEnum(sc, writer);
        //      } else if (sc.readIf(J_ENUM2)) {
        //        doEnum2(sc, input, writer);
      } else if (sc.readIf(J_STRS)) {
        doStrs(sc, writer, false);
      } else if (sc.readIf(J_CHARS)) {
        doStrs(sc, writer, true);
      } else if (sc.readIf(J_VERBATIM)) {
        doVerbatim(sc, writer);
      } else {
        throw new RuntimeException("unrecognized preprocessor command");
      }
//      writer.write("//!\n");

    }
    return wasPreProc;
  }

  private void doVerbatim(TextScanner sc, Writer writer) throws IOException {

    parseHeaderArgs(sc);
    sc.setSkipType(-1);
    lb.setLength(0);
    int curs = 0;
    boolean indented = false;
    boolean firstLine = true;
    boolean nonWS = false;

    if (name != null) {
      lb.append(vis());
      lb.append("static final String ");
      lb.append(name);
      lb.append(" = ");
      lb.append(END_LINE);

      curs = lb.length();
    }

    boolean withStar = true;

    if (sc.peek().text().equals("\n"))
      sc.read();
    while (!sc.eof()) {
      Token tk = sc.read();
      String t = tk.text();

      char c = t.charAt(0);

      if (withStar) {
        if (!nonWS && ((c <= ' ' && c != '\n') || c == '*')) {
          if (c == '*') {
            nonWS = true;
          }
          continue;
        }
        nonWS = true;
      }

      if (!indented) {
        curs = lb.length() + 6;
        Tools.tab(lb, curs);
        if (!firstLine)
          lb.append('+');
        else
          firstLine = false;
        lb.append('"');
        indented = true;
      }
      JavaStrEncoder.encoder.encode(lb, t);
      if (t.endsWith("\n")) {
        lb.append("\"" + END_LINE);
        indented = false;
        nonWS = false;
        continue;
      }
    }
    if (indented) {
      lb.append('"');
    }
    if (name != null) {
      lb.append(";");
      indented = true;
    }
    if (indented) {
      lb.append(END_LINE);
    }
    writer.write(lb.toString());
  }

  //  /**
  //   * 
  //   * @param sc
  //   * @param output
  //   * @throws IOException
  //   */
  //  private void doEnum2(TextScanner sc, TextScanner java, Writer output)
  //      throws IOException {
  //    
  //    final boolean db = false;
  //    if (db) 
  //      pr("doEnum2");
  //   
  //    parseHeaderArgs(sc);
  //
  //    Token lastJavaToken = java.last();
  //
  //    while (true) {
  //      Token t = java.peek();
  //      if (t.eof()) {
  //        lastJavaToken.exception("Missing terminating /* ! */");
  //      }
  //      lastJavaToken = t;
  //
  //      String s = t.text();
  //
  //      if (t.id(T_COMMENTML)) {
  //        String body = s;
  //        body = body.substring(2, body.length() - 2).trim();
  //        if (body.equals("!"))
  //          break;
  //      }
  //
  //      if (t.id(T_INTCONST)) {
  //        s = Integer.toString(offset++);
  //      }
  //
  //      if (t.id(T_ID)) {
  //        if (s.equals(s.toLowerCase())) {
  //          output.write("\n");
  //          output.write(vis());
  //          output.write(" static final int ");
  //          String id = s.toUpperCase();
  //          if (!id.startsWith(prefix))
  //            output.write(prefix);
  //          output.write(id);
  //          output.write(" = ");
  //          output.write(Integer.toString(offset++));
  //          output.write(";\n");
  //          s = "";
  //        }
  //      }
  //
  //      java.read();
  //      output.write(s);
  //    }
  //  }

  private void doStrs(TextScanner sc, Writer w, boolean chars)
      throws IOException {
    parseHeaderArgs(sc);
    boolean indented = false;
    int curs = 0;

    lb.setLength(0);
    if (name != null) {
      lb.append(vis());
      lb.append(chars ? "static final char[] " : "static final String[] ");
      lb.append(name);
      lb.append(" = {  ");
      lb.append(END_LINE);

      // indented = true;
      curs = lb.length();
    }

    while (!sc.eof()) {
      if (!indented) {
        curs = lb.length() + 6;
        Tools.tab(lb, curs);
        indented = true;
      }

      String str = readText(sc.read());
      if (lb.length() - curs + str.length() > 60) {
        lb.append(END_LINE);
        curs = lb.length() + 6;
        Tools.tab(lb, curs);
      }
      if (chars) {
        for (int i = 0; i < str.length(); i++) {
          char c = str.charAt(i);
          String s2 = Character.toString(c);
          if (c == '\\') {
            s2 = str.substring(i, i + 2);
            i++;

          }
          lb.append('\'');
          lb.append(s2);
          lb.append('\'');
          if (i + 1 < str.length())
            lb.append(',');
        }
      } else {
        lb.append('"');
        JavaStrEncoder.encoder.encode(lb, str);
        //				lb.append(str);
        lb.append("\"");
      }
      if (!sc.peek().eof())
        lb.append(",");

    }
    if (name != null) {
      lb.append("\n};");
      indented = true;
    }
    lb.append('\n');
//      lb.append(END_LINE);
    w.write(lb.toString());
  }

  private int vis;

  // private boolean charMode;

  private int offset;

  private String prefix;

  private String name;

  private String combinedStringsLabel;

  private boolean withLengths;

  private String readText(Token tk) {
    String out = null;
    switch (tk.id()) {
    case J_STRING:
      {
        out = tk.text();
        out = TextScanner.removeQuotes(out); // JavaStrEncoder.encoder.decode(Scanner.removeQuotes(out));
      }
      break;
    case J_ID:
    case J_INTCONST:
      out = tk.text();
      break;
    }
    if (out == null)
      tk.exception("not text");

    return out;
  }

  private void parseHeaderArgs(TextScanner sc) {
    vis = J_PUBLIC;
    offset = 0;
    prefix = "";
    name = null;
    combinedStringsLabel = null;
    withLengths = false;

    // charMode = false;

    outer: while (true) {
      Token t = sc.peek();

      switch (t.id()) {
      case J_PUBLIC:
      case J_PRIVATE:
      case J_PACKAGE:
      case J_PROTECTED:
        vis = t.id();
        sc.read();
        break;
      case J_STRS:
        sc.read();
        combinedStringsLabel = sc.read(J_ID).text();
        break;
      case J_PREFIX:
        sc.read();
        prefix = sc.read(J_ID).text().toUpperCase();
        break;
      case J_INTCONST:
        offset = Integer.parseInt(t.text());
        sc.read();
        break;
      case J_NAME:
        sc.read();
        name = readText(sc.read());
        break;
      case J_WITHLENGTHS:
        sc.read();
        withLengths = true;
        break;
      default:
        if (t.text().equals("\\")) {
          sc.read();
        }
        break outer;
      }
    }
  }

  private static void pr(Object obj) {
    Streams.out.println(obj);
  }
  /**
   * 
   * @param sc
   * @param output
   * @throws IOException
   */
  private void doEnum(TextScanner sc, Writer output) throws IOException {

    
    parseHeaderArgs(sc);

    StringBuilder cs = null;
    int csc = 0;
    if (combinedStringsLabel != null) {
      cs = new StringBuilder();
      cs.append("    ");
      cs.append(vis());
      cs.append("static final String ");
      cs.append(combinedStringsLabel);
      cs.append(" = \"");
    }

    output.write("\n");

    int count = 0;
    while (!sc.eof()) {
      String name = sc.read(J_ID).text().toUpperCase();
      int size = 1;
      Token t = sc.peek();
      if (t.id(J_INTCONST)) {
        size = Integer.parseInt(t.text());
        sc.read();
      }
      if (!name.equals("_")) {
        if (cs != null) {
          if (cs.length() > csc + 60) {
            cs.append("\"+ ");
            cs.append(END_LINE);
            csc = cs.length();
            cs.append("    \"");
          }
          if (count > 0)
            cs.append(' ');
          cs.append(name);
        }

        {
          lb.setLength(0);
          lb.append("    ");
          lb.append(vis());
          lb.append("static final int ");
          int c = lb.length();
          lb.append(prefix);
          lb.append(name);

          Tools.tab(lb, c + 16);
          lb.append(" = ");
          lb.append(offset);
          lb.append(';');
          Tools.tab(lb, c + 24);
          lb.append(END_LINE);
        }

        output.write(lb.toString());

        if (withLengths) {
          {
            lb.setLength(0);
            lb.append("    ");
            lb.append(vis());
            lb.append("static final int ");
            int c = lb.length();
            lb.append(prefix);
            lb.append(name);
            lb.append("_LEN");
            Tools.tab(lb, c + 16);
            lb.append(" = ");
            lb.append(size);
            lb.append(';');
            Tools.tab(lb, c + 24);
            lb.append(END_LINE);
          }

          output.write(lb.toString());
        }
        count++;
      }
      offset += size;
    }
    if (cs != null) {
      cs.append("\"; ");
      output.write(cs.toString());
    }
//    output.write(END_LINE2);
  }

  private String vis() {
    return visNames[vis - J_PUBLIC];
  }

  private static final String END_LINE = "\n"; // "//!\n";
//  private static final String END_LINE2 = "//!\n";

  private static Map dfaMap = new HashMap();

  private DFA javaDFA() {
    return DFA.readFromSet(dfaMap, this, "java.dfa");
  }

  private DFA jppDFA() {
    return DFA.readFromSet(dfaMap, this, "jpp.dfa");
  }

  private DArray fileList = new DArray();

  private boolean echo;

  private boolean trace;

  private boolean verbose;

  private boolean backups = true;

  private String ext = ".java";

  private DArray dirList = new DArray();

  // -s option
  private boolean simulationMode;

//  // -c
//  private boolean lookForMarker = true;

}
