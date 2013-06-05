package base;

import java.io.*;

public class StringInputStream
    extends InputStream {

  public StringInputStream(String s) {
    this.src = s;
  }

  public int read() throws IOException {
    int out = -1;

    if (cursor < src.length()) {
      out = src.charAt(cursor);
      cursor++;
    }
    return out;
  }

  public int available() {
    return src.length() - cursor;
  }

  private String src;
  private int cursor;
}
