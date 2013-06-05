package base;

public class DQueue implements Cloneable {
  /**
   * Get string describing object
   * @return String
   */
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("[\n");
    for (int i = 0; i < length(); i++) {
      Object obj = peekAt(i);
      //      if (i > 0) {
      //        sb.append(' ');
      //      }
      sb.append(' ');
      sb.append(obj);
      if (sb.charAt(sb.length() - 1) != '\n')
        sb.append('\n');
    }
    sb.append("]");
    return sb.toString();
  }

  public Object peek() {
    return peekAt(0);
  }

  /**
   * Clone object.
   * For good summary of what's entailed in Java cloning,
   * see Core Java, Vol. 1, p. 262.
   *
   * @return Object
   */
  public Object clone() {
    try {
      DQueue d = (DQueue) super.clone();

      // clone the array
      d.a = (Object[]) d.a.clone();
      return d;
    } catch (CloneNotSupportedException e) {
      throw new InternalError(e.toString());
    }
  }

  public void push(Object obj) {
    push(obj, false);
  }

  public void push(Object obj, boolean toFront) {
    if (spaceRemaining() <= 1) {
      expandBuffer();
    }
    if (!toFront) {
      a[tail++] = obj;
      if (tail == a.length) {
        tail = 0;
      }
    } else {
      if (--head < 0) {
        head = a.length - 1;
      }
      a[head] = obj;
    }

  }

  public int length() {
    final boolean db = false;

    int k = tail - head;
    if (tail < head) {
      k += a.length;
    }
    if (db)
      Streams.out.println("length, tail=" + tail + ", head=" + head
          + ", a.length=" + a.length + ", returning " + k);
    return k;
  }

  public Object pop() {
    if (length() == 0) {
      throw new Error("pop of empty queue");
    }

    Object out = a[head];
    if (++head == a.length) {
      head = 0;
    }
    return out;
  }

  public Object peekAt(int n) {
    if (n >= length()) {
      throw new Error("peek past end of queue");
    }
    return a[calcPos(n)];
  }

  public String peekString(int n) {
    return (String) peekAt(n);
  }

  private int calcPos(int fromStart) {
    int k = head + fromStart;
    if (k >= a.length) {
      k -= a.length;
    }
    return k;
  }

  private void expandBuffer() {
    int nSize = a.length * 2;
    Object[] a2 = new Object[nSize];
    for (int i = 0, j = head; j != tail; i++) {
      a2[i] = a[j];
      if (++j == a.length) {
        j = 0;
      }
    }
    tail = length();
    head = 0;
    a = a2;
  }

  public int popInt() {
    return ((Integer) pop()).intValue();
  }

  public void pushInt(int i) {
    push(new Integer(i));
  }

  public void pushInt(int i, boolean toFront) {
    push(new Integer(i), toFront);
  }

  public double popDouble() {
    return ((Double) pop()).doubleValue();
  }

  public void pushDouble(double d) {
    push(new Double(d));
  }

  public String popString() {
    return (String) pop();
  }

  public boolean isEmpty() {
    return length() == 0;
  }

  private int spaceRemaining() {
    return a.length - length();
  }

  int head() {
    return head;
  }

  int tail() {
    return tail;
  }

  public DQueue() {
    this(16);
  }

  public DQueue(int initialCapacity) {
    a = new Object[1 + initialCapacity];
  }

  private Object[] a; // = new Object[20];

  private int head, tail;
}
