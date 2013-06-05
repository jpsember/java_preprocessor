package base;

import java.io.*;

public class Streams2 {

  /**
   * @return
   */
  public static File homeDirectory() {
    if (homeDir == null) {
      String hd = System.getProperty("user.home");
      homeDir = new File(hd);
    }
    return homeDir;
  }

  /**
   * Get the root directory
   * 
   * @return
   */
  public static File rootDirectory() {
    if (rootDirectory == null) {
      setRootDirectory(homeDirectory());
    }

    return rootDirectory;
  }

  /**
   * Write text file if it has changed.
   * @param filename : name of file, relative to root directory
   * @param content : new contents of file
   * @throws IOException 
   */
  public static void writeIfChanged(String filename, String content)
      throws IOException {
    final boolean db = false;

    File path = new File(Streams2.rootDirectory(), filename);
    String txOld = "";

    try {
      txOld = Streams2.readTextFile(path.getPath());
    } catch (IOException e) {
      Tools.warn("couldn't find existing file " + path);
    }
    if (!content.equals(txOld)) {
      if (db) {
        Streams.out.println("old=" + Tools.d(txOld, 80, false) + "\nnew="
            + Tools.d(content, 80, false));
        Streams.out.println("writing new file to " + path);
      }
      Streams2.writeTextFile(path, content);
    }
  }


  /**
   * Define the home directory for the server.
   * We use this as the base for files that may not be visible to the client,
   * for example, backup files, example photos, calendar scripts.
   * 
   * @param dir : home directory; if null, reads it from user.home system property
   */
  public static void setHomeDirectory(File dir) {
    // if (dir == null) dir = homeDirectory();
    homeDir = dir;
    // in case it's null, read it back so it is set to user.home
    homeDirectory();
  }

  /**
   * The root directory for the application
   * 
   * "Since there's no telling which directory the servlet engine was launched
   * from, you should always use absolute pathnames from inside a servlet.
   * Usually I pass in the root directory in an InitParameter, and create File
   * objects relative to that directory."
   */
  private static File rootDirectory;

  private static File homeDir;

  /**
   * Define the root directory (ROOT) for the servlet. All data files are stored
   * within the tree of directories rooted at this directory. Creates a
   * directory ROOT/_temp_ for storing temporary files.
   * 
   * For instance, we store the password .png files in this directory.
   * 
   * @param rootDir : root directory
   * @param staleDelay : if >= 0, any temporary file within the temporary
   *          directory older than staleDelay milliseconds will be deleted
   * @throws ServletException if problem creating or preparing directory
   */
  public static void setRootDirectory(File rootDir) {
    rootDirectory = rootDir;
  }

  /**
   * Create a directory on the client, optionally delete files of a certain age.
   * @param path : path of directory, relative to rootDirectory()
   * @param staleDelay : if >= 0, certain files within the temporary
   *          directory older than staleDelay milliseconds will be deleted
   * @param tempExt : only files within directory that end with this string
   *          will be candidates for deletion
   * @throws ServletException if problem creating or preparing directory
   */
  public static File createDirectory(String path, long staleDelay,
      String tempExt) throws IOException {
    if (false) {
      staleDelay = 1000L * 120;
      Tools.warn("setting short staleDelay");
    }

    final boolean db = false;

    File newDir = new File(rootDirectory(), path);

    if (!newDir.exists()) {
      if (db)
        Streams.out.println("attempting to make directory " + newDir);

      if (!newDir.mkdir())
        throw new IOException("failed to make " + newDir);
    }

    if (staleDelay >= 0) {
      long timeCutoff = System.currentTimeMillis() - staleDelay;

      File[] lst = newDir.listFiles();

      if (db)
        Streams.out
            .println("examining files in " + newDir + " for flushing...");

      for (int i = 0; i < lst.length; i++) {
        File f = lst[i];
        if (!f.isFile() || f.isHidden() || !f.getName().endsWith(tempExt))
          continue;

        if (f.lastModified() < timeCutoff) {
          if (db)
            Streams.out.println(" deleting " + f);
          if (!f.delete())
            throw new IOException("unable to delete: " + f);
        }
      }
    }
    return newDir;
  }

//  /**
//   * Get all files of a particular type
//   * @param dir : directory to examine
//   * @param extension : extension to filter by
//   * @return DArray : an array of strings
//   */
//  public static DArray getFileList(String dir, String extension) {
//    DArray list = null;
//    if (Streams.streamFromPath != null) {
//      list = ((StreamFromPathEm) Streams.streamFromPath).getFileList(dir,
//          extension);
//    }
//
//    if (list == null) {
//      File f = new File(dir);
//      if (!f.isDirectory()) {
//        f = f.getParentFile();
//      }
//
//      list = Path.getFileList(f, extension, true);
//    }
//    return list;
//  }

  static void ensureApplet() {
    if (!Streams.isApplet()) {
      throw new RuntimeException("Not an applet");
    }
  }

  public static void copyFile(File source, File dest, boolean overwriteExisting)
      throws IOException {
    InputStream in = new BufferedInputStream(new FileInputStream(source));
    copyFile(in, dest, overwriteExisting);
  }

  /**
   * Copy a file.
   * @param inp InputStream containing file
   * @param dest : file to write
   */
  public static void copyFile(InputStream inp, File dest,
      boolean overwriteExisting) throws IOException {

    if (!overwriteExisting && dest.exists()) {
      throw new IOException("Cannot overwrite " + dest.getAbsolutePath());
    }

    OutputStream out = new BufferedOutputStream(new FileOutputStream(dest));
    final int BUFF_SIZE = 4096;
    byte[] buff = new byte[BUFF_SIZE];
    while (true) {
      int len = inp.read(buff);
      if (len < 0) {
        break;
      }
      out.write(buff, 0, len);
    }
    out.close();
  }

  public static void write(byte[] bytes, File dest, boolean overwriteExisting)
      throws IOException {
    copyFile(new ByteArrayInputStream(bytes), dest, overwriteExisting);
  }

  public static byte[] readBinaryFile(String path) throws IOException {
    FileInputStream r = new FileInputStream(path);
    ByteArrayOutputStream w = new ByteArrayOutputStream();

    byte[] b = new byte[2000];
    while (true) {
      int len = r.read(b);
      if (len < 0)
        break;
      w.write(b, 0, len);
    }
    b = w.toByteArray();
    r.close();
    return b;
  }

  /**
   * Read a file into a string
   * @param path String
   * @return String
   */
  public static String readTextFile(String path) throws IOException {
    return Streams2.readTextFile(path, false);
  }

  public static String readTextFile(String path, boolean withinJAR)
      throws IOException {
    StringBuilder sb = new StringBuilder();
    Reader r = Streams.reader(path, withinJAR);
    while (true) {
      int c = r.read();
      if (c < 0) {
        break;
      }

      sb.append((char) c);
    }
    r.close();
    return sb.toString();
  }

  public static PrintStream printStream(String path) throws IOException {
    OutputStream out = Streams.outputStream(path);
    Tools.ASSERT(path != null);
    return // (path == null) ? new NCPrintStream(out) : 
      new PrintStream(out);
  }

  public static void writeTextFile(File file, String content)
      throws IOException {
    BufferedWriter w = new BufferedWriter(new FileWriter(file));
    w.write(content);
    w.close();
  }

  /**
   * 
   * Write string to text file.  Throws IOExceptions as ScanExceptions.  Use
   * File version as alternative.
   * 
   * @param path
   * @param content
   * @deprecated avoid masking IOExceptions
   */
  public static void writeTextFile(String path, String content) {
    try {
      Writer w = Streams.writer(path);
      w.write(content);
      w.close();
    } catch (IOException e) {
      ScanException.toss(e);
    }
  }

//  /**
//   * Read a line of text from System.in, or from the console if appropriate
//   * @return String
//   */
//  public static String readLine(String prompt, CommandHistory history) {
//    String s = null;
//
//    if (Streams.streamFromPath != null) {
//      s = ((StreamFromPathEm) Streams.streamFromPath).readLineFromConsole(
//          prompt, history);
//    } else {
//      Streams.out.print(prompt);
//      StringBuilder sb = new StringBuilder();
//      try {
//        while (true) {
//          int c = Streams.in.read();
//          if (c < 0) {
//            break;
//          }
//          Streams.out.print((char) c);
//          if (c == '\n') {
//            break;
//          }
//          sb.append((char) c);
//        }
//      } catch (IOException e) {
//        Streams.out.println("readLine IOException: " + e);
//      }
//      s = sb.toString().trim();
//    }
//    return s;
//  }

  public static String readTextFile(Reader s) throws IOException {
    StringBuilder sb = new StringBuilder();
    while (true) {
      int c = s.read();
      if (c < 0)
        break;
      sb.append((char) c);
    }
    s.close();
    return sb.toString();
  }

  /**
   * Move a file to a temporary directory under a different name
   * @param original : file to be moved
   * @param backupDirectory : backup directory; if null, creates one
   *   named "_tmp_" in original file's directory; if no backup directory
   *   exists, creates one
   * @param maxBackups : if > 0, deletes older copies of backup file
   * @return moved file
   * @throws IOException 
   */
  public static File moveFileToTemp(File original, File backupDirectory,
      int maxBackups) throws IOException {

    final boolean db = false;

    if (db)
      Streams.out.println("Streams2.moveFileToTemp original=" + original
          + " backupDir=" + backupDirectory + " maxBackups=" + maxBackups);

    if (backupDirectory == null) {
      backupDirectory = new File(original.getParent(), "_tmp_");
      if (db)
        Streams.out.println(" created backupDirectory " + backupDirectory);

    }
    if (!backupDirectory.isDirectory()) {
      if (!backupDirectory.mkdirs())
        throw new IOException("unable to make backup dir: " + backupDirectory);
    }

    // Get name of backup file
    String nameOnly = original.getName();
    if (db)
      Streams.out.println(" nameOnly=" + nameOnly);

    File backupFile = null;
    if (maxBackups <= 0)
      maxBackups = Integer.MAX_VALUE;

    File oldestBackup = null;

    int number = 0;
    for (; number < maxBackups; number++) {
      backupFile = new File(backupDirectory, nameOnly + "_" + number + ".txt");
      if (db)
        Streams.out.println(" seeing if backupFile exists: " + backupFile);

      if (!backupFile.exists())
        break;

      if (db)
        Streams.out.println(" it does...");

      if (oldestBackup == null
          || oldestBackup.lastModified() > backupFile.lastModified()) {
        oldestBackup = backupFile;
        if (db)
          Streams.out.println("  setting as oldest backup");

      }
      backupFile = null;
    }
    if (backupFile == null) {
      if (db)
        Streams.out.println(" using oldest backup as backupFile");

      backupFile = oldestBackup;
    }
    if (db)
      Streams.out.println(" backupFile=" + backupFile);

    if (backupFile.exists()) {
      delete(backupFile);
      //			
      //			if (!backupFile.delete())
      //				throw new IOException("failed to delete "+backupFile);
    }
    if (db)
      Streams.out.println(" renaming original to backup");

    renameTo(original, backupFile);
    //		
    //		if (!original.renameTo(backupFile))
    //		throw new IOException("failed to move " + original + " to " + backupFile);
    return backupFile;
  }

  public static void renameTo(File src, File dest) throws IOException {
    if (!src.renameTo(dest))
      throw new IOException("failed to rename " + src + " to " + dest);

  }

  public static void delete(File f) throws IOException {
    if (!f.delete())
      throw new IOException("failed to delete " + f);

  }
}
