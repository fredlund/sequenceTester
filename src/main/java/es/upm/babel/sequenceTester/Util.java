package es.upm.babel.sequenceTester;

import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;

/**
 * Various support methods, e.g., for defining
 * traces of calls.
 */
public class Util {
  
  public static void unblocked(Set<Call<?>> blockedCalls, Set<Call<?>> unblocked)
  {
    Set<Call<?>> newUnblocked = new HashSet<Call<?>>();

    for (Call<?> blockedCall : blockedCalls) {
      if (!blockedCall.hasBlocked()) {
        newUnblocked.add(blockedCall);
      }
    }
    blockedCalls.removeAll(newUnblocked);
    unblocked.addAll(newUnblocked);
  }
  
  public static String extendTrace(List<Call<?>> calls, Set<Call<?>> newUnblocked, String trace) {
    // Compute a new trace
    String unblocksString="";
    for (Call<?> unblockedCall : newUnblocked) {
      String callString = unblockedCall.printCallWithReturn();
      if (unblocksString=="") unblocksString=callString;
      else unblocksString+=", "+callString;
    }
    if (unblocksString!="")
      unblocksString = " -- unblocked "+unblocksString;
    
    String callsString="";
    for (Call<?> call : calls) {
      if (callsString != "") callsString += "\n  "+call.printCall();
      else callsString = call.printCall();
    }
    
    String callPlusUnblock;
    if (calls.size() > 1)
      callPlusUnblock = "parallel\n  {\n  "+callsString+"\n  }"+unblocksString;
    else
      callPlusUnblock = callsString+unblocksString;
    
    if (trace != "") 
      trace += "\n  "+callPlusUnblock;
    else
      trace = "  "+callPlusUnblock;
    
    return trace;
  }
  
  public static String mkTrace(String trace) {
    return "\nTrace (error detectado en la ultima linea):\n"+trace+"\n\n";
  }

  /**
   * Returns a test statement composed of a sequence of calls.
   */
  public static TestStmt seq(Calls... callsSeq) {
    return seqEndsWith(new Nil(), callsSeq);
  }
  
  /**
   * Returns a test statement composed of a sequence of calls,
   * and ending with test statement parameter.
   */
  public static TestStmt seqEndsWith(TestStmt endStmt, Calls... callsSeq) {
    int index = callsSeq.length-1;
    TestStmt stmt = endStmt;
    while (index >= 0) {
      Calls calls = callsSeq[index--];
      stmt = new Prefix(calls.calls(),calls.unblocks(),stmt);
    }
    return stmt;
  }

  /**
   * Sequential composition of two test statements.
   */
  public static TestStmt compose(TestStmt stmt1, TestStmt stmt2) {
    if (stmt1 instanceof Prefix) {
      Prefix prefix = (Prefix) stmt1;
      List<Call<?>> calls = prefix.calls();
      Unblocks unblocks = prefix.unblocks();
      TestStmt stmt = prefix.stmt();
      return new Prefix(calls,unblocks,compose(stmt,stmt2));
    } else if (stmt1 instanceof Nil) {
      return stmt2;
    } else {
      UnitTest.failTestFramework("cannot compose statements "+stmt1+" and "+stmt2+"\n");
      return stmt1;
    }
  }
}

