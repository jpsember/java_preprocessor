package base;

import java.io.*;

/**
 * Class to manipulate command line arguments
 *
 * Arguments are of two types:
 *    single-character options:  -t -I -ab
 *      (they must consist of one or more letters, digits, or _)
 *      (-ab is treated as -a -b)
 *    string options:            --debug
 *    values:   file.txt
 *              42
 *              "string with \n embedded and \\n escaped characters"
 *              'also can be single \n quotes'
 *              -       (single dash)
 *              -2      (digit interpreted as value)
 *
 */
public class CmdArgs {
  public void help() {
    throw new CmdArgsException();
  }

  public void exception(String msg) {
    throw new CmdArgsException(msg);
  }

  public static void setDebug(boolean f) {
    db = f;
  }

  private static boolean db;

  /**
   * Constructor
   *
   * @param args String[] passed to main(); if the first argument
   *    is --debugargs, strips it off and sets verbose mode for this
   *    object
   * @param defaults : if not null, string to parse and insert in front
   *   of args as defaults
   *
   *     It has the following format:
   *        ==  : set mode to equivalencies
   *        !!  : set mode to defaults (the initial mode)
   *
   *     In defaults mode, tokens are parsed as arguments
   *     In equivalencies mode, pairs are read, for --long -short equivalences
   *
   * @param helpMsg : if not null, help message to display if exception occurs
   */
  public CmdArgs(String[] args, String defaults, String helpMsg) {
    if (helpMsg != null) {
      this.helpMsg = helpMsg;
    }

    if (defaults != null) {
      boolean modeDef = true;

      TextScanner s = new TextScanner(defaults);
      while (true) {
//        Token tk = s.read();
//
        s.readWS();
        if (s.eof()) {
          break;
        }

//        if (tk.eof()) {
//          break;
//        }
        String arg = s.readWordOrStr(false); //)Scanner.removeQuotes(tk.text()); // s.readWordOrStr();
        if (db) {
          System.out.println("CmdArgs, scanning " + TextScanner.debug(arg));
        }
        if (arg.equals("!!")) {
          modeDef = true;
        }
        else if (arg.equals("==")) {
          modeDef = false;
        }
        else {
          if (modeDef) {
            addArguments(arg);
          }
          else {
            String arg2 = s.readWordOrStr(true); //Scanner.removeQuotes(s.read(true).text());
            if (db) {
              System.out.println("adding equivs " + arg + " == " + arg2);
            }
            equivs.add(arg);
            equivs.add(arg2);
          }
        }
      }
    }

    int start = 0;
    if (args.length > 0 && args[0].equals("--debugargs")) {
      db = true;
      start = 1;
    }
    addArguments(args, start);
  }

  /**
   * Throw an 'unsupported' CmdArg exception with last option parsed
   */
  public void unsupported() {
    throw new CmdArgsException("Unsupported option: " + lastOption);
  }

  /**
   * Parse arguments
   *
   * @param args String[]
   */
  private void addArguments(String[] args, int start) {
    if (db) {
      System.out.println("addArguments");
    }
    // parse the arguments as option or value.
    for (int i = start; i < args.length; i++) {
      String s = args[i];
      if (db) {
        System.out.println(" arg: " + s);
      }

      // If it starts with a space, surround it with quotes to make it a value.
      if (s.startsWith(" ")) {
        s = "\"" + s.trim() + "\"";
      }

      // is it a character option?
      if (isStringOption(s)) {
        if (db) {
          System.out.println("  pushing " + s.substring(1));
        }
        strings.push(s); //s.substring(1));
      }
      else if (isOption(s)) {
        for (int j = 1; j < s.length(); j++) {
          char oc = s.charAt(j);
          if (db) {
            System.out.println("  pushing " + "-" + oc);
          }
          strings.push("-" + oc);
        }
      }
      else {
        if (db) {
          System.out.println("  pushing " + s);
        }
        strings.push(s);
      }
    }
  }

  // determine if there are more arguments to process
  public boolean hasNext() {
    boolean f = (argNumber < strings.length());
    if (db) {
      System.out.println("hasNext " + state() + " returning " + f);
    }
    return f;
  }

  private String state() {
    StringBuilder sb = new StringBuilder("<");
    sb.append(argNumber);
    sb.append(" of " + strings.length());
    if (argNumber < strings.length()) {
      sb.append(", next=" + strings.peek());
    }
    sb.append(">");
    return sb.toString();
  }

  // determine if there is a next argument which is a value
  public boolean nextIsValue() {
    boolean out = hasNext() && !nextIsOption(); //strings.peekString(0).startsWith("-");
    if (db) {
      System.out.println("nextIsValue " + state() + " returning " + out);
    }
    return out;
  }

  public String peek() {
    String out = null;
    if (hasNext()) {
      out = strings.peekString(0);
    }
    return out;
  }

  public boolean nextIsInt() {
    String s = peek();
    boolean out = false;
    if (s != null) {
      try {
        Integer.parseInt(s);
        out = true;
      }
      catch (NumberFormatException e) {}
    }
    return out;
  }

  /**
   * Determine if there is a next argument that is a single-character or
   * multiple-character option
   * @return boolean
   */
  public boolean nextIsOption() {
    boolean out = false;
    do {
      if (!hasNext()) {
        break;
      }
      String s = strings.peekString(0);
      if (!isOption(s)) {
        break;
      }
      out = true;
    }
    while (false);
    if (db) {
      System.out.println("nextIsChar " + state() + " returning " + out);
    }
    return out;
  }

  /**
   * Determine if there is a next argument that is a single-character
   * option
   * @return boolean
   */
  public boolean nextIsChar() {
    boolean out = false;
    do {
      if (!hasNext()) {
        break;
      }
      String s = strings.peekString(0);
      if (!isOption(s)) {
        break;
      }
      if (s.length() > 2) {
        break;
      }
      out = true;
    }
    while (false);
    if (db) {
      System.out.println("nextIsChar " + state() + " returning " + out);
    }
    return out;
  }

  /**
   * Read next argument as an option.  Throw exception if missing or not
   * an option.
   *
   * @return String
   */
  public String nextOption() {
    if (nextIsValue()) {
      throw new CmdArgsException("Unexpected value in arguments: " +
                                 strings.peek());
    }
    String st = null;
    if (db) {
      st = state();
    }

    lastOption = findEquiv(strings.popString());
    if (db) {
      System.out.println("nextOption " + st + " returning " + lastOption);
    }
    return lastOption;
  }

//  public String nextArg() {
//    return strings.popString();
//  }

  /**
   * Examine equivalencies table to convert long option to short
   * @param s String
   * @return original string, or equivalent form
   */
  private String findEquiv(String s) {
    String out = s;
    for (int i = 0; i < equivs.length(); i += 2) {
      if (s.equals(equivs.getString(i))) {
        out = equivs.getString(i + 1);
        break;
      }
    }
    if (db) {
      System.out.println("findEquiv " + s + " is " + out);
    }
    return out;
  }

  /**
   * Read next argument as a single-character option.  Throw exception if
   * missing or not of this type
   * @return char
   */
  public char nextChar() {
    String s = nextOption();
    if (s.length() > 2) {
      throw new CmdArgsException("Unexpected argument: " + s);
    }
    if (db) {
      System.out.println("nextChar " + state() + " returning " + s.charAt(1));
    }

    return s.charAt(1);
  }

  private String lastOption;

  // read the next argument if it matches a particular option;
  // if there are no more, or it's not a match, return false
  public boolean peekOption(String c) {
    if (db) {
      System.out.println("peekOption " + state());
    }
    boolean out = true;
    do {
      if (hasNext()) {
        String s = strings.peekString(0);
        if (isOption(s)
            && optionBody(s).equals(c)) {
          nextOption();
          break;
        }
      }
      out = false;
    }
    while (false);
    if (db) {
      System.out.println("  returning " + out);
    }
    return out;

  }

  private static String optionBody(String s) {
    int i = 1;
    if (s.startsWith("--")) {
      i = 2;
    }
    return s.substring(i);
  }

  // parse next argument as integer
  public int nextInt() {
    if (db) {
      System.out.println("nextInt " + state());
    }
    return Integer.parseInt(nextValue());
  }

  // parse next argument as double
  double nextDouble() {
    if (db) {
      System.out.println("nextDouble " + state());
    }
    return Double.parseDouble(nextValue());
  }

  // read next argument as a value; throw exception if it's
  // not a value, or is missing
  public String nextValue() {
    if (db) {
      System.out.println("nextValue " + state());
    }
    if (!nextIsValue()) {
      StringBuilder sb = new StringBuilder("Missing value in arguments");
      if (lastOption != null) {
        sb.append(" for option " + lastOption);
      }
      throw new CmdArgsException(sb.toString());
    }
    if (db) {
      System.out.println(" returning " + strings.peek());
    }
    String s = strings.popString();
    return TextScanner.removeQuotes(s);
  }

//  /**
//   * Read next argument as an integer
//   * @return int
//   */
//  public int nextInt() {
//    String s = nextValue();
//    return Scanner.parseInt(s);
//}

  // indicate that processing is done; generate exception if more
  // arguments remain unprocessed
  public void done() {
    if (db) {
      System.out.println("done; " + state());
    }
    if (argNumber < strings.length()) {
      StringBuilder sb = new StringBuilder("Unexpected arguments: ");
      while (!strings.isEmpty()) {
        sb.append(strings.popString());
        sb.append(" ");
      }
      throw new CmdArgsException(sb.toString());
    }

  }

  /*	Get next value as a path, and convert to abstract form.
   */
  public File nextPath() {
    if (db) {
      System.out.println("nextPath; " + state());
    }
    return new File(nextValue());
  }

  private static boolean isStringOption(String s) {
    return s.startsWith("--");
  }

  /**
   * Determine if string represents an option (starts with "-" or "--", and
   * next character is letter or _)
   * @param s String
   * @return boolean
   */
  private static boolean isOption(String s) {
    boolean out = false;
    do {
      if (!s.startsWith("-")) {
        break;
      }
      if (s.length() > 1
          && (
              s.charAt(1) != '-'
              && !TextScanner.isIdentifierStart(s.charAt(1)))) {
        break;
      }
      out = true;
    }
    while (false);
    return out;
  }

  private void addWord(String str, DArray sa,
                       int pos, int len, boolean addZeroLen) {
    if (addZeroLen || len > 0) {
      String out = str.substring(pos, pos + len);
      sa.add(out);
      if (db) {
        System.out.println("addWord [" + out + "]");
      }

    }
  }

  /**
   * Parse string into individual strings, add to command arguments
   * @param str : string to split into individual cmd line args
   */
  public void addArguments(String str) {
    DArray sa = new DArray();
    {

      if (db) {
        System.out.println("addArguments [" + str + "]");
      }
      char strDelim = 0;
      int len = 0;
      int pos = 0;
      for (int i = 0; i < str.length(); i++) {
        boolean addChar = false;
        char c = str.charAt(i);

        if (db) {
          System.out.println(" char = " + c);
        }
        if (strDelim != 0 && c == strDelim) {
          strDelim = 0;
          addWord(str, sa, pos, len, true);
          len = 0;
          addChar = false;
        }
        else if (strDelim == 0 && (c == '\'' || c == '\"')) {
          if (db) {
            System.out.println(" starting str");
          }
          addWord(str, sa, pos, len, false);
          len = 0;
          strDelim = c;
        }
        else if (c == ' ' && strDelim == 0) {
          if (db) {
            System.out.println(" whitespace, adding word");
          }
          addWord(str, sa, pos, len, false);
          len = 0;
        }
        else {
          addChar = true;
        }
        if (addChar) {
          if (len == 0) {
            pos = i;
          }
          len++;
        }
      }
      if (strDelim != 0) {
        throw new CmdArgsException("Missing quote in arguments");
      }
      addWord(str, sa, pos, len, false);
    }
    addArguments(sa.toStringArray(), 0);
  }

  private DQueue strings = new DQueue();
  private int argNumber;
  private String helpMsg = "(No help provided)\n";
  private DArray equivs = new DArray();

  class CmdArgsException
      extends ScanException {
    public CmdArgsException() {
      super(helpMsg);
    }

    public CmdArgsException(String msg) {
      super(msg + "\n" + helpMsg);
    }
  }
}
