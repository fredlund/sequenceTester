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
  private Map<String,Oracle<?>> mustUnblock;
  private Map<String,Oracle<?>> mayUnblock;

  /**
   * Creates an ublocks specification.
   * The mustUnblock parameter specifies which calls must unblock, and the mayUnblock parameter
   * specifies which calls may unblock.
   */
  public Unblocks(Map<String,Oracle<?>> mustUnblock,
                  Map<String,Oracle<?>> mayUnblock) {
    this.mustUnblock =  mustUnblock == null ? new HashMap<String,Oracle<?>>() : mustUnblock;
    this.mayUnblock = mayUnblock == null ? new HashMap<String,Oracle<?>>() : mayUnblock;
  }


  //////////////////////////////////////////////////////////////////////

  boolean checkCalls(List<Call<?>> calls, Set<Call<?>> newUnblocked, Set<Call<?>> allCalls, Set<Call<?>> blockedCalls, String trace, String configurationDescription, boolean doFail, boolean doPrint) {

    boolean isOk = true;
    
    // Check that each unbloked call is either
    // listed in the may or must unblocked enumeration,
    // and check that the value (or exception) is correct
    for (Call<?> unblockedCall : newUnblocked) {
      if (!mustUnblock.containsKey(unblockedCall.getSymbolicName()) &&
          !mayUnblock.containsKey(unblockedCall.getSymbolicName())) {
        isOk = false;
        if (doFail || doPrint)
          print_reason_for_unblocking_incorrectly(unblockedCall,calls,trace,configurationDescription, doFail, doPrint);
      }
      
      // Now for checking the results of all unblocked calls
      // Note that we can have oracles in three places:
      // associated with the call itself, and/or in a must unblock spec, and/or in a may unblock spec.
      // We need to check all.

      // First check if the call itself has an oracle
      isOk = isOk && checkOracle(unblockedCall, unblockedCall.getOracle(), trace, configurationDescription, doFail, doPrint);
      
      // There may also be an oracle in the must unblock specification
      isOk = isOk && checkOracle(unblockedCall, mustUnblock.get(unblockedCall.getSymbolicName()), trace, configurationDescription, doFail, doPrint);

      // There may also be an oracle in the may unblock specification
      isOk = isOk && checkOracle(unblockedCall, mayUnblock.get(unblockedCall.getSymbolicName()), trace, configurationDescription, doFail, doPrint);

      if (!isOk) break;
    }

    Set<Call<?>> wronglyUnblocked = new HashSet<>();
    if (isOk) {
      // Check that each call that must have been unblocked,
      // is no longer blocked
      for (String key : mustUnblock.keySet()) {
        Call<?> shouldBeUnblockedCall = Call.lookupCall(key);
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
           "\n"+Util.mkTrace(trace),
           doFail, doPrint);
      }
    }
  
    return isOk;
  }
  
 boolean checkOracle(Call<?> unblockedCall, Oracle<?> o, String trace, String configurationDescription, boolean doFail, boolean doPrint) {
    boolean isOk = true;

    if (o != null) {
      // Did the call terminate with an exception?
      if (unblockedCall.raisedException()) {

        // Yes...
        Throwable exc = unblockedCall.getException();
        
        // Does the oracle specify a normal return? (i.e., no exception)
        if (o.returnsNormally()) {
          // Yes, an error...
          isOk = false;
          if (doFail || doPrint) {
            StringWriter errors = new StringWriter();
            exc.printStackTrace(new PrintWriter(errors));
            String StackTrace = errors.toString();
          
            doFailOrPrint
              (prefixConfigurationDescription(configurationDescription)+
               "la llamada "+unblockedCall.printCall()+
               " deberia haber terminado normalmente "+
               "pero lanzó la excepción "+exc+
               "\nStacktrace:\n"+StackTrace+"\n"+Util.mkTrace(trace), doFail, doPrint);
          }
        }
        
        // Else check if the exception is the correct exception
        if (isOk && !o.correctException(exc)) {
          isOk = false;

          if (doFail || doPrint) {
            StringWriter errors = new StringWriter();
            exc.printStackTrace(new PrintWriter(errors));
            String StackTrace = errors.toString();
            
            doFailOrPrint
              (prefixConfigurationDescription(configurationDescription)+
               "la llamada "+unblockedCall.printCall()+
               " lanzo la excepcion "+
               "incorrecto: "+exc+
               "; debería haber lanzado la exception "+
               o.correctExceptionClass().getName()+
               "\n"+"\nStacktrace:\n"+StackTrace+"\n"+Util.mkTrace(trace), doFail, doPrint);
          }
        }
      } else {
        // Does the oracle specify an exception?
        if (!o.returnsNormally()) {
          
          // Yes; an error
          isOk = false;
          if (doFail || doPrint)
            doFailOrPrint
              (prefixConfigurationDescription(configurationDescription)+
               "la llamada "+unblockedCall.printCall()+
               " deberia haber lanzado "+
               "la excepcion "+o.correctExceptionClass()+
               "pero terminó normalmente"+
               "\n"+Util.mkTrace(trace), doFail, doPrint);
        }
        
        // No, a normal return was specified.
        // Check the return value
        if (isOk && o.checksReturnValue()) {
          Object result = unblockedCall.returnValue();
          if (!o.correctReturnValue(unblockedCall.returnValue())) {
            // An error
            isOk = false;

            if (doFail || doPrint) {
              // Does the oracle specify a unique return value?
              if (o.hasUniqueReturnValue()) {
              
                // Yes; we can provide better diagnostic output
                Object uniqueReturnValue = o.uniqueReturnValue();
                doFailOrPrint
                  (prefixConfigurationDescription(configurationDescription)+
                   "la llamada "+unblockedCall.printCall()+
                   " devolvió el valor "+
                   "incorrecto: "+result+
                   "; debería haber devuelto el valor "+
                   uniqueReturnValue+
                   "\n"+Util.mkTrace(trace), doFail, doPrint);
              } else {
                // No; possibly worse diagnostic output
                String errorStr = o.error();

                doFailOrPrint
                  (prefixConfigurationDescription(configurationDescription)+
                   "la llamada "+unblockedCall.printCall()+
                   " devolvió el valor "+
                   "incorrecto: "+result+
                   (errorStr != null ? " "+errorStr : "")+
                   "\n"+Util.mkTrace(trace), doFail, doPrint);
              }
            }
          }
        }
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
    
  private void print_reason_for_unblocking_incorrectly(Call<?> call, List<Call<?>> calls, String trace, String configurationDescription, boolean doFail, boolean doPrint) {
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
         "\n\nStacktrace:\n"+StackTrace+"\n"+Util.mkTrace(trace),doFail,doPrint);
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
      if (call.hasReturnValue()) returnString = "pero "+returned(call.returnValue());

      doFailOrPrint
        (prefixConfigurationDescription(configurationDescription)+
         "la llamada "+call.printCall()+" "+blockStr+"\n"+returnString+
         "\n"+Util.mkTrace(trace),doFail,doPrint);
    }
  }

  /**
   * Returns the list of calls (and associated oracles) which must unblock.
   */
  public Map<String,Oracle<?>> mustUnblock() {
    return mustUnblock;
  }
  
  /**
   * Returns the list of calls (and associated oracles) which may unblock.
   */
  public Map<String,Oracle<?>> mayUnblock() {
    return mayUnblock;
  }

  //////////////////////////////////////////////////////////////////////

  static Map<String,Oracle<?>> unblocksMap(String... unblocks) {
    Map<String,Oracle<?>> unblockMap = new HashMap<>();
    for (String unblock : unblocks)
      unblockMap.put(unblock,null);
    return unblockMap;
  }

  static Map<String,Oracle<?>> unblocksMap(List<Pair<String,Oracle<?>>> unblocks) {
    Map<String,Oracle<?>> unblockMap = new HashMap<>();
    for (Pair<String,Oracle<?>> pair : unblocks)
      unblockMap.put(pair.getLeft(),pair.getRight());
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

  //////////////////////////////////////////////////////////////////////
  
  public static Pair<List<Call<?>>,Unblocks> unblocks(List<Call<?>> calls, String... unblocks) {
    Map<String,Oracle<?>> unblocksMap = Unblocks.unblocksMap(unblocks);
    for (Call call : calls)
      unblocksMap.put(call.getSymbolicName(),null);
    return new Pair<List<Call<?>>,Unblocks>(calls, new Unblocks(unblocksMap,null));
  }
  
  public static Pair<List<Call<?>>,Unblocks> unblocks(Call<?> call, String... unblocks) {
    List<Call<?>> list = new ArrayList<>();
    list.add(call);
    return unblocks(list, unblocks);
  }
  
  public static Pair<List<Call<?>>,Unblocks> unblocks(List<Call<?>> calls, List<Pair<String,Oracle<?>>> mustUnblocks) {
    Map<String,Oracle<?>> unblocksMap = Unblocks.unblocksMap(mustUnblocks);
    for (Call<?> call : calls)
      unblocksMap.put(call.getSymbolicName(),null);
    return new Pair<List<Call<?>>,Unblocks>(calls, new Unblocks(unblocksMap,null));
  }
  
  public static Pair<List<Call<?>>,Unblocks> unblocks(Call<?> call, List<Pair<String,Oracle<?>>> mustUnblocks) {
    List<Call<?>> list = new ArrayList<>();
    list.add(call);
    return unblocks(list, mustUnblocks);
  }
  
  public static Pair<List<Call<?>>,Unblocks> blocks(List<Call<?>> calls, String... unblocks) {
    return new Pair<List<Call<?>>,Unblocks>(calls, Unblocks.must(unblocks));
  }

  public static Pair<List<Call<?>>,Unblocks> blocks(Call<?> call, String... unblocks) {
    List<Call<?>> list = new ArrayList<>();
    list.add(call);
    return blocks(list, unblocks);
  }
  
  public static Pair<List<Call<?>>,Unblocks> unblocks(Call<?> call) {
    return unblocks(call, new String[] {});
  }
  
  public static Pair<List<Call<?>>,Unblocks> blocks(Call<?> call) {
    return blocks(call, new String[] {});
  }
}


