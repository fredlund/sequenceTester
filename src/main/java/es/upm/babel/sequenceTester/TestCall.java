package es.upm.babel.sequenceTester;

import java.util.Arrays;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.io.StringWriter;
import java.io.PrintWriter;
import es.upm.babel.cclib.Tryer;


/**
 * A unit test statement that executes a set of calls.
 * Checks a specification of which calls were blocked and unblocked.
 */
public class TestCall {
  private Call[] calls;
  private Set<Integer> callIds;
  private Set<Integer> mustUnblock;
  private Set<Integer> mayUnblock;
  private Map<Integer,Result> unblockChecks;
  
  public TestCall(Call[] calls,
                  int[] mustUnblock,
                  Result[] mustUnblockResults,
                  int[] mayUnblock,
                  Result[] mayUnblockResults) {
    this.calls = calls;
    this.mustUnblock = new HashSet<Integer>();
    if (mustUnblock != null)
      for (Integer i : mustUnblock)
        this.mustUnblock.add(i);
    this.mayUnblock = new HashSet<Integer>();
    if (mayUnblock != null)
      for (Integer i : mayUnblock)
        this.mayUnblock.add(i);
    unblockChecks = new HashMap<Integer,Result>();
    if (mustUnblockResults != null) {
      for (int i=0; i<mustUnblock.length-1; i++) {
        if (mustUnblockResults[i] != null)
          unblockChecks.put(mustUnblock[i],mustUnblockResults[i]);
      }
    }
    if (mayUnblockResults != null) {
      for (int i=0; i<mayUnblock.length; i++) {
        if (mayUnblockResults[i] != null)
          unblockChecks.put(mayUnblock[i],mayUnblockResults[i]);
      }
    }
  }
  
  public String execute(Map<Integer,Call> allCalls,
                        Map<Integer,Call> blockedCalls,
                        Object controller,
                        String trace,
                        String configurationDescription) {
    // Issue parallel calls
    System.out.println("Will execute "+Call.printCalls(calls));
    Call.execute(calls,controller,allCalls);
    
    // Compute unblocked (and change blockedCalls)
    Set<Call> newUnblocked = Util.newUnblocked(calls, blockedCalls);
    
    String callsString = Call.printCalls(calls);
    
    trace = Util.extendTrace(calls,newUnblocked,trace);
    
    // Check that each unbloked call is either
    // listed in the xor or must unblocked enumeration,
    // and check that the value (or exception) is correct
    for (Call unblockedCall : newUnblocked) {
      if (!mustUnblock.contains(unblockedCall.name()) &&
          !mayUnblock.contains(unblockedCall.name())) {
        print_reason_for_unblocking_incorrectly(unblockedCall,trace,configurationDescription);
      }
      
      Result r = unblockedCall.result();
      if (r == null) r = unblockChecks.get(unblockedCall.name());
      if (r != null && r.hasReturnCheck()) {
        if (!unblockedCall.raisedException()) {
          Object result = unblockedCall.returnValue();
          if (!r.shouldReturn())
            UnitTest.failTest
              (prefixConfigurationDescription(configurationDescription)+
               "la llamada "+unblockedCall.printCall()+
               " deberia haber lanzado "+
               "una excepcion "+
               "pero "+returned(unblockedCall.returnValue())+
               "\n"+Util.mkTrace(trace));
          if (r.checksValue() && !r.correctReturnValue(result)) {
            if (r.hasUniqueReturnValue()) {
              Object uniqueReturnValue = r.uniqueReturnValue();
              UnitTest.failTest
                (prefixConfigurationDescription(configurationDescription)+
                 "la llamada "+unblockedCall.printCall()+
                 " devolvió el valor "+
                 "incorrecto: "+result+
                 "; debería haber devuelto el valor "+
                 uniqueReturnValue+
                 "\n"+Util.mkTrace(trace));
            } else {
              UnitTest.failTest
                (prefixConfigurationDescription(configurationDescription)+
                 "la llamada "+unblockedCall.printCall()+
                 " devolvió el valor "+
                 "incorrecto: "+result+"\n"+Util.mkTrace(trace));
            }
          }
        } else {
          Throwable exc = unblockedCall.getException();
          StringWriter errors = new StringWriter();
          exc.printStackTrace(new PrintWriter(errors));
          String StackTrace = errors.toString();
          
          if (r.shouldReturn()) {
            UnitTest.failTest
              (prefixConfigurationDescription(configurationDescription)+
               "la llamada "+unblockedCall.printCall()+
               " deberia haber terminado normalmente "+
               "pero lanzó la excepción "+exc+
               "\nStacktrace:\n"+StackTrace+"\n"+Util.mkTrace(trace));
          }
          if (r.checksValue() && !r.correctException(exc))
            UnitTest.failTest
              (prefixConfigurationDescription(configurationDescription)+
               "la llamada "+unblockedCall.printCall()+
               " lanzo la excepcion "+
               "incorrecto: "+exc+
               "; debería haber lanzado la exception "+
               r.correctExceptionClass().getName()+
               "\n"+"\nStacktrace:\n"+StackTrace+"\n"+Util.mkTrace(trace));
        }	    
      }
    }
    
    // Check that each call that must have been unblocked,
    // is no longer blocked
    for (Integer UnblockedId : mustUnblock) {
      if (blockedCalls.containsKey(UnblockedId)) {
        Call call = allCalls.get(UnblockedId);
        if (call.isBlocked()) {
          String llamadas;
          if (calls.length > 1)
            llamadas =
              "las llamadas \nparallel\n{\n  "+callsString+"\n}\n";
          else
            llamadas = "la llamada "+callsString;
          UnitTest.failTest
            (prefixConfigurationDescription(configurationDescription)+
             "la llamada "+call.printCall()+
             " todavia es bloqueado aunque deberia haber sido"+
             " desbloqueado por "+llamadas+
             "\n"+Util.mkTrace(trace));
        }
      }
    }
    return trace;
  }
  
  String prefixConfigurationDescription(String configurationDescription) {
    if (configurationDescription == "") return "";
    else return "con la configuration "+configurationDescription+",\n";
  }
  
  void print_reason_for_unblocking_incorrectly(Call call, String trace, String configurationDescription) {
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
         "\n\nStacktrace:\n"+StackTrace+"\n"+Util.mkTrace(trace));
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
      
      UnitTest.failTest
        (prefixConfigurationDescription(configurationDescription)+
         "la llamada "+call.printCall()+" "+blockStr+"\n"+
         "pero "+returned(call.returnValue())+
         "\n"+Util.mkTrace(trace));
    }
  }
  
  public static TestCall unblocks(Call call) {
    return
      new TestCall(new Call[] {call},
                   new int[] {call.name()},
                   null,
                   new int[] {},
                   null);
  }
  
  public static TestCall unblocks(BasicCall bc) {
    Call call = new Call(bc);
    return
      new TestCall(new Call[] {call},
                   new int[] {call.name()},
                   null,
                   new int[] {},
                   null);
  }
  
  public static TestCall blocks(String svar, BasicCall bc) {
    Call call = new Call(bc).n(svar);
    return
      new TestCall(new Call[] {call},
                   new int[] {},
                   null,
                   new int[] {},
                   null);
  }
  
  public static TestCall blocks(Call call) {
    return
      new TestCall(new Call[] {call},
                   new int[] {},
                   null,
                   new int[] {},
                   null);
  }
  
  public static int[] unblocks(String... parms) {
    int intparms[] = new int[parms.length];
    for (int i=0; i<parms.length; i++)
      intparms[i] = Call.lookupCall(parms[i]).name();
    return intparms;
  }
  
  public static int[] unblocks(Pair<String,Return>... parms) {
    int intparms[] = new int[parms.length];
    for (int i=0; i<parms.length; i++)
      intparms[i] = Call.lookupCall(parms[i].getLeft()).name();
    return intparms;
  }
  
  public static TestCall unblocks(BasicCall bc, String... unblocks) {
    Call call = new Call(bc);
    int unblock_spec[] = unblocks(unblocks);
    int unblocks_arg[] = new int[unblock_spec.length+1];
    for (int i=0; i<unblock_spec.length; i++)
      unblocks_arg[i] = unblock_spec[i];
    unblocks_arg[unblock_spec.length] = call.name();
    return
      new TestCall(new Call[] {call},
                   unblocks_arg,
                   null,
                   new int[] {},
                   null);
  }
  
  public static TestCall unblocks(Call call, String... unblocks) {
    int unblock_spec[] = unblocks(unblocks);
    int unblocks_arg[] = new int[unblock_spec.length+1];
    for (int i=0; i<unblock_spec.length; i++)
      unblocks_arg[i] = unblock_spec[i];
    unblocks_arg[unblock_spec.length] = call.name();
    return
      new TestCall(new Call[] {call},
                   unblocks_arg,
                   null,
                   new int[] {},
                   null);
  }
  
  public static TestCall unblocks(BasicCall bc, Pair<String,Return>... unblocks) {
    Call call = new Call(bc);
    int unblock_spec[] = unblocks(unblocks);
    int unblocks_arg[] = new int[unblock_spec.length+1];
    for (int i=0; i<unblock_spec.length; i++)
      unblocks_arg[i] = unblock_spec[i];
    unblocks_arg[unblock_spec.length] = call.name();
    Result results[] = new Result[unblock_spec.length];
    for (int i=0; i<unblock_spec.length; i++)
      results[i] = unblocks[i].getRight();
    return
      new TestCall(new Call[] {call},
                   unblocks_arg,
                   results,
                   new int[] {},
                   null);
  }
  
  public static TestCall unblocks(Call call, Pair<String,Return>... unblocks) {
    int unblock_spec[] = unblocks(unblocks);
    int unblocks_arg[] = new int[unblock_spec.length+1];
    for (int i=0; i<unblock_spec.length; i++)
      unblocks_arg[i] = unblock_spec[i];
    unblocks_arg[unblock_spec.length] = call.name();
    Result results[] = new Result[unblock_spec.length];
    for (int i=0; i<unblock_spec.length; i++)
      results[i] = unblocks[i].getRight();
    return
      new TestCall(new Call[] {call},
                   unblocks_arg,
                   results,
                   new int[] {},
                   null);
  }
  
  public static TestCall blocks(String svar, BasicCall bc, String... unblocks) {
    Call call = new Call(bc).n(svar);
    int unblock_spec[] = unblocks(unblocks);
    return new TestCall(new Call[] {call},
                        unblock_spec,
                        null,
                        new int[]{},
                        null);
  }
  
  public static TestCall blocks(Call call, String... unblocks) {
    int unblock_spec[] = unblocks(unblocks);
    return new TestCall(new Call[] {call},
                        unblock_spec,
                        null,
                        new int[]{},
                        null);
  }
  
  public static TestCall may(Call[] calls, String... parms) {
    return new TestCall(calls,
                        new int[] {},
                        null,
                        unblocks(parms),
                        null);
  }
  
  public static TestCall must(Call[] calls, String... parms) {
    return new TestCall(calls,
                        unblocks(parms),
                        null,
                        new int[] {},
                        null);
  }
  
  public String returned(Object value) {
    if (value == null)
      return "terminó normalmente";
    else
      return "devolvió el valor "+value;
  }
  
  public Call[] calls() {
    return calls;
  }
  
  public Set<Integer> mustUnblock() {
    return mustUnblock;
  }
  
  public Set<Integer> mayUnblock() {
    return mayUnblock;
  }
  
  
}



