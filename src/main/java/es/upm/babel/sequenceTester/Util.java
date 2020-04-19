package es.upm.babel.sequenceTester;

import java.util.Set;
import java.util.Map;
import java.util.HashSet;


/**
 * Various support methods, e.g., for defining
 * traces of calls.
 */
public class Util {
  
  public static Set<Call> newUnblocked(Call[] calls, Map<Integer,Call> blockedCalls)
  {
    for (Call call : calls)
      blockedCalls.put(call.name(),call);
    
    // Checks which previously blocked calls have become unblocked
    // and remove these unblocked calls from the list of blocked calls
    Set<Call> newUnblocked = computeUnblocked(blockedCalls);
    for (Call unblockedCall : newUnblocked)
      blockedCalls.remove(unblockedCall.name());
    
    return newUnblocked;
  }
  
  public static String extendTrace(Call[] calls, Set<Call> newUnblocked, String trace) {
    // Compute a new trace
    String unblocksString="";
    for (Call unblockedCall : newUnblocked) {
      if (unblocksString=="") unblocksString=unblockedCall.printCallWithReturn();
      else unblocksString+=", "+unblockedCall.toString();
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
  
  static Set<Call> computeUnblocked(Map<Integer,Call> blockedCalls) {
    Set<Call> unblocked = new HashSet<Call>();
    
    for (Call blockedCall : blockedCalls.values()) {
      if (!blockedCall.isBlocked())
        unblocked.add(blockedCall);
    }
    return unblocked;
  }
  
  public static String mkTrace(String trace) {
    return "\nTrace (error detectado en la ultima linea):\n"+trace+"\n\n";
  }
}

