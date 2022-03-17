package es.upm.babel.sequenceTester;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.io.StringWriter;
import java.io.PrintWriter;


/**
 * A description of which calls must or may become unblocked, optionally with attached oracles.
 */

public class Unblocks {
  private Set<Call<?>> mustUnblock;
  private Set<Call<?>> mayUnblock;

  /**
   * Creates an ublocks specification.
   * The mustUnblock parameter specifies which calls must unblock, and the mayUnblock parameter
   * specifies which calls may unblock.
   */
  public Unblocks(Set<Call<?>> mustUnblock, Set<Call<?>> mayUnblock) {
    this.mustUnblock =  mustUnblock == null ? new HashSet<Call<?>>() : mustUnblock;
    this.mayUnblock = mayUnblock == null ? new HashSet<Call<?>>() : mayUnblock;
  }

  public Unblocks() {
    this(new HashSet<Call<?>>(), new HashSet<Call<?>>());
  }

  public Unblocks(List<Call<?>> mayCalls) {
    this(new HashSet<Call<?>>(mayCalls), new HashSet<Call<?>>());
  }

  public Unblocks(List<Call<?>> mayCalls, List<Call<?>> mustCalls) {
    this(new HashSet<Call<?>>(mayCalls), new HashSet<Call<?>>(mustCalls));
  }

  //////////////////////////////////////////////////////////////////////

  boolean checkCalls(boolean doFail, boolean doPrint) {
    boolean isOk = true;
    UnitTest t = UnitTest.currentTest;
    Set<Call<?>> calls = t.calls;
    
    System.out.println
      ("checkCalls: mustUnblock="+mustUnblock+
       " mayUnblock="+mayUnblock+
       " calls="+Call.printCalls(calls)+" unblocked="+t.unblockedCalls);

    // Check that each unbloked call is either
    // listed in the may or must unblocked enumeration.
    for (Call<?> unblockedCall : t.unblockedCalls()) {
      if (!mustUnblock.contains(unblockedCall) &&
          !mayUnblock.contains(unblockedCall)) {
        isOk = false;
        if (doFail || doPrint)
          printReasonForUnblockingIncorrectly(unblockedCall,calls,t.getConfigurationDescription(), doFail, doPrint);
      }
      if (!isOk) break;
    }

    Set<Call<?>> wronglyUnblocked = new HashSet<>();
    if (isOk) {
      // Check that each call that must have been unblocked,
      // is no longer blocked
      for (Call shouldBeUnblockedCall : mustUnblock) {
        if (t.blockedCalls.contains(shouldBeUnblockedCall)) {
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
          (prefixConfigurationDescription(t.getConfigurationDescription())+
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
    
  private void printReasonForUnblockingIncorrectly(Call<?> call, Set<Call<?>> calls, String configurationDescription, boolean doFail, boolean doPrint) {
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

  //////////////////////////////////////////////////////////////////////

  private void doFailOrPrint(String msg, boolean doFail, boolean doPrint) {
    if (doFail)
      UnitTest.failTest(msg);
    else if (doPrint)
      System.out.println(msg);
  }

  public String toString() {
    return "<must = "+mustUnblock+", may="+mayUnblock+">";
  }
}


