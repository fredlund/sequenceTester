package es.upm.babel.sequenceTester;

import java.util.Set;
import java.util.Map;
import java.util.HashSet;


/**
 * Various support methods, e.g., for defining
 * traces of calls.
 */
public class Util {
  
  public static Set<Call> newUnblocked(Call[] calls, Set<Call> blockedCalls)
  {
    for (Call call : calls) {
      blockedCalls.add(call);
    }

    // Checks which previously blocked calls have become unblocked
    // and remove these unblocked calls from the list of blocked calls
    Set<Call> newUnblocked = computeUnblocked(blockedCalls);
    for (Call unblockedCall : newUnblocked)
      blockedCalls.remove(unblockedCall);
    
    return newUnblocked;
  }
  
  public static String extendTrace(Call[] calls, Set<Call> newUnblocked, String trace) {
    // Compute a new trace
    String unblocksString="";
    for (Call unblockedCall : newUnblocked) {
      String callString = unblockedCall.printCallWithReturn();
      if (unblocksString=="") unblocksString=callString;
      else unblocksString+=", "+callString;
    }
    if (unblocksString!="")
      unblocksString = " -- unblocked "+unblocksString;
    
    String callsString="";
    for (Call call : calls) {
      if (callsString != "") callsString += "\n  "+call;
      else callsString = call.toString();
    }
    
    String callPlusUnblock;
    if (calls.length > 1)
      callPlusUnblock = "parallel\n  {\n  "+callsString+"\n  }"+unblocksString;
    else
      callPlusUnblock = callsString+unblocksString;
    
    if (trace != "") 
      trace += "\n  "+callPlusUnblock;
    else
      trace = "  "+callPlusUnblock;
    
    return trace;
  }
  
  static Set<Call> computeUnblocked(Set<Call> blockedCalls) {
    Set<Call> unblocked = new HashSet<Call>();
    
    for (Call blockedCall : blockedCalls) {
      if (!blockedCall.hasBlocked())
        unblocked.add(blockedCall);
    }
    return unblocked;
  }
  
  public static String mkTrace(String trace) {
    return "\nTrace (error detectado en la ultima linea):\n"+trace+"\n\n";
  }

  /**
   * Returns a test statement composed of a sequence of calls.
   */
  public static TestStmt sequence(TestCall... testCalls) {
    return sequenceEndsWith(testCalls, new Nil());
  }
  
  /**
   * Returns a test statement composed of a sequence of calls,
   * and ending with test statement parameter.
   */
  public static TestStmt sequenceEndsWith(TestCall[] testCalls, TestStmt endStmt) {
    int index = testCalls.length-1;
    TestStmt stmt = endStmt;
    while (index >= 0) {
      stmt = new Prefix(testCalls[index--],stmt);
    }
    return stmt;
  }
  
  /**
   * Sequential composition of two test statements.
   */
  public static TestStmt compose(TestStmt stmt1, TestStmt stmt2) {
    if (stmt1 instanceof Prefix) {
      Prefix prefix = (Prefix) stmt1;
      TestCall testCall = prefix.testCall();
      TestStmt stmt = prefix.stmt();
      return new Prefix(testCall,compose(stmt,stmt2));
    } else if (stmt1 instanceof Nil) {
      return stmt2;
    } else {
      UnitTest.failTestFramework("cannot compose statements "+stmt1+" and "+stmt2+"\n");
      return stmt1;
    }
  }
}

