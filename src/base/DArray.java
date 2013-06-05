package base;

import java.util.*;
import java.lang.reflect.*;

final public class DArray
    implements Cloneable {

  /**
   * Build a DArray that contains one object
   *
   * @param o1
   *          Object
   * @return DArray
   */
  public static DArray build(Object o1) {
    DArray out = new DArray();
    out.add(o1);
    return out;
  }

  public DArray getDArray(int i) {
    return (DArray) get(i);
  }

  public int size() {
    return itemsUsed;
  }

  public void append(DArray src) {
    insert(src, 0, src.length(), length());
  }

  /**
   * Build a DArray that contains two objects
   *
   * @return DArray
   */
  public static DArray build(Object o1, Object o2) {
    DArray out = new DArray();
    out.add(o1);
    out.add(o2);
    return out;
  }

  /**
   * Build a DArray that contains three objects
   *
   * @return DArray
   */
  public static DArray build(Object o1, Object o2, Object o3) {
    DArray out = new DArray();
    out.add(o1);
    out.add(o2);
    out.add(o3);
    return out;
  }

//  /**
//   * Dump contents of an array of FPoint2's to a string
//   *
//   * @param a
//   *          DArray
//   * @param allDigits
//   *          true to display all digits of FPoint2's
//   * @return String with each point indented by a space
//   */
//  public String dumpFPoint2(boolean allDigits) {
//    StringBuilder sb = new StringBuilder();
//    for (int i = 0; i < length(); i++) {
//      sb.append(' ');
//      sb.append(getFPoint2(i).toString(allDigits, false));
//      sb.append('\n');
//    }
//    return sb.toString();
//  }

  /**
   * Dump contents of an array of Doubles to a string
   *
   * @param a
   *          DArray
   * @param allDigits
   *          true to display all digits of Doubles
   * @return String with each value indented by a space
   */
  public String dumpDouble(boolean allDigits) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < length(); i++) {
      sb.append(' ');
      double t = getDouble(i);
      if (allDigits) {
        sb.append(t);
      }
      else {
        sb.append(Tools.f(t));
      }
      sb.append('\n');
    }
    return sb.toString();
  }

  /**
   * Swap two elements of the array
   *
   * @param i :
   *          first element
   * @param j :
   *          second element
   */
  public void swap(int i, int j) {
    Object iObj = n[i];
    n[i] = n[j];
    n[j] = iObj;
  }

  /**
   * Ensure that the array has enough space to fit a certain number of items. If
   * not, allocates enough room for the size to double before growing again.
   *
   * @param n :
   *          current array, or null if it doesn't exist
   * @param used : #
   *          items in use in array
   * @param desiredLen :
   *          capacity that we need immediately (if it grows, its new size will
   *          be larger than this amount)
   * @return new array
   */
  private static Object[] ensureCapacity(Object[] n, int used, int desiredLen) {

    int currLen = 0;
    if (n != null) {
      currLen = n.length;
    }
    if (false) {
      Tools.ASSERT(used <= currLen, "ensureCapacity called with used=" + used
                   + ", n=" + n + ", len=" + currLen);
    }

    // Is growth necessary?

    if (currLen < desiredLen) {

      final boolean db = false;

      int newLen = Math.max(20, desiredLen * 2);
      Object[] n2 = new Object[newLen];
      if (db) {
        System.out.println("ensureCap curr=" + currLen + " desiredLen="
                           + desiredLen + " newLen=" + newLen);
      }

      if (used > 0) {
        if (db) {
          System.out.println(" arrayCopy n to n2");
          System.out.println(" n=" + n + "\n n2=" + n2 + "\n used=" + used);
        }
        System.arraycopy(n, 0, n2, 0, used);
      }
      n = n2;
    }
    return n;
  }

  /**
   * Ensure that the array has enough space to fit a certain number of items. If
   * not, allocates enough room for the size to double before growing again.
   *
   * @param n :
   *          array of ints
   * @param used :
   *          number of items actually in use, to be copied if size increases
   * @param len :
   *          required capacity
   */
  private int[] ensureIntCapacity(int[] n, int used, int len) {
    int currLen = 0;
    if (n != null) {
      currLen = n.length;
    }
    if (false) {
      Tools.ASSERT(used <= currLen, "ensureCapacity called with used=" + used
                   + ", n=" + n + ", len=" + currLen);
    }

    if (currLen < len) {
      int newLen = Math.max(20, len * 2);
      int[] n2 = new int[newLen];

      if (false) {
        if (used > 0) {
          if (n == null || used > n.length || n2 == null || used > n2.length) {
            throw new Error("ensureIntCapacity, n=" + n + ", n2=" + n2
                            + ", used=" + used);
          }
        }
      }
      if (used != 0) {
        System.arraycopy(n, 0, n2, 0, used);
      }

      n = n2;
    }
    return n;
  }

  /**
   * Shift all elements from a certain point forward (to make room for a new
   * one)
   *
   * @param position :
   *          index of first element to be shifted forward
   * @param distance :
   *          number of slots to shift them forward
   */
  private void shift(int position, int distance) {
    if (distance > 0) {
      int shift = length() - position;
      n = ensureCapacity(n, length(), length() + distance);
      if (shift > 0) {
        System.arraycopy(n, position, n, position + distance, shift);
      }
      itemsUsed += distance;
    }
  }

  /**
   * Insert items from another DArray to this one
   *
   * @param source :
   *          DArray to read items from
   * @param srcIndex :
   *          position in source array of first item
   * @param length :
   *          number of items to insert
   * @param destIndex :
   *          destination index for items
   */
  public void insert(DArray source, int srcIndex, int length, int destIndex) {
    if (length > 0) {
      shift(destIndex, length);
      System.arraycopy(source.n, srcIndex, n, destIndex, length);
    }
  }

  /**
   * Insert an item into the array
   *
   * @param position :
   *          position to insert at, or -1 to append to end
   * @param obj :
   *          item to insert
   */
  public void insert(int position, Object obj) {
    if (position < 0) {
      position = length();
    }
    shift(position, 1);
    set(position, obj);
  }

  public void delete(int start) {
    delete(start,size() - start);
  }
  
  /**
   * Delete a range of elements; shift higher ones down to fill gap
   *
   * @param start :
   *          index of first element to delete
   * @param count :
   *          number of elements to delete
   */
  public void delete(int start, int count) {

    final boolean db = false;

    if (db) {
      System.out.println("delete start=" + start + " count=" + count
                         + " current length=" + length());
    }
    int shift = length() - (start + count);
    if (db) {
      System.out.println(" shift=" + shift);
    }
    Tools.ASSERT(shift >= 0, "Illegal arguments passed to delete");
    if (count > 0) {
      System.arraycopy(n, start + count, n, start, shift);

      // clear old items to null so we don't keep old references around
      for (int i = length() - count; i < length(); i++) {
        n[i] = null;
      }

      itemsUsed -= count;
    }
  }

  /**
   * Push a boolean onto the stack
   *
   * @param b
   *          boolean value to push
   */
  public void pushBoolean(boolean b) {
    add(new Boolean(b));
  }

  /**
   * Pop a boolean
   *
   * @return boolean value
   */
  public boolean popBoolean() {
    return ( (Boolean) pop()).booleanValue();
  }

  /**
   * Print contents of array
   */
  public void print() {

    System.out.println("DArray contents:");

    for (int i = 0; i < length(); i++) {
      Object o = getObj(i);
      System.out.print(Tools.f(i) + ": ");
      if (o == null) {
        System.out.println("NULL");
        continue;
      }
      System.out.println(o.toString());
    }
  }

  public String getString(int i) {
    return (String) get(i);
  }

  public double getDouble(int i) {
    return ( (Double) get(i)).doubleValue();
  }

  public void addDouble(double d) {
    add(new Double(d));
  }

  public int popInt() {
    return ( (Integer) pop()).intValue();
  }

  public double popDouble() {
    return ( (Double) pop()).doubleValue();
  }

  public void pushInt(int i) {
    push(new Integer(i));
  }

  public void push(Object o) {
    add(o);
  }

  public Object pop() {
    int i = itemsUsed - 1;
    Object obj = get(i);
    n[i] = null;
    itemsUsed = i;
    return obj;
  }

  /**
   * Find position of a particular integer value
   *
   * @param val :
   *          integer to find
   * @return location, or -1 if not found
   */
  public int findInt(int val) {
    return find(new Integer(val));

    // for (int i = 0; i < length(); i++) {
    // Object obj = getObj(i);
    // if (obj == null) {
    // continue;
    // }
    // if ( ( (Integer) obj).intValue() == val) {
    // return i;
    // }
    // }
    // return -1;
  }

  public int find(Object obj) {
    int loc = -1;
    for (int i = 0; i < length(); i++) {
      Object ob = getObj(i);
      if (ob == null) {
        continue;
      }
      if (ob.equals(obj)) {
        loc = i;
        break;
      }
    }
    return loc;
  }

  public int lastInt() {
    return ( (Integer) last()).intValue();
  }

  public boolean lastBoolean() {
    return ( (Boolean) last()).booleanValue();
  }

  public Object last() {
    return get(length() - 1);
  }

  /**
   * Determine if an item exists at a particular location
   *
   * @param index :
   *          index of item
   * @return true if it's a valid index and there's a non-null item stored there
   */
  public boolean exists(int index) {
    return index >= 0 && index < length() && getObj(index) != null;
  }

  /**
   * Free up an existing item and add its location to the recycle bin
   *
   * @param index :
   *          index of item
   */
  public void free(int index) {
    // if (Base.DEBUG) {
    // Tools.ASSERT(exists(index));
    // }
    n[index] = null;
    recycle(index);
  }

  /**
   * Free up an object, if it exists; add its locatio nto the recycle bin
   *
   * @param obj
   *          Object
   * @return true if item was found
   */
  public boolean free(Object obj) {
    boolean found = false;
    int loc = find(obj);
    if (loc >= 0) {
      free(loc);
      found = true;
    }
    return found;
  }

  /**
   * Add an index to the recycle bin for use by alloc()
   *
   * @param index :
   *          index of item to recycle
   */
  private void recycle(int index) {
    recycleBin = ensureIntCapacity(recycleBin, rcUsed, rcUsed + 1);
    recycleBin[rcUsed] = index;
    // if (debug) System.out.println("recycle slot "+index+" storing at position
    // "+rcUsed+" of bin");
    rcUsed++;
  }

  /**
   * Add an item to the end of the array.
   *
   * @param obj
   *          Object to add
   * @return int : index of added item
   */
  public int add(Object obj) {
    int pos = length();
    set(pos, obj);
    return pos;
  }

  public boolean remove(Object obj, boolean shiftToFillGap) {
    int index = find(obj);
    boolean found = index >= 0;
    if (found) {
      n[index] = null;
      if (shiftToFillGap) {
        delete(index, 1);
      }
    }
    return found;
  }

  public boolean remove(Object obj) {
    return remove(obj, true);
  }

  /**
   * Set an item at a particular position to a value.
   *
   * @param obj
   *          Object to set
   * @param position :
   *          index to replace; array is expanded if necessary to contain at
   *          least 1+position items
   */
  public void set(int position, Object obj) {
    Tools.ASSERT(obj != null);

    // add null items if necessary until there are at least
    // 1+position items in array
    n = ensureCapacity(n, itemsUsed, position + 1);

    n[position] = obj;
    if (position >= length()) {
      itemsUsed = position + 1;
    }
  }

  public void clearObj(int position ) {
    if (position < n.length) {
      n[position] = null;
    }
  }
  
  /**
   * Add an integer to the array; use recycled slots if they exist
   *
   * @param val :
   *          integer to add
   * @return index where it was stored
   */
  public int allocInt(int val) {
    return alloc(new Integer(val));
  }

  /**
   * Add an item to the array; use recycled slots if they exist
   *
   * @param obj
   *          Object to add
   * @return index where it was stored
   */
  public int alloc(Object obj) {
    Tools.ASSERT(obj != null);

    int slot = -1;

    if (rcUsed > 0) {
      rcUsed--;
      slot = recycleBin[rcUsed];
      // if (debug) System.out.println("alloc, slot="+slot+" from recycle bin
      // #"+rcUsed);
    }
    else {
      slot = length();
      // if (debug) System.out.println("alloc, slot="+slot+" from end of
      // array");
    }
    set(slot, obj);
    return slot;
  }

  public void setDouble(int position, double val) {
    set(position, new Double(val));
  }

  public int addInt(int val) {
    return add(new Integer(val));
  }

  // renamed to catch param order problems
  public void setInt(int position, int val) {
    set(position, new Integer(val));
  }

  public int getInt(int item) {
    return ( (Integer) (get(item))).intValue();
  }

  public boolean getBoolean(int item) {
    return ( (Boolean) get(item)).booleanValue();
  }

  public boolean isEmpty() {
    return length() == 0;
  }

  public void clear() {
    itemsUsed = 0;
    n = null;
    recycleBin = null;
    rcUsed = 0;
  }

  /**
   * Get an item from the array. It must exist.
   *
   * @param index :
   *          index of item
   * @return Object
   */
  public Object get(int index) {
    Object out = n[index];
    if (out == null) {
      Tools.ASSERT(out != null, "Attempt to get null item");
    }
    return out;
  }

  /**
   * Get an item from the array, which may or may not exist.
   *
   * @param index :
   *          index of item
   * @return Object; will be null if it doesn't exist.
   */
  public Object getObj(int index) {
    return n[index];
  }

  /**
   * Get the number of items in the array. This is 1 + the highest item index
   * added to the array via add() or alloc() since array was last cleared. In
   * other words, free() doesn't reduce this value.
   *
   * @return int
   */
  public int length() {
    return itemsUsed;
  }

  /**
   * Get number of items actually used; this is size() - rcUsed
   * @return
   */
  public int actualUsed() {
  return itemsUsed - rcUsed;
}

  public void sort(Comparator c) {
    if (!isEmpty()) {
      Arrays.sort(n, 0, length(), c);
    }
  }

  public Object[] toArray(Class itemType) {
    return toArray(itemType, false);
  }

  /**
   * Construct an array from a list.
   *
   * @param itemType :
   *          type of items in array; i.e., if this is Integer, returns an
   *          Integer[]
   * @return Object[]
   */
  public Object[] toArray(Class itemType, boolean skipDeleted) {
    DArray src = this;
    if (skipDeleted) {
      src = new DArray();
      for (int i = 0; i < length(); i++) {
        if (exists(i)) {
          src.add(get(i));
        }
      }
    }
    Object[] result = (Object[]) Array.newInstance(itemType, src.length());
    for (int i = 0; i < src.length(); i++) {
      result[i] = src.getObj(i);
    }
    return result;
  }

  /**
   * Construct an array from a list.
   *
   * @param skipDeleted :
   *          if true, array won't contain pointers for any items that have been
   *          deleted (and array index won't necessarily equal the item's id)
   * @return an array of Objects
   */
  public Object[] toArray(boolean skipDeleted) {
    return toArray(Object.class, skipDeleted);
    //
    // int size = length();
    // if (skipDeleted) {
    // for (int i = 0; i < length(); i++) {
    // if (!exists(i)) {
    // size--;
    // }
    // }
    // }
    //
    // Object[] a = new Object[size];
    // int j = 0;
    // for (int i = 0; i < length(); i++) {
    // if (skipDeleted && !exists(i)) {
    // continue;
    // }
    // a[j++] = getObj(i);
    // }
    // return a;
  }

  /**
   * Construct an array from the DArray, assuming each item is a String.
   *
   * @param skipDeleted :
   *          if true, array won't contain pointers for any items that have been
   *          deleted (and array index won't necessarily equal the item's id)
   * @return an array of Strings
   */
  public String[] toStringArray(boolean skipDeleted) {
    return (String[]) toArray(String.class, skipDeleted);
    //
    // Object[] oa = toArray(skipDeleted);
    // String[] a = new String[oa.length];
    // System.arraycopy(oa, 0, a, 0, oa.length);
    // return a;
  }

  /**
   * Construct an array from the DArray, assuming each item is a String.
   *
   * @return an array of Strings
   */
  public String[] toStringArray() {
    return toStringArray(true);
  }

  /**
   * Get descriptive string
   *
   * @return String
   */
  public String toString() {
    return toString(true);
  }

  public static int[] copy(int[] s, int start, int length) {
    int[] a = new int[length];
    System.arraycopy(s, start, a, 0, length);
    return a;
  }

  public static double[] copy(double[] s, int start, int length) {
    double[] a = new double[length];
    System.arraycopy(s, start, a, 0, length);
    return a;
  }

  public static int[] copy(int[] s) {
    return copy(s, 0, s.length);
  }

  public static double[] copy(double[] s) {
    return copy(s, 0, s.length);
  }

  public static void clearTo(int[] a, int val) {
    for (int i = 0; i < a.length; i++) {
      a[i] = val;
    }
  }

  public static void clearTo(double[] a, double val) {
    for (int i = 0; i < a.length; i++) {
      a[i] = val;
    }
  }

  // public boolean debug;

  public static String toString(int[] a) {
    return toString(a,false);
  }

  public static String toString(Object[] a ) {
    StringBuilder sb = new StringBuilder(" [");
    for (int i = 0; i < a.length; i++) {
      if (i != 0) {
        sb.append(' ');
      }
      Object obj = a[i];
      sb.append(Tools.tv(obj));
    }
    sb.append("] ");
    return sb.toString();
  }

  public static String toString(int[] a, boolean hexMode) {
    StringBuilder sb = new StringBuilder(" [");
    for (int i = 0; i < a.length; i++) {
      if (i != 0) {
        sb.append(' ');
      }
      if (hexMode)
        TextScanner.toHex(sb,a[i],8);
      else
      sb.append(a[i]);
    }
    sb.append("] ");
    return sb.toString();
  }
  public static String toString(byte[] a) {
    return toString(a,false);
  }
  public static String toString(byte[] a, boolean hexMode) {
    StringBuilder sb = new StringBuilder(" [");
    for (int i = 0; i < a.length; i++) {
      if (i != 0) {
        sb.append(' ');
      }
      if (hexMode)
        TextScanner.toHex(sb,a[i],2);
      else
      sb.append(a[i]);
    }
    sb.append("] ");
    return sb.toString();
  }


  public static String toString(double[] a) {
    StringBuilder sb = new StringBuilder(" [");
    // sb.append(" #="+a.length+" [");
    for (int i = 0; i < a.length; i++) {
      if (i != 0) {
        sb.append(' ');
      }
      sb.append(Tools.f(a[i]));
    }
    sb.append("] ");
    return sb.toString();
  }

  public String toString(boolean withLineFeeds) {
    StringBuilder sb = new StringBuilder();
    if (withLineFeeds) {
      sb.append("DArray length=" + length() + "\n");
    }
    else {
      sb.append("[");
    }
    // int max = withLineFeeds ? length() : 5;

    for (int i = 0; i < length(); i++) {
      if (length() == i) {
        break;
      }
      if (!withLineFeeds && sb.length() >= 60) {
        sb.append(" ...");
        sb.append("(" + (length() - 1 - i) + " more)");
        break;
      }
      if (!withLineFeeds) {
        sb.append(" ");
        sb.append(getObj(i));
      }
      else {
        sb.append(getObj(i));
        sb.append("\n");
      }
    }
    if (!withLineFeeds) {
      sb.append("] ");
    }
    return sb.toString();
  }

  /**
   * Constructor
   */
  public DArray() {
  }

  public DArray(int initialCapacity) {
    n = new Object[initialCapacity];
  }

public DArray(Object[] oa) {
  for (int i = 0; i < oa.length; i++) {
    Object obj = oa[i];
    if (obj != null)
    set(i, obj);
  }
}

  public DArray(int[] ia) {
    for (int i = 0; i < ia.length; i++) {
      addInt(ia[i]);
    }
  }

  public void permute(Random r) {
    for (int i = 0; i < length(); i++) {
      int j = r.nextInt(length());
      swap(i, j);
    }
  }

  public int[] toIntArray() {
    int[] a = new int[length()];
    for (int i = 0; i < length(); i++) {
      a[i] = getInt(i);
    }
    return a;
  }
  public byte[] toByteArray() {
    byte[] a = new byte[length()];
    for (int i = 0; i < length(); i++) {
      a[i] = ((Byte)get(i)).byteValue();
    }
    return a;
  }

  public DArray subset(int start, int length) {
    DArray out = new DArray();
    for (int i = start; i < start + length; i++) {
      out.add(get(i));
    }

    return out;
  }

  /**
   * Clone object. For good summary of what's entailed in Java cloning, see Core
   * Java, Vol. 1, p. 262.
   *
   * @return Object
   */
  public Object clone() {
    try {
      DArray d = (DArray)super.clone();

      // clone the arrays
      if (n != null) {
        d.n = (Object[]) d.n.clone();

      }
      if (recycleBin != null) {
        d.recycleBin = (int[]) d.recycleBin.clone();
      }
      return d;
    }
    catch (CloneNotSupportedException e) {
      throw new InternalError(e.toString());
    }
  }

  /**
   * Copy constructor
   *
   * @param src :
   *          DArray to make copy of
   */
  /*
   * public DArray(DArray src) { if (src.n != null) { n = new
   * Object[src.n.length]; System.arraycopy(src.n,0,n,0,src.length()); } if
   * (src.recycleBin != null) { recycleBin = new int[src.recycleBin.length];
   * System.arraycopy(src.recycleBin,0,recycleBin,0,src.rcUsed); } itemsUsed =
   * src.itemsUsed; rcUsed = src.rcUsed; }
   */
  // private static final boolean MAIN = false;
  public static int[] permutation(int length, Random r) {
    int[] a = new int[length];
    for (int i = 0; i < length; i++) {
      a[i] = i;
    }
    for (int i = 0; i < length; i++) {
      int j = r.nextInt(length);
      int temp = a[i];
      a[i] = a[j];
      a[j] = temp;
    }
    return a;
  }

  Object[] n() {
    return n;
  }

  int[] recycleBin() {
    return recycleBin;
  }

  
  int itemsUsed() {
    return itemsUsed;
  }

  int rcUsed() {
    return rcUsed;
  }

  private Object[] n;

  private int[] recycleBin;

  // number of items in use; see length()
  private int itemsUsed;

  // number of items used in recycle bin
  private int rcUsed;

  public static String toString(String[] str) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < str.length; i++) {
      String s = str[i];
      sb.append(s);
      if (!s.endsWith("\n"))
        sb.append('\n');
    }
    return sb.toString();
  }
}
