package base;
import java.io.*;

public class BigDFAState  implements Cloneable {
    /**
    * Remove any existing transition from this state on a symbol
    * @param symbol char
    */
   public void clearTransition(char symbol) {
     int insPos = findSymbol(symbol);
     if (symbolMatches(symbol, insPos)) {
       int k = insPos << 1;
       trans.delete(k, k + 2);
     }
   }

     public void write(PrintWriter w, int stateNumber) {
      for (int j = 0; j < trans.length(); j += 2) {
        char sym = trans.charAt(j);
        int s2 = trans.charAt(j + 1);
        w.print(stateNumber);
        w.print(' ');
        w.print(s2);
        w.print(' ');
        w.print(encodeSymbol(sym));
        w.println();
      }
      if (finalFlag()) {
        w.print(stateNumber);
        w.println();
      }
    }

   /**
     * Write state to stream
     */
    public void write(DataOutputStream w) throws IOException {
      StringBuilder sb = new StringBuilder();
//      sb.append("The time has come, the walrus said, to speak of many things;\nof shoes, and ships, and sealing wax");
//if (false)
//  {
      sb.append(flags);
      sb.append( (char) nTrans());
      sb.append(trans.toString());
//  }
      w.writeUTF(sb.toString());

    }

    public Object clone() {
     try {
       BigDFAState d = (BigDFAState)super.clone();
       if (trans != null) {
         d.trans = new StringBuilder(d.trans.toString());
       }
       return d;
     }
     catch (CloneNotSupportedException e) {
       throw new InternalError(e.toString());
     }
   }

   private static String optFlag(String s, boolean f) {
  return f ? s : Tools.sp(s.length());
}
/**
 * Get a list of the symbols that this state has transitions on
 * @return array of ints, each a symbol
 */
public String getTransitioningSymbols() {
  int s = nTrans();
  StringBuilder sb = new StringBuilder(s);
  for (int i = 0; i < s; i++) {
    sb.append(trans.charAt(i << 1));
  }
  return sb.toString();
}

private static String encodeSymbol(char c) {
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

/**
 * Add transition.  Any existing transition on the symbol is replaced.
 * @param symbol : symbol to transit on
 * @param stateD : destination state
 */
public void addTransition(char symbol, int stateD) {

  if (stateD < 0 || stateD > Character.MAX_VALUE) {
    throw new DFAError("Cannot store transition (symbol=" + symbol +
                       ", state=" + stateD + ")");
  }

  int insPos = findSymbol(symbol);
  int j = insPos << 1;
  if (symbolMatches(symbol, insPos)) {
    trans.setCharAt(j + 1, (char) stateD);
  }
  else {
    if (nTrans() == MAX_TRANSITIONS) {
      throw new DFAError("Too many transitions");
    }
    if (trans == null) {
      trans = new StringBuilder(2 * 8);
    }
    trans.insert(j, "" + symbol + (char) (stateD));
  }
}



   /**
     * Get string describing object
     * @return String
     */
    public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append("S(");
      sb.append("t=" + TextScanner.toHex(flags & F_TERMINALCODE, 3) + "|"
                + optFlag("F", finalFlag())
          );
      sb.append(" | ");
      String s = getTransitioningSymbols();
      for (int i = 0; i < s.length(); i++) {
        char c = s.charAt(i);
        sb.append("'" + TextScanner.convert(c) + "'-->" + getTransitionState(c) +
                  " ");
      }
      return sb.toString();
    }


    public void UNUSED_clearFlags(int f) {
      flags = (char) (flags & ~f);
    }






























    protected static final int
        MAX_TRANSITIONS = Character.MAX_VALUE
        ;

    public static final int
        MAX_TERMINAL_CODE = Token.T_USER_END
        , F_TERMINALCODE = MAX_TERMINAL_CODE
        , F_FINAL = 1 << 15
        ;

    /**
     * Make state a final state.
     */
    public void setFinalFlag() {
      setFlag(F_FINAL);
    }

    public void setFinalFlag(int code) {
      int curr = finalCode();
      if (curr < 0 || curr > code) {
        setTerminalCode(code);
        setFinalFlag();
      }
    }

    public void setTerminalCode(int n) {
      if (n > MAX_TERMINAL_CODE) {
        throw new DFAError("Terminal code too large");
      }

      flags = (char) ( (flags & ~F_TERMINALCODE) | n);
    }

    /*	Determine the final state code associated with this state
                    < code, or -1 if none
     */
    public int finalCode() {
      return (flags & F_TERMINALCODE) - 1;
    }

    public void setTerminalCode() {
      setTerminalCode(1 + findTokenID());

    }

    public int getTerminalCode() {
      return (flags & F_TERMINALCODE) - 1;
    }

    public boolean finalFlag() {
      return flag(F_FINAL);
    }

    public void setFlag(int f) {
      flags |= f;
    }

    public boolean flag(int f) {
      return (flags & f) != 0;
    }

    /**
     * Find insertion position of transition
     * @param symbol : symbol for transition
     * @return position where symbol is to be inserted / replace existing one
     */
    protected int findSymbol(int symbol) {
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
        }
        else {
          min = test + 1;
        }
      }
      return min;
    }

    private char transSymbol(int index) {
      return trans.charAt(index << 1);
    }

    protected int nTrans() {
      int out = 0;
      if (trans != null) {
        out = trans.length() >> 1;
      }
      return out;
    }

//    /**
//     * Add transition.  Any existing transition on the symbol is replaced.
//     * @param symbol : symbol to transit on
//     * @param stateD : destination state
//     */
//    public void addTransition(char symbol, int stateD) {
//      DFA.locked();
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
//    }

    /**
     * Get state to move to on a symbol
     * @param symbol
     * @return destination state, or -1 if no transition exists
     */
    public int getTransitionState(char symbol) {
      int out = -1;
      int insPos = findSymbol(symbol);
      if (symbolMatches(symbol, insPos)) {
        out = trans.charAt( (insPos << 1) + 1);
      }
      return out;
    }

//    public DFAState() {
////      reset();
//    }

    protected boolean symbolMatches(char symbol, int index) {
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

    /*	Read state from file
     */
    public void read(DataInputStream r) throws IOException {
      read(r, BigDFA.VERSION);
    }

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
    protected char flags;

    // State transitions.
    // Each transition is stored as a <symbol, state> pair.
    protected StringBuilder trans = new StringBuilder();

    // ---------- 'big' DFA functions only



}
