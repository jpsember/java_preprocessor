package base;

import java.io.*;
import java.util.*;

/**
 * Deterministic Finite State Automaton.
 *
 * Objects of this type can only be constructed from .dfa files, and
 * are read-only.  To construct and manipulate DFAs, use the BigDFA class
 * instead.  This allows programs to use these 'small' dfas for scanning,
 * and leave the extra functionality out when not needed.
 *
 */
public class DFA {

  /**
   * Constructor
   * @param path : path to read DFA from
   */
  public DFA(String path) {
    try {
      InputStream s = new FileInputStream(path); // Streams.inputStream(path);
      read(s);
      s.close();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Construct a DFA.
   * @param s : InputStream, a binary xxx.dfa file
   */
  public DFA(InputStream s) {
    try {
      read(s);
    } catch (IOException e) {
      ScanException.toss(e);
    }
    //    locked = true;
  }

  /**
   * Read DFA from file.
   * Throws IOExceptions as ScanExceptions.
   * @param owner : owner of file, for resource loader; can be null
   * @param path : name of file, if owner defined; else, filesystem path
   */
  public DFA(Class owner, String path) {

    try {
      InputStream s = Streams.openResource(owner, path);
      read(s);
      s.close();
    } catch (IOException e) {
      ScanException.toss(e);
    }

  }

  /**
   * Read a DFA, if it is not already read, using default DFA map
   * @param owner : owner of DFA, for using class loader to locate it
   * @param path : path of DFA
   * @return DFA
   */
  public static DFA readFromSet(Object owner, String path) {
    return readFromSet(null,owner,path);
  }
  
  /**
   * Read a DFA, if it is not already read
   * @param map : map to store DFA's within
   * @param owner : owner of DFA, for using class loader to locate it
   * @param path : path of DFA
   * @return DFA
   */
  public static DFA readFromSet(Map map, Object owner, String path) {
    Class c = Streams.classParam(owner);

    String key = path;
    if (c != null) {
      key = c.getName() + key;
    }
    
    if (map == null) {
      if (defaultDFAMap == null) {
        defaultDFAMap = new HashMap();
      }
      map = defaultDFAMap;
    }
    
    DFA dfa = (DFA) map.get(key);
    if (dfa == null) {
      dfa = new DFA(c, path);
      map.put(key, dfa);
    }
    return dfa;
  }
  private static Map defaultDFAMap;

  //  /**
  //   * Construct a DFA by reading from a table of integers
  //   * that has been generated as a JavaCodeOutputStream.
  //   *
  //   * @param data int[]
  //   */
  //  public DFA(int[] data) {
  //    try {
  //      read(new JavaCodeInputStream(data));
  //    }
  //    catch (IOException e) {
  //      throw new DFAError(e.toString());
  //    }
  //    locked = true;
  //  }

  private static final int
  //      VERSION93 = 0x9993,
  VERSION = 0x9994 // magic number for file version
  ;

  //  private boolean tracing;

  //  /**
  //   * Turn debug tracing on/off
  //   * @param t boolean
  //   */
  //  private void setTrace(boolean t) {
  //    tracing = t;
  //  }
  //

 
  /**
   * Attempt to recognize a token from a string.  If recognized,
   * the current state of the DFA is left at the appropriate final
   * state; if not, the state will be -1.
   * Does not affect position of reader.
   *
   * @param r : reader
   * @param t : token containing context; id and text fields will be modified
   *   if a token is recognized; otherwise, returns T_UNKNOWN or T_EOF with
   *   empty text
   */
  public void recognize(PushbackReader r, Token t) {
    // we must allocate StringBuilder here for thread safety.
    StringBuilder sb  = new StringBuilder();
    
    //    if (tracing) {
    //      System.out.println("DFA.recognize " + t);
    //    }
    try {
      sb.setLength(0); //StringBuilder sb = new StringBuilder();

      // keep track of the length of the longest token found, and
      // the final state it's associated with
      int maxLengthFound = 0;
      int bestFinalState = -1;
      int bestFinalCode = -1;

      //      if (tracing) {
      //        System.out.println(" startState = " + startState());
      //      }

      int state = startState();

      while (true) {
        // read character from the reader; exit if eof
        int ch = r.read();
        if (ch < 0) {
          break;
        }

        // add character to buffer for later pushing back
        char c = (char) ch;
        sb.append(c);

        // if this is not a legal character for a token, no match.
        if (c > Token.T_ASCII_END) {
          break;
        }

        int newState = getTransitionState(state, c);
        //        if (tracing) {
        //          System.out.println(" s=" + state + " c=" + c + " d=" + newState);
        //        }
        // if there's no transition on this symbol from the state, no match.
        if (newState < 0) {
          break;
        }

        state = newState; //setState(newState);

        // if state has token code, we've reached a final state.
        //        DFAState s = getState(state);
        int token = getState(state).getTerminalCode();
        //        if (tracing) {
        //          System.out.println(" termCode=" + token);
        //        }

        if (token >= 0) {
          maxLengthFound = sb.length();
          bestFinalCode = token;
          bestFinalState = newState;
          //          if (tracing) {
          //            System.out.println("  found token length " + maxLengthFound);
          //          }
        }
      }
      state = bestFinalState; //setState(bestFinalState);

      // push back all characters we read, since location tracking
      // has probably been disabled.
      for (int i = sb.length() - 1; i >= 0; i--) {
        r.unread(sb.charAt(i));
      }

      if (bestFinalCode >= 0) {
        t.setId(bestFinalCode);
        t.setText(sb.substring(0, maxLengthFound));
      } else if (sb.length() == 0) {
        t.setText("");
        t.setId(Token.T_EOF);
      } else {
        t.setText(sb.substring(0, 1));
        t.setId(sb.charAt(0));
      }

      //      if (tracing) {
      //        System.out.println(" returning token " + t);
      //      }
    } catch (IOException e) {
      throw new ScanException(e.toString());
    }
  }

  // static void locked() {
  //  throw new DFAError("Illegal method call");
  //}
  //
  //  public static final DFAError lock = new DFAError("Illegal method call");

  //  /**
  //   * Add a transition from one state to another, with optional token name.
  //   * @param stateI int : initial state
  //   * @param stateD int : destination state
  //   * @param symbol char : symbol to transit on
  //   */
  //  private void addTransition(int stateI, int stateD, char symbol
  //      ) {
  //    if (locked) {
  //      locked();
  //    }
  //
  //    addState(stateI);
  //    addState(stateD);
  //
  //    // ensure there's no transition on this symbol already
  //    int existDest = getTransitionState(stateI, symbol);
  //    if (existDest >= 0 && existDest != stateD) {
  //      throw new DFAError("State already has transition symbol");
  //    }
  ////    DFAState s = getState(stateI);
  //    getState(stateI).addTransition(symbol, stateD);
  //  }

  /**
   * Determine what state, if any, is reached by transitioning from a
   * state on a symbol
   * @param stateI : initial state
   * @param symbol : symbol to transition on
   * @return new state, or -1 if no transition exists on this symbol
   */
  private int getTransitionState(int stateI, char symbol) {
    verifyValidState(stateI);
    int stateD = -1;
    do {
      if (stateI >= nStates()) {
        break;
      }

      stateD = getState(stateI).getTransitionState(symbol);
    } while (false);
    return stateD;
  }

  private void verifyValidState(int s) {
    if (s < 0 || s > Character.MAX_VALUE) {
      throw new DFAError("Invalid state: " + s);
    }
  }

  //  /**
  //   * Set names of tokens
  //   * @param a : DArray containing Strings
  //   */
  //  private void setTokenNames(DArray a) {
  //    if (locked) {
  //      locked();
  //    }
  //    tokenNames.clear();
  //    tokenNames.insert(a, 0, a.length(), 0);
  //  }

  /**
   * Determine the start state
   * @return int
   */
  private int startState() {
    return startState;
  }

  /**
   * Determine if a state is a final state
   * @param state int
   * @return boolean
   */
  private boolean isFinalState(int state) {
    return getState(state).finalFlag();
  }

  /**
   * Find all the states that contain a transition to a terminal code,
   * and store that code with the state.
   */
  private void storeTerminalFlags() {
    //    if (locked) {
    //      locked();
    //    }
    for (int i = 0; i < nStates(); i++) {
      getState(i).setTerminalCode();
      //      s.setTerminalCode();
    }
  }

//  private DFA() {
//  }

  //  /**
  //   * Clear DFA to just-constructed state
  //   */
  //  public void reset() {
  //    if (locked) {
  //      locked();
  //    }
  //    startState = -1;
  //    states.clear();
  //    tokenNames.clear();
  //  }

  /**
   * Read DFA from a source
   * @param s InputStream
   * @throws IOException
   */
  private void read(InputStream s) throws IOException {
    //    reset();

    DataInputStream in = new DataInputStream(s);

    int v = in.readUnsignedShort();
    //    boolean older = false;
    switch (v) {
    //      case VERSION93:
    //        older = true;
    //        break;
    case VERSION:
      break;
    default:
      throw new DFAError("Bad version in DFA");
    }
    int sCnt = in.readUnsignedShort();
    int sState = in.readUnsignedShort();
    int tokenNameCount = in.readUnsignedShort();
    for (int i = 0; i < sCnt; i++) {
      addState(i);
      getState(i).read(in, v);
    }
    setStartState(sState);

    for (int i = 0; i < tokenNameCount; i++) {
      String n = in.readUTF();
      tokenNames.add(n);
    }
    storeTerminalFlags();
  }

  /**
   * Set start state
   * @param s int
   */
  private void setStartState(int s) {
    //    if (locked) {
    //      locked();
    //    }
    verifyValidState(s);

    // If this state is a final state, that's a problem!
    // We don't want to recognize zero-length tokens.
    if (isFinalState(s)) {
      throw new DFAError("Start state cannot be a final state");
    }

    startState = s;
  }

  /**
   * Add a state if it doesn't already exist
   * @param s int
   */
  private void addState(int s) {
    //    if (locked) {
    //      locked();
    //    }
    verifyValidState(s);

    // add new states if necessary between end of state
    // array and this state

    while (s >= nStates()) {
      states.add(constructState());
    }
  }

  private DFAState constructState() {
    return new DFAState();
  }

  //  /**
  //   * Make a particular state a final state.  Creates it if it doesn't exist.
  //   * @param s : state number
  //   */
  //  private void makeStateFinal(int s) {
  //    addState(s);
  //    getState(s).setFinalFlag();
  ////    state.setFinalFlag();
  //  }

  //  /*	Determine # states in DFA.
  //                  < # states
  //   */
  //  public int numStates() {
  //    return states.length();
  //  }
  //
  public int tokenId(String s) {
    int out = -1;
    for (int i = 0; i < tokenNames.length(); i++) {
      if (s.equals(tokenNames.getString(i))) {
        out = i + Token.T_USER;
        break;
      }
    }
    return out;
  }

  /**
   * Get the name of a token from the DFA
   * @param type : id of token
   * @return name of token, or T_UNKNOWN if it is of unknown type
   */
  public String tokenName(int type) {
    String n = null;

    int t = type - Token.T_USER;
    if (t >= 0 && t < tokenNames.length()) {
      n = (String) tokenNames.getObj(t);
    } else {
      n = defaultTokenName(type);
    }

    return n;
  }

  public static String defaultTokenName(int type) {
    String n = null;
    switch (type) {
    case Token.T_EOF:
      n = "T_EOF";
      break;
    default:
      n = "T_UNKNOWN";
      break;
    }
    return n;
  }

  //  public int nUserTokens() {
  //    return tokenNames.length();
  //  }

  //  private static String encodeSymbol(char c) {
  //    String s = null;
  //
  //    if (c <= ' '
  //        || c > Token.T_ASCII_END
  //        || c == '#'
  //        ) {
  //      s = "#" + (int) c;
  //    }
  //    else {
  //      s = Character.toString(c);
  //    }
  //    return s;
  //  }
  //
  // names of tokens (T_xxx), or empty if none included
  private DArray tokenNames = new DArray();

  private DFAState getState(int n) {
    return (DFAState) states.get(n);
  }

  private int nStates() {
    return states.length();
  }

  // dynamic array of states
  private DArray states = new DArray();

  // start state of the DFA
  private int startState = -1;

  //  private boolean locked;

  private static class DFAState
  //    implements Cloneable
  {

    public static final int MAX_TERMINAL_CODE = Token.T_USER_END,
        F_TERMINALCODE = MAX_TERMINAL_CODE, F_FINAL = 1 << 15;

//    /**
//     * Make state a final state.
//     */
//    public void setFinalFlag() {
//      setFlag(F_FINAL);
//    }

//    public void setFinalFlag(int code) {
//      int curr = finalCode();
//      if (curr < 0 || curr > code) {
//        setTerminalCode(code);
//        setFinalFlag();
//      }
//    }

    public void setTerminalCode(int n) {
      if (n > MAX_TERMINAL_CODE) {
        throw new DFAError("Terminal code too large");
      }

      flags = (char) ((flags & ~F_TERMINALCODE) | n);
    }

//    /*	Determine the final state code associated with this state
//     < code, or -1 if none
//     */
//    public int finalCode() {
//      return (flags & F_TERMINALCODE) - 1;
//    }

    public void setTerminalCode() {
      setTerminalCode(1 + findTokenID());

    }

    public int getTerminalCode() {
      return (flags & F_TERMINALCODE) - 1;
    }

    public boolean finalFlag() {
      return flag(F_FINAL);
    }

//    public void setFlag(int f) {
//      flags |= f;
//    }
//
    public boolean flag(int f) {
      return (flags & f) != 0;
    }

    /**
     * Find insertion position of transition
     * @param symbol : symbol for transition
     * @return position where symbol is to be inserted / replace existing one
     */
    private int findSymbol(int symbol) {
      int min = 0, max = nTrans() - 1;

      while (true) {
        if (min > max) {
          break;
        }
        int test = (min + max) >> 1;
        int tSym = transSymbol(test);
        if (tSym == symbol) {
          min = test;
          break;
        }
        if (tSym > symbol) {
          max = test - 1;
        } else {
          min = test + 1;
        }
      }
      return min;
    }

    private char transSymbol(int index) {
      return trans.charAt(index << 1);
    }

    private int nTrans() {
      int out = 0;
      if (trans != null) {
        out = trans.length() >> 1;
      }
      return out;
    }

    //  /**
    //   * Add transition.  Any existing transition on the symbol is replaced.
    //   * @param symbol : symbol to transit on
    //   * @param stateD : destination state
    //   */
    //  public void addTransition(char symbol, int stateD) {
    //    DFA.locked();
    //
    ////    if (stateD < 0 || stateD > Character.MAX_VALUE) {
    ////      throw new DFAError("Cannot store transition (symbol=" + symbol +
    ////                         ", state=" + stateD + ")");
    ////    }
    ////
    ////    int insPos = findSymbol(symbol);
    ////    int j = insPos << 1;
    ////    if (symbolMatches(symbol, insPos)) {
    ////      trans.setCharAt(j + 1, (char) stateD);
    ////    }
    ////    else {
    ////      if (nTrans() == MAX_TRANSITIONS) {
    ////        throw new DFAError("Too many transitions");
    ////      }
    ////      if (trans == null) {
    ////        trans = new StringBuilder(2 * 8);
    ////      }
    ////      trans.insert(j, "" + symbol + (char) (stateD));
    ////    }
    //  }

    /**
     * Get state to move to on a symbol
     * @param symbol
     * @return destination state, or -1 if no transition exists
     */
    public int getTransitionState(char symbol) {
      int out = -1;
      int insPos = findSymbol(symbol);
      if (symbolMatches(symbol, insPos)) {
        out = trans.charAt((insPos << 1) + 1);
      }
      return out;
    }

    public DFAState() {
      //      reset();
    }

    private boolean symbolMatches(char symbol, int index) {
      return (index < nTrans()) && transSymbol(index) == symbol;
    }

    /*	Read state from file
     */
    void read(DataInputStream r, int version) throws IOException {
      //    reset();
      String s = r.readUTF();
      flags = s.charAt(0);
      char sCount = s.charAt(1);
      if (sCount > 0) {
        trans = new StringBuilder(s.substring(2));
      }

      //    if (version == DFA.VERSION93) {
      //      // Translate all transitions on symbols >= 128 to >= 256.
      //      for (int i = 0; i < nTrans(); i++) {
      //        char k = trans.charAt(i * 2);
      //        if (k >= Token.T_USER_OLD)
      //          trans.setCharAt(i*2, (char)(k + (Token.T_USER - Token.T_USER_OLD)));
      //      }
      //    }
    }

//    /*	Read state from file
//     */
//    public void read(DataInputStream r) throws IOException {
//      read(r, DFA.VERSION);
//    }

    /**
     * Determine which token id, if any, is associated with this state
     * @return id of token, or -1
     */
    private int findTokenID() {
      int out = -1;
      int insPos = findSymbol(Token.T_USER);
      if (insPos < nTrans()) {
        out = transSymbol(insPos);
      }
      return out;
    }

    // F_xxx
    private char flags;

    // State transitions.
    // Each transition is stored as a <symbol, state> pair.
    private StringBuilder trans = new StringBuilder();

    // ---------- 'big' DFA functions only

    //  /**
    //  * Remove any existing transition from this state on a symbol
    //  * @param symbol char
    //  */
    // public void clearTransition(char symbol) {
    //   int insPos = findSymbol(symbol);
    //   if (symbolMatches(symbol, insPos)) {
    //     int k = insPos << 1;
    //     trans.delete(k, k + 2);
    //   }
    // }
    //
    // private void reset() {
    //    flags = 0;
    //    trans = new StringBuilder();
    //  }

    //  public void write(PrintWriter w, int stateNumber) {
    //    for (int j = 0; j < trans.length(); j += 2) {
    //      char sym = trans.charAt(j);
    //      int s2 = trans.charAt(j + 1);
    //      w.print(stateNumber);
    //      w.print(' ');
    //      w.print(s2);
    //      w.print(' ');
    //      w.print(DFA.encodeSymbol(sym));
    //      w.println();
    //    }
    //    if (finalFlag()) {
    //      w.print(stateNumber);
    //      w.println();
    //    }
    //  }

    // /**
    //   * Write state to stream
    //   */
    //  public void write(DataOutputStream w) throws IOException {
    //    StringBuilder sb = new StringBuilder();
    ////      sb.append("The time has come, the walrus said, to speak of many things;\nof shoes, and ships, and sealing wax");
    ////if (false)
    ////  {
    //    sb.append(flags);
    //    sb.append( (char) nTrans());
    //    sb.append(trans.toString());
    ////  }
    //    w.writeUTF(sb.toString());
    //
    //  }

    //  public Object clone() {
    //   try {
    //     DFAState d = (DFAState)super.clone();
    //     if (trans != null) {
    //       d.trans = new StringBuilder(d.trans.toString());
    //     }
    //     return d;
    //   }
    //   catch (CloneNotSupportedException e) {
    //     throw new InternalError(e.toString());
    //   }
    // }
    // /**
    //   * Get string describing object
    //   * @return String
    //   */
    //  public String UNUSED_toString() {
    //    StringBuilder sb = new StringBuilder();
    //    sb.append("S(");
    //    sb.append("t=" + Scanner.toHex(flags & F_TERMINALCODE, 3) + "|"
    //              + optFlag("F", finalFlag())
    //        );
    //    sb.append(" | ");
    //    String s = getTransitioningSymbols();
    //    for (int i = 0; i < s.length(); i++) {
    //      char c = s.charAt(i);
    //      sb.append("'" + Scanner.convert(c) + "'-->" + getTransitionState(c) +
    //                " ");
    //    }
    //    return sb.toString();
    //  }
    //
    //
    //  public void UNUSED_clearFlags(int f) {
    //    flags = (char) (flags & ~f);
    //  }

  }

}
