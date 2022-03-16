package es.upm.babel.sequenceTester;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.HashMap;
import java.io.StringWriter;
import java.io.PrintWriter;


/**
 * A description of which calls must or may become unblocked, optionally with attached oracles.
 */

public class Unblocks {
  private Set<String> mustUnblock;
  private Set<String> mayUnblock;

  /**
   * Creates an ublocks specification.
   * The mustUnblock parameter specifies which calls must unblock, and the mayUnblock parameter
   * specifies which calls may unblock.
   */
  public Unblocks(Set<String> mustUnblock,
                  Set<String> mayUnblock) {
    this.mustUnblock =  mustUnblock == null ? new HashSet<String>() : mustUnblock;
    this.mayUnblock = mayUnblock == null ? new HashSet<String>() : mayUnblock;
  }


  //////////////////////////////////////////////////////////////////////

  boolean checkCalls(List<Call<?>> calls, Set<Call<?>> newUnblocked, Set<Call<?>> allCalls, Set<Call<?>> blockedCalls, String configurationDescription, boolean doFail, boolean doPrint) {

    boolean isOk = true;
    
    // Check that each unbloked call is either
    // listed in the may or must unblocked enumeration.
    for (Call<?> unblockedCall : newUnblocked) {
      if (!mustUnblock.contains(unblockedCall.getSymbolicName()) &&
          !mayUnblock.contains(unblockedCall.getSymbolicName())) {
        isOk = false;
        if (doFail || doPrint)
          print_reason_for_unblocking_incorrectly(unblockedCall,calls,configurationDescription, doFail, doPrint);
      }
      if (!isOk) break;
    }

    Set<Call<?>> wronglyUnblocked = new HashSet<>();
    if (isOk) {
      // Check that each call that must have been unblocked,
      // is no longer blocked
      for (String key : mustUnblock) {
        Call<?> shouldBeUnblockedCall = Call.byName(key);
        if (blockedCalls.contains(shouldBeUnblockedCall)) {
          wronglyUnblocked.add(shouldBeUnblockedCall);
          isOk = false;
        }
      }
    }

    if (wronglyUnblocked.size() > 0) {
      if (doFail || doPrint) {
        String llamadas;
        if (calls.size() > 1)
          llamadas =
            "las llamadas \nparallel\n{\n  "+Call.printCalls(calls)+"\n}\n";
        else
          llamadas = "la llamada "+Call.printCalls(calls);
        doFailOrPrint
          (prefixConfigurationDescription(configurationDescription)+
           "la llamadas "+Call.printCalls(wronglyUnblocked)+
           " todavia son bloqueadas aunque deberian haber sido"+
           " desbloqueadas por "+llamadas+
           "\n"+UnitTest.mkTrace(),
           doFail, doPrint);
      }
    }
    return isOk;
  }
    
  private String returned(Object value) {
    if (value == null)
      return "terminó normalmente";
    else
      return "devolvió el valor "+value;
  }
  
  private String prefixConfigurationDescription(String configurationDescription) {
    if (configurationDescription == null || configurationDescription.equals("")) return "";
    else return "con la configuration "+configurationDescription+",\n";
  }
    
  private void print_reason_for_unblocking_incorrectly(Call<?> call, List<Call<?>> calls, String configurationDescription, boolean doFail, boolean doPrint) {
    if (call.raisedException()) {
      Throwable exc = call.getException();
      StringWriter errors = new StringWriter();
      exc.printStackTrace(new PrintWriter(errors));
      String StackTrace = errors.toString();
      
      doFailOrPrint
        (prefixConfigurationDescription(configurationDescription)+
         "la llamada "+call.printCall()+
         " deberia bloquear\n"+
         "pero lanzó la excepción "+exc+
         "\n\nStacktrace:\n"+StackTrace+"\n"+UnitTest.mkTrace(),doFail,doPrint);
    } else {
      boolean justExecuted = false;
      for (Call<?> executingCall : calls)
        if (executingCall == call)
          justExecuted = true;
      
      String blockStr;
      if (justExecuted)
        blockStr = "deberia bloquear";
      else
        blockStr = "deberia todavía estar bloqueada después las llamadas "+Call.printCalls(calls);
      
      String returnString = "";
      if (call.hasReturnValue()) returnString = "pero "+returned(call.getReturnValue());

      doFailOrPrint
        (prefixConfigurationDescription(configurationDescription)+
         "la llamada "+call.printCall()+" "+blockStr+"\n"+returnString+
         "\n"+UnitTest.mkTrace(),doFail,doPrint);
    }
  }

  /**
   * Returns the list of calls (and associated oracles) which must unblock.
   */
  public Set<String> mustUnblock() {
    return mustUnblock;
  }
  
  /**
   * Returns the list of calls (and associated oracles) which may unblock.
   */
  public Set<String> mayUnblock() {
    return mayUnblock;
  }

  //////////////////////////////////////////////////////////////////////

  static Set<String> unblocksMap(String... unblocks) {
    Set<String> unblockMap = new HashSet<>();
    for (String unblock : unblocks)
      unblockMap.add(unblock);
    return unblockMap;
  }

  private void doFailOrPrint(String msg, boolean doFail, boolean doPrint) {
    if (doFail)
      UnitTest.failTest(msg);
    else if (doPrint)
      System.out.println(msg);
  }

  //////////////////////////////////////////////////////////////////////


  /**
   * Factory method which specifies that the call parameters must unblock.
   */
  public static Unblocks must(String... unblocks) {
    return  new Unblocks(unblocksMap(unblocks),null);
  }
  
  /**
   * Factory method which specifies that the call parameters may unblock.
   */
  public static Unblocks may(String... unblocks) {
    return  new Unblocks(null, unblocksMap(unblocks));
  }

  public String toString() {
    return "<must = "+mustUnblock+", may="+mayUnblock+">";
  }
}


