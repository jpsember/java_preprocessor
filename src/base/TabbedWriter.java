package base;
import java.io.*;

public class TabbedWriter extends Writer {
  private int margin;
  public void indent() {
    margin += 4;
  }
  public void outdent() {
    margin = Math.max(0,margin-4);
  }
  private char indent, prompt;

  public void setIndentChars(char indent, char prompt) {
    this.indent = indent;
    this.prompt = prompt;
  }

  public TabbedWriter(Writer w) {
    this.w = w;
    setIndentChars(' ','\0');
  }
  public TabbedWriter(OutputStream w) {
    this(new OutputStreamWriter(w));
//
//    this.w = new OutputStreamWriter(w);
  }

  private Writer w;

  public void write(int c) throws IOException {
//    w.write(c);
//    w.write('.');
    if (doIndent) {
      for (int i = 0; i < margin; i++)
        w.write(indent);
      if (prompt != '\0')
        w.write(prompt);
      doIndent = false;
    }
    w.write(c);
    if (c == '\n') {
      doIndent = true;
      flush();
    }
  }
  private boolean doIndent;

  public void write(char[] c, int off, int len) throws IOException
  {
    for (int i = 0; i < len; i++)
      write(c[off++]);
  }
  public void close() throws IOException {
    w.close();
  }
  public void flush() throws IOException {
    w.flush();
  }
}
