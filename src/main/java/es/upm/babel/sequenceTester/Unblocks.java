package es.upm.babel.sequenceTester;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.io.StringWriter;
import java.io.PrintWriter;


/**
 * Provides methods for specifying and checking which may or must become unblocked.
 */

public class Unblocks {
  private final Set<Call<?>> mustUnblock;
  private final Set<Call<?>> mayUnblock;

  /**
   * Creates an ublocks specification.
   * The mustUnblock parameter specifies which calls must unblock, and the mayUnblock parameter
   * specifies which calls may unblock.
   */
  public Unblocks(Set<Call<?>> mustUnblock, Set<Call<?>> mayUnblock) {
    this.mustUnblock =  mustUnblock == null ? new HashSet<Call<?>>() : mustUnblock;
    this.mayUnblock = mayUnblock == null ? new HashSet<Call<?>>() : mayUnblock;
  }

  /**
   * Specifies that no calls may be unblocked.
   */
  public Unblocks() {
    this(new HashSet<Call<?>>(), new HashSet<Call<?>>());
  }

  /**
   * Specifies that a number of calls must be unblocked.
   */
  public Unblocks(List<Call<?>> mustCalls) {
    this(new HashSet<Call<?>>(mustCalls), new HashSet<Call<?>>());
  }

  /**
   * Specifies that a number of calls must be unblocked, and
   * that (some other) calls may be unblocked.
   */
  public Unblocks(List<Call<?>> mustCalls, List<Call<?>> mayCalls) {
    this(new HashSet<Call<?>>(mustCalls), new HashSet<Call<?>>(mayCalls));
  }

  //////////////////////////////////////////////////////////////////////

  void checkCalls(Execute e) {
    boolean isOk = true;
    UnitTest t = UnitTest.getCurrentTest();
    List<Call<?>> calls = e.getCalls();
    Set<Call<?>> unblockedCalls = e.getUnblockedCalls();
    
    // Check that each unbloked call is either
    // listed in the may or must unblocked enumeration.
    for (Call<?> unblockedCall : unblockedCalls) {
      if (!mustUnblock.contains(unblockedCall) &&
          !mayUnblock.contains(unblockedCall)) {
        isOk = false;
        printReasonForUnblockingIncorrectly(unblockedCall,calls,t.getConfigurationDescription());
      }
      if (!isOk) break;
    }

    Set<Call<?>> wronglyUnblocked = new HashSet<>();
    if (isOk) {
      // Check that each call that must have been unblocked,
      // is no longer blocked
      for (Call shouldBeUnblockedCall : mustUnblock) {
        if (e.getBlockedCalls().contains(shouldBeUnblockedCall)) {
          wronglyUnblocked.add(shouldBeUnblockedCall);
          isOk = false;
        }
      }
    }

    if (wronglyUnblocked.size() > 0) {
      String llamadas;
      if (calls.size() > 1)
        llamadas =
          "las llamadas \nparallel\n{\n  "+Call.printCalls(calls)+"\n}\n";
      else
        llamadas = "la llamada "+Call.printCalls(calls);
      UnitTest.failTest
        (prefixConfigurationDescription(t.getConfigurationDescription())+
         "la llamadas "+Call.printCalls(wronglyUnblocked)+
         " todavia son bloqueadas aunque deberian haber sido"+
         " desbloqueadas\n");
    }
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
    
  private void printReasonForUnblockingIncorrectly(Call<?> call, List<Call<?>> calls, String configurationDescription) {
    if (call.raisedException()) {
      Throwable exc = call.getException();
      StringWriter errors = new StringWriter();
      exc.printStackTrace(new PrintWriter(errors));
      String StackTrace = errors.toString();
      
      UnitTest.failTest
        (prefixConfigurationDescription(configurationDescription)+
         "la llamada "+call.printCall()+
         " deberia bloquear\n"+
         "pero lanzó la excepción "+exc+
         "\n\nStacktrace:\n"+StackTrace+"\n");
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

      UnitTest.failTest
        (prefixConfigurationDescription(configurationDescription)+
         "la llamada "+call.printCall()+" "+blockStr+"\n"+returnString+"\n");
    }
  }

  /**
   * Returns the set of calls (and associated oracles) which must unblock.
   */
  public Set<Call<?>> mustUnblock() {
    return mustUnblock;
  }
  
  /**
   * Returns the set of calls (and associated oracles) which may unblock.
   */
  public Set<Call<?>> mayUnblock() {
    return mayUnblock;
  }

  public String toString() {
    return "<must = "+mustUnblock+", may="+mayUnblock+">";
  }
}


