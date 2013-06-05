package base;

import java.util.*;

/**
 * Collection for a map whose items are also stored in an array
 * for quick access.
 * 
 */
public class ArrayMap {

  /**
   * Add an item to the map; replace existing if found
   * @param key
   * @param value
   */
  public Object add(Object key, Object value) {
    Object prevValue = null;
    Integer pos = (Integer) keyPositionMap.get(key);
    if (pos == null) {
     int index = valArray.add(value);
     keyArray.add(key);
     keyPositionMap.put(key, new Integer(index));
    } else {
      prevValue = valArray.get(pos.intValue());
      valArray.set(pos.intValue(),value);
    }
    return prevValue;
  }

  public DArray getKeys() {return keyArray;}
  
  /**
   * Determine if map is empty
   * @return
   */
  public boolean isEmpty() {
    return keyArray.isEmpty();
  }

  /**
   * Get # items in map
   * @return
   */
  public int size() {
    return keyArray.size();
  }

  /**
   * Get key from map
   * @param index : index within the array (0...size()-1)
   * @return
   */
  public Object getKey(int index) {
    return (String) keyArray.get(index);
  }

  /**
   * Get value associated with key
   * @param key
   * @return
   */
  public Object getValue(Object key) {
    Object ret = null;
    Integer pos = (Integer) keyPositionMap.get(key);
    if (pos != null) {
      ret = valArray.get(pos.intValue());
    }
    
    return ret;
  }

  /**
   * Get value associated with index within array
   * @param index
   * @return
   */
  public Object getValue(int index) {
    return  valArray.get(index) ;
  }

  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("ArrayMap[\n");
    for (int i = 0; i < keyArray.size(); i++) {
      sb.append(' ');
      Object key = getKey(i);
      sb.append(key.toString());
      sb.append(" --> ");
      sb.append(valArray.get(i));
      sb.append('\n');
    }
    sb.append("]\n");

    return sb.toString();
  }

  /**
   * Construct a map from [int]->[Object]
   * @param iVals : int array
   * @param oVals : array of Objects
   * @return Map
   */
  public static Map intKeyMap(int[] iVals, Object[] oVals) {
    Map map = new HashMap(iVals.length);
    for (int i = iVals.length - 1; i >= 0; i--) {
      Object prev = map.put(new Integer(iVals[i]), oVals[i]);
      if (prev != null)
        Tools.warn("intKeyMap, duplicate entry for key: " + iVals[i]);
    }
    return map;
  }

  public static Map intKeyMap(int[] iVals, String labels) {
    StringTokenizer tk = new StringTokenizer(labels);
    DArray lbl = new DArray();
    while (tk.hasMoreTokens()) {
      String l = tk.nextToken();
      l = Tools.f(l, 16);
      lbl.add(l);
    }
    if (lbl.size() != iVals.length)
      throw new IllegalArgumentException("unexpected # of labels");
    return intKeyMap(iVals, lbl.toStringArray());
  }

  private Map keyPositionMap = new HashMap();

  private DArray valArray = new DArray();
private DArray keyArray = new DArray();

  public static String readString(Map map, int type) {
    
    String s = (String) map.get(new Integer(type));
    if (s == null) 
      s = Tools.f("<unknown: "+type+">",16);
    return s;
  }
}
