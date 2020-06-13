package es.upm.babel.sequenceTester;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.io.StringWriter;
import java.io.PrintWriter;


/**
 * A description of which calls must or may become unblocked, optionally with attached oracles.
 */

public class Unblocks {
  private List<Pair<Integer,Oracle>> mustUnblock;
  private List<Pair<Integer,Oracle>> mayUnblock;

  public Unblocks(List<Pair<Integer,Oracle>> mustUnblock,
                  List<Pair<Integer,Oracle>> mayUnblock) {
    this.mustUnblock =  mustUnblock == null ? new ArrayList<Pair<Integer,Oracle>>() : mustUnblock;
    this.mayUnblock = mayUnblock == null ? new ArrayList<Pair<Integer,Oracle>>() : mayUnblock;
  }


  //////////////////////////////////////////////////////////////////////

  public boolean checkCalls(Call[] calls, Set<Call> newUnblocked, Map<Integer,Call> allCalls, Map<Integer,Call> blockedCalls, String trace, String configurationDescription, boolean doFail, boolean doPrint) {

    boolean isOk = true;
    
    // Check that each unbloked call is either
    // listed in the may or must unblocked enumeration,
    // and check that the value (or exception) is correct
    for (Call unblockedCall : newUnblocked) {
      if (!contains(unblockedCall.getCallId(),mustUnblock) &&
          !contains(unblockedCall.getCallId(),mayUnblock)) {
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
      isOk = isOk && checkOracle(unblockedCall, getOracle(unblockedCall.getCallId(), mustUnblock()), trace, configurationDescription, doFail, doPrint);

      // There may also be an oracle in the may unblock specification
      isOk = isOk && checkOracle(unblockedCall, getOracle(unblockedCall.getCallId(), mayUnblock()), trace, configurationDescription, doFail, doPrint);

      if (!isOk) break;
    }

    if (isOk) {
      // Check that each call that must have been unblocked,
      // is no longer blocked
      for (Pair<Integer,Oracle> pair : mustUnblock) {
        Integer UnblockedId = pair.getLeft();
        if (blockedCalls.containsKey(UnblockedId)) {
          Call call = allCalls.get(UnblockedId);
          if (call.hasBlocked()) {
            isOk = false;
            if (doFail || doPrint) {
              String llamadas;
              if (calls.length > 1)
              llamadas =
                "las llamadas \nparallel\n{\n  "+Call.printCalls(calls)+"\n}\n";
              else
                llamadas = "la llamada "+Call.printCalls(calls);
              doFailOrPrint
                (prefixConfigurationDescription(configurationDescription)+
                 "la llamada "+call+
                 " todavia es bloqueado aunque deberia haber sido"+
                 " desbloqueado por "+llamadas+
                 "\n"+Util.mkTrace(trace),
                 doFail, doPrint);
            }
            break;
          }
        }
      }
    }

    return isOk;
  }
  

  boolean checkOracle(Call unblockedCall, Oracle o, String trace, String configurationDescription, boolean doFail, boolean doPrint) {
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
               "la llamada "+unblockedCall+
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
               "la llamada "+unblockedCall+
               " lanzo la excepcion "+
               "incorrecto: "+exc+
               "; debería haber lanzado la exception "+
               o.correctExceptionClass().getName()+
               "\n"+"\nStacktrace:\n"+StackTrace+"\n"+Util.mkTrace(trace), doFail, doPrint);
          }
        }
      } else {
        // No, the call terminated normally...
        Object result = unblockedCall.returnValue();
        
        // Does the oracle specify an exception?
        if (!o.returnsNormally()) {
          
          // Yes; an error
          isOk = false;
          if (doFail || doPrint)
            doFailOrPrint
              (prefixConfigurationDescription(configurationDescription)+
               "la llamada "+unblockedCall+
               " deberia haber lanzado "+
               "la excepcion "+o.correctExceptionClass()+
               "pero "+returned(unblockedCall.returnValue())+
               "\n"+Util.mkTrace(trace), doFail, doPrint);
        }
        
        // No, a normal return was specified.
        // Check the return value
        if (isOk && !o.correctReturnValue(result)) {
          
          // An error
          isOk = false;

          if (doFail || doPrint) {
            // Does the oracle specify a unique return value?
            if (o.hasUniqueReturnValue()) {
              
              // Yes; we can provide better diagnostic output
              Object uniqueReturnValue = o.uniqueReturnValue();
              doFailOrPrint
                (prefixConfigurationDescription(configurationDescription)+
                 "la llamada "+unblockedCall+
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
                 "la llamada "+unblockedCall+
                 " devolvió el valor "+
                 "incorrecto: "+result+
                 (errorStr != null ? " "+errorStr : "")+
                 "\n"+Util.mkTrace(trace), doFail, doPrint);
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
    if (configurationDescription == "") return "";
    else return "con la configuration "+configurationDescription+",\n";
  }
    
  private void print_reason_for_unblocking_incorrectly(Call call, Call[] calls, String trace, String configurationDescription, boolean doFail, boolean doPrint) {
    if (call.raisedException()) {
      Throwable exc = call.getException();
      StringWriter errors = new StringWriter();
      exc.printStackTrace(new PrintWriter(errors));
      String StackTrace = errors.toString();
      
      doFailOrPrint
        (prefixConfigurationDescription(configurationDescription)+
         "la llamada "+call+
         " deberia bloquear\n"+
         "pero lanzó la excepción "+exc+
         "\n\nStacktrace:\n"+StackTrace+"\n"+Util.mkTrace(trace),doFail,doPrint);
    } else {
      boolean justExecuted = false;
      for (Call executingCall : calls)
        if (executingCall == call)
          justExecuted = true;
      
      String blockStr;
      if (justExecuted)
        blockStr = "deberia bloquear";
      else
        blockStr = "deberia todavía estar bloqueada después las llamadas "+Call.printCalls(calls);
      
      doFailOrPrint
        (prefixConfigurationDescription(configurationDescription)+
         "la llamada "+call+" "+blockStr+"\n"+
         "pero "+returned(call.returnValue())+
         "\n"+Util.mkTrace(trace),doFail,doPrint);
    }
  }

  //////////////////////////////////////////////////////////////////////


  public static Unblocks must(String... unblocks) {
    return  new Unblocks(unblocksSpec(unblocks),null);
  }
  
  public static Unblocks may(String... unblocks) {
    return  new Unblocks(null, unblocksSpec(unblocks));
  }

  public static List<Pair<Integer,Oracle>> unblocksSpec(String... unblocks) {
    List<Pair<Integer,Oracle>> unblockList = new ArrayList<>();
    for (int i=0; i<unblocks.length; i++)
      unblockList.add(0,unblockSpec(unblocks[i]));
    return unblockList;
  }

  public List<Pair<Integer,Oracle>> mustUnblock() {
    return mustUnblock;
  }
  
  public List<Pair<Integer,Oracle>> mayUnblock() {
    return mayUnblock;
  }

  public static Pair<Integer,Oracle> unblockSpec(Call call) {
    return new Pair<>(call.getCallId(),null);
  }

  public static Pair<Integer,Oracle> unblockSpec(String callName) {
    return unblockSpec(Call.lookupCall(callName));
  }

  public boolean contains(int callId, List<Pair<Integer,Oracle>> unblocks) {
    for (Pair<Integer,Oracle> unblock : unblocks) {
      Integer id = unblock.getLeft();
      if (id != null & id == callId)
        return true;
    }
    return false;
  }

  public static Pair<Integer,Oracle> getUnblockSpec(int callId, List<Pair<Integer,Oracle>> unblocks) {
    for (Pair<Integer,Oracle> unblock : unblocks) {
      Integer id = unblock.getLeft();
      if (id != null & id == callId)
        return unblock;
    }
    return null;
  }

  public static Oracle getOracle(int callId, List<Pair<Integer,Oracle>> unblocks) {
    Pair<Integer,Oracle> unblock = getUnblockSpec(callId,unblocks);
    if (unblock == null) return null;
    return unblock.getRight();
  }

  public String toString() {
    return "<must = "+mustUnblock+", may="+mayUnblock+">";
  }

  private void doFailOrPrint(String msg, boolean doFail, boolean doPrint) {
    if (doFail)
      UnitTest.failTest(msg);
    else if (doPrint)
      System.out.println(msg);
  }
}


