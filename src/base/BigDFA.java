package base;
import java.io.*;

public class BigDFA {

  /**
   * Construct a copy of an existing DFA.
   * @param src DFA
   */
  public BigDFA(BigDFA src) {
    locked = false;
    states = (DArray) src.states.clone();
    tokenNames = (DArray) src.tokenNames.clone();
    startState = src.startState;
  }

public DFA buildDFA() {
  DFA dfa = null;
  try {
  ByteArrayOutputStream bs = new ByteArrayOutputStream();
  this.write(bs);
   dfa = new DFA(new ByteArrayInputStream(bs.toByteArray()));
  } catch (IOException e) {
    throw new ScanException(e.toString());
  }
  return dfa;
}

  public BigDFAState getStateB(int s) {
    return (BigDFAState)states.get(s); //)getState(i);
  }

  /**
   * Get string describing object
   * @return String
   */
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("DFA, start=" + startState() + "\n");
    for (int i = 0; i < numStates(); i++) {
      sb.append(Tools.f(i, 4) + ": " + getState(i).toString() + "\n");
    }
    return sb.toString();
  }


  public BigDFA() {
  locked = false;
}

     /**
     * Clear transition on symbol for a state
     * @param state int
     * @param symbol char
     */
    public void clearTransition(int state, char symbol) {
      BigDFAState s = (BigDFAState)getState(state);
      s.clearTransition(symbol);

    }
    private static final String augToken = "#";

    /*	Write to a sink
     */
    public void write(OutputStream s) throws IOException {
      DataOutputStream out = new DataOutputStream(s);
      out.writeShort(VERSION);
      out.writeShort(nStates());
      out.writeShort(startState());
      out.writeShort(tokenNames.length());

      for (int i = 0; i < nStates(); i++) {
        BigDFAState st = (BigDFAState)getState(i);
        st.write(out);
      }
      for (int i = 0; i < tokenNames.length(); i++) {
        out.writeUTF(tokenName(i + Token.T_USER));
      }
    }



    /**
   * Read DFA.
   * @param r Reader
   * @throws IOException
   *
   * Format is:
   *
   * <line> ::=         <trans>
   *                  | <final>
   *                  | '#' <augment>
   *
   *
   * <trans> ::=        <initial:state> <dest:state> <symbol>
   * <final> ::=        <state>
   * <augment> ::=      's' <start:state>
   *                  | 't' <tokenname:string>
   *
   */
  public void read(Reader r) throws IOException {
    reset();

    TextScanner scan = new TextScanner(r, null, null, -1);

    // keep track of what will become the start state
    int startSt = -1;

    while (true) {
      String lineStr = scan.readLine();
      if (lineStr == null) {
        break;
      }

      // is this line blank?
      if (lineStr.length() == 0) {
        continue;
      }

      Reader r2 = new StringReader(lineStr);
      TextScanner s2 = new TextScanner(r2);

      String s = s2.readWord();
      if (s == null) {
        continue;
      }

      if (s.equals(augToken)) {

        s = s2.readWord(true);
        if (s.equals("s")) {
          startSt = TextScanner.parseInt(s2.readWord(true));
        }
        else if (s.equals("t")) {
          tokenNames.add(s2.readWord(true));
        }
      }
      else {
        int s0 = Integer.parseInt(s);

        // if no more data on this line, it's a <final state>
        String token = s2.readWord();
        if (token == null) {
          makeStateFinal(s0);
        }
        else {

          // read the next state & transition symbol

          int s1 = Integer.parseInt(token);
          String symbol = parseSymbol(s2.readWord(true));

          // check validity of input...
          if (symbol.length() != 1) {
            throw new IOException("Bad format in DFA file");
          }

          // add a transition symbol between these states; if a symbol
          // already exists, it will throw an exception (it's supposed
          // to be a DFA, not an NFA)

          // if the states don't exist, they will be created.

          addTransition(s0, s1, symbol.charAt(0));
        }

        // replace startState if necessary
        if (startSt < 0 || s0 == 0) {
          startSt = s0;
        }
      }
    }
    if (startSt >= 0) {
      setStartState(startSt);
    }

    storeTerminalFlags();
  }

  protected BigDFAState constructState() {
    return new BigDFAState();
  }


  public static String parseSymbol(String s) {
    String sOut = s;
    if (s.length() >= 2
        && s.charAt(0) == '#') {
      int k = Integer.parseInt(s.substring(1));
      sOut = "" + (char) k;
    }
    return sOut;
  }

  public void write(Writer s) {
    PrintWriter w = new PrintWriter(s);
    for (int i = 0; i < numStates(); i++) {
      BigDFAState st = (BigDFAState)getState(i);
      st.write(w, i);
    }
    w.println("# s " + startState());
    for (int i = 0; i < tokenNames.length(); i++) {
      w.println("# t " + tokenNames.getString(i));
    }
    w.flush();
  }

  // names of tokens (T_xxx), or empty if none included
   protected DArray tokenNames = new DArray();

   protected BigDFAState getState(int n) {
      return (BigDFAState) states.get(n);
   }

   public int nStates() {
     return states.length();
   }

   // dynamic array of states
   protected DArray states = new DArray();

   // start state of the DFA
   protected int startState = -1;

   protected boolean locked;

















   /**
    * Construct a DFA.
    * @param s : InputStream, a binary xxx.dfa file
    */
   public BigDFA(InputStream s) {
     try {
       read(s);
     }
     catch (IOException e) {
       ScanException.toss(e);
     }
     locked = true;
   }

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

   protected static final int
//      VERSION93 = 0x9993,
       VERSION = 0x9994 // magic number for file version
       ;

   private boolean tracing;

   /**
    * Turn debug tracing on/off
    * @param t boolean
    */
   public void setTrace(boolean t) {
     tracing = t;
   }

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
     if (tracing) {
       System.out.println("DFA.recognize " + t);
     }
     try {
       StringBuilder sb = new StringBuilder();

       // keep track of the length of the longest token found, and
       // the final state it's associated with
       int maxLengthFound = 0;
       int bestFinalState = -1;
       int bestFinalCode = -1;

       if (tracing) {
         System.out.println(" startState = " + startState());
       }

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
         if (tracing) {
           System.out.println(" s=" + state + " c=" + c + " d=" + newState);
         }
         // if there's no transition on this symbol from the state, no match.
         if (newState < 0) {
           break;
         }

         state = newState; //setState(newState);

         // if state has token code, we've reached a final state.
//        DFAState s = getState(state);
         int token = getState(state).getTerminalCode();
         if (tracing) {
           System.out.println(" termCode=" + token);
         }

         if (token >= 0) {
           maxLengthFound = sb.length();
           bestFinalCode = token;
           bestFinalState = newState;
           if (tracing) {
             System.out.println("  found token length " + maxLengthFound);
           }
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
       }
       else if (sb.length() == 0) {
         t.setText("");
         t.setId(Token.T_EOF);
       }
       else {
         t.setText(sb.substring(0, 1));
         t.setId(sb.charAt(0));
       }

       if (tracing) {
         System.out.println(" returning token " + t);
       }
     }
     catch (IOException e) {
       throw new ScanException(e.toString());
     }
   }

//  static void locked() {
//   throw new DFAError("Illegal method call");
// }
//
//  public static final DFAError lock = new DFAError("Illegal method call");

   /**
    * Add a transition from one state to another, with optional token name.
    * @param stateI int : initial state
    * @param stateD int : destination state
    * @param symbol char : symbol to transit on
    */
   public void addTransition(int stateI, int stateD, char symbol
       ) {
//     if (locked) {
//       locked();
//     }

     addState(stateI);
     addState(stateD);

     // ensure there's no transition on this symbol already
     int existDest = getTransitionState(stateI, symbol);
     if (existDest >= 0 && existDest != stateD) {
       throw new DFAError("State already has transition symbol");
     }
//    DFAState s = getState(stateI);
     getState(stateI).addTransition(symbol, stateD);
   }

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
       if (stateI >= numStates()) {
         break;
       }

       stateD = getState(stateI).getTransitionState(symbol);
     }
     while (false);
     return stateD;
   }

   private void verifyValidState(int s) {
     if (s < 0 || s > Character.MAX_VALUE) {
       throw new DFAError("Invalid state: " + s);
     }
   }

   /**
    * Set names of tokens
    * @param a : DArray containing Strings
    */
   public void setTokenNames(DArray a) {
//     if (locked) {
//       locked();
//     }
     tokenNames.clear();
     tokenNames.insert(a, 0, a.length(), 0);
   }

   /**
    * Determine the start state
    * @return int
    */
   public int startState() {
     return startState;
   }

   /**
    * Determine if a state is a final state
    * @param state int
    * @return boolean
    */
   public boolean isFinalState(int state) {
     return getState(state).finalFlag();
   }

   /**
    * Find all the states that contain a transition to a terminal code,
    * and store that code with the state.
    */
   public void storeTerminalFlags() {
//     if (locked) {
//       locked();
//     }
     for (int i = 0; i < numStates(); i++) {
       getState(i).setTerminalCode();
//      s.setTerminalCode();
     }
   }

//   protected BigDFA() {}

   /**
    * Clear DFA to just-constructed state
    */
   public void reset() {
//     if (locked) {
//       locked();
//     }
     startState = -1;
     states.clear();
     tokenNames.clear();
   }


   /**
    * Read DFA from a source
    * @param s InputStream
    * @throws IOException
    */
   public void read(InputStream s) throws IOException {
     reset();

     DataInputStream in = new DataInputStream(s);

     int v = in.readUnsignedShort();
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
   public void setStartState(int s) {
//     if (locked) {
//       locked();
//     }
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
   public void addState(int s) {
//     if (locked) {
//       locked();
//     }
     verifyValidState(s);

     // add new states if necessary between end of state
     // array and this state

     while (s >= numStates()) {
       states.add(constructState());
     }
   }

//   protected DFAState constructState() {
//     return new DFAState();
//   }

   /**
    * Make a particular state a final state.  Creates it if it doesn't exist.
    * @param s : state number
    */
   public void makeStateFinal(int s) {
     addState(s);
     getState(s).setFinalFlag();
//    state.setFinalFlag();
   }

   /*	Determine # states in DFA.
                   < # states
    */
   public int numStates() {
     return states.length();
   }

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
     }
     else {
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

   public int nUserTokens() {
     return tokenNames.length();
   }

   public static String encodeSymbol(char c) {
     String s = null;

     if (c <= ' '
         || c > Token.T_ASCII_END
         || c == '#'
         ) {
       s = "#" + (int) c;
     }
     else {
       s = Character.toString(c);
     }
     return s;
   }

}
