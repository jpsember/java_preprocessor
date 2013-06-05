package base;

import java.io.*;

public class Path {
  /**
   * Constructor:  private, since no instances can exist.
   */
  private Path() {
  }

  public static boolean hasExtension(String path) {
    return (getExtension(path).length() != 0);
  }

  public static String addExtension(String path, String ext) {
    if (!hasExtension(path)) {
      path = changeExtension(path, ext);
    }
    return path;
  }

  /**
   * Change extension of a file.
   * @param name : current filename
   * @param ext : new extension, "" for none
   * @return String representing new filename
   */
  public static String changeExtension(String name, String ext) {
    ext = extString(ext);
    String out = name;
    String currExt = extString(name);
    if (!currExt.equalsIgnoreCase(ext)) {
      out = removeExt(name);
      if (ext.length() > 0) {
        out = out + "." + ext;
      }
    }
    return out;
  }

  /**
   * Replace the extension of a file
   * @param f : existing File
   * @param ext : new extension
   * @return File with replaced extension
   */
  public static File changeExtension(File f, String ext) {

    // get the name from the file; we will change its extension.
    String name = f.getName();
    if (name.length() == 0) {
      throw new RuntimeException("Set extension of empty name: " + f);
    }

    String parent = f.getParent();
    return new File(parent, changeExtension(name, ext));
  }

  public static String getParent(String f) {
    int i = f.lastIndexOf('/');
    if (i < 0) {
      i = 0;
    }
    return f.substring(0, i);
  }

  /**
   * Get the user directory
   *
   * @return
   */
  public static String getUserDir() {
    if (Streams.isApplet()) {
      return "";
    }

    String h = System.getProperty("user.dir");
    return h;
  }

  /**
   * Convert string to an extension; add '.' if necessary;
   * if '.' already exists, remove everything to left of it
   */
  private static String extString(String s) {
    String out = null;
    int pos = s.lastIndexOf('.');
    out = s.substring(pos + 1);
    return out;
  }

  /**
   * Get extension of a file
   * @param file : File
   * @return String containing extension, empty if it has none (or is a directory)
   */
  public static String getExtension(File file) {
    String ext = "";
    String f = file.getName();
    int extPos = f.lastIndexOf('.');
    if (extPos >= 0) {
      ext = f.substring(extPos + 1);
    }
    return ext;
  }

  public static boolean hasExtension(File file) {
    return getExtension(file).length() > 0;
  }

  public static String getExtension(String path) {
    return getExtension(new File(path));
  }

  /**
   * Remove extension, if any, from path
   */
  public static String removeExt(String path) {
    int extPos = path.lastIndexOf('.');
    if (extPos >= 0) {
      return path.substring(0, extPos);
    }
    return path;
  }

//  /**
//   * Get all files of a particular type
//   * @param dir : directory to examine
//   * @param extension : extension to filter by
//   * @return DArray : an array of File objects
//   */
//  public static DArray getFileList(File dir, String extension, boolean asStrings) {
//    Tools.ASSERT(dir.isDirectory(), "getFileList, not a directory");
//    DArray list = new DArray();
//
//    String[] srcList = dir.list(
//        new FNameFilter(extension, "getFileList", false));
//
//    for (int i = 0; i < srcList.length; i++) {
//      File f = new File(dir,srcList[i]);
//      if(asStrings)
//        list.add(f.getPath());
//      else
//        list.add(f);
//    }
//    return list;
//  }


  /**
   * Get the next file in a list, based on previous file's position
   * @param fileList : list of files
   * @param previous : previous file, or null
   * @param wrap : true to wrap at bottom
   * @return next file, or null if none remain
   */
  public static File getNextFile(DArray fileList, File previous,
                                 boolean wrap) {

    int pos = 0;

    if (previous != null) {
      while (pos < fileList.length()) {
        File nxt = (File) fileList.get(pos);
        if (previous.equals(nxt)) {
          pos++;
          break;
        }
        pos++;
      }
    }
    File out = null;
    if (fileList.length() > 0) {
      if (wrap) {
        pos = pos % fileList.length();
      }
      if (pos < fileList.length()) {
        out = (File) fileList.get(pos);
      }
    }
    return out;
  }

  /**
   * Get the next file in a list, based on previous file's position
   * @param fileList : list of files
   * @param previous : previous file, or null
   * @param wrap : true to wrap at bottom
   * @return next file, or null if none remain
   */
  public static String getNextFile(DArray fileList, String previous,
                                   boolean wrap) {

    int pos = 0;

    if (previous != null) {
      while (pos < fileList.length()) {
        String nxt = fileList.getString(pos);
        if (previous.equals(nxt)) {
          pos++;
          break;
        }
        pos++;
      }
    }
    String out = null;
    if (fileList.length() > 0) {
      if (wrap) {
        pos = pos % fileList.length();
      }
      if (pos < fileList.length()) {
        out = fileList.getString(pos);
      }
    }
    return out;
  }

}
