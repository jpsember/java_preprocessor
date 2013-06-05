package base;
import java.io.*;

public class PathFilter
     extends javax.swing.filechooser.FileFilter
        implements FilenameFilter {

        /**
   * Accept file?
   * @param dir File, or null
   * @param name String
   * @return boolean
   */
  public boolean accept(File dir, String name) {

    boolean flag = false;
      File f = new File(name);

      if (f.isDirectory()) {
        flag = true;
      } else
flag = accept(name);
    return flag;
  }


  public  boolean accept(File file) {
    return accept(file.getPath());
  }

  public  String getDescription() {
    if (ext.length() > 0) {
      StringBuilder sb = new StringBuilder();
      for (int i = 0 ;i < ext.length(); i++) {
        if (i > 0) sb.append(", ");
        sb.append("*.");
        sb.append(ext.get(i));
      }
      return sb.toString();
    }
    return "*** override PathFilter: getDescription ***";
  }

  public PathFilter() {}
  public PathFilter(String extensions) {
    TextScanner sc = new TextScanner(extensions);
    while (true) {
      String w = sc.readWord();
      if (w == null) break;
      ext.add(w);
    }
  }

  public boolean accept(String path) {
    String e = Path.getExtension(path);
    for (int i = 0; i < ext.length(); i++)
      if (e.equals(ext.getString(i))) {
        return true;
      }
  return false;
  }

  protected DArray ext = new DArray();
}
