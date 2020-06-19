package es.upm.babel.sequenceTester;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import es.upm.babel.cclib.Tryer;


/**
 * A unit test statement that executes a set of calls, and checks that the correct calls
 * were unblocked and blocked.
 */
public class TestCall {
  private Call[] calls;
  private Unblocks unblocks;
  
  public TestCall(Call[] calls, Unblocks unblocks) {
    this.calls = calls;
    this.unblocks = unblocks;
  }
  
  public String execute(Set<Call> allCalls,
                        Set<Call> blockedCalls,
                        Object controller,
                        String trace,
                        String configurationDescription) {

    // Issue parallel calls
    Set<Call> newUnblocked = Call.execute(calls,controller,allCalls,blockedCalls);
    trace = Util.extendTrace(calls,newUnblocked,trace);
    // Check blocking behaviour
    unblocks.checkCalls(calls,newUnblocked,allCalls,blockedCalls,trace,configurationDescription,true,false);
    return trace;
  }
  
  public Call[] calls() {
    return calls;
  }
  
  public Unblocks unblocks() {
    return unblocks;
  }

  //////////////////////////////////////////////////////////////////////
  //
  // Convenience factory methods.

  public static TestCall unblocks(Call[] calls, String... unblocks) {
    Map<String,Oracle> unblocksMap = Unblocks.unblocksMap(unblocks);
    for (Call call : calls)
      unblocksMap.put(call.getSymbolicName(),null);
    return new TestCall(calls, new Unblocks(unblocksMap,null));
  }
  
  public static TestCall unblocks(Call call, String... unblocks) {
    return unblocks(new Call[] { call }, unblocks);
  }
  
  public static TestCall unblocks(Call[] calls, List<Pair<String,Oracle>> mustUnblocks) {
    Map<String,Oracle> unblocksMap = Unblocks.unblocksMap(mustUnblocks);
    for (Call call : calls)
      unblocksMap.put(call.getSymbolicName(),null);
    return new TestCall(calls, new Unblocks(unblocksMap,null));
  }
  
  public static TestCall unblocks(Call call, List<Pair<String,Oracle>> mustUnblocks) {
    return unblocks(new Call[] { call }, mustUnblocks);
  }
  
  public static TestCall blocks(Call[] calls, String... unblocks) {
    return new TestCall(calls, Unblocks.must(unblocks));
  }

  public static TestCall blocks(Call call, String... unblocks) {
    return blocks(new Call[] {call}, unblocks);
  }
  
  public static TestCall unblocks(Call call) {
    return unblocks(call, new String[] {});
  }
  
  public static TestCall blocks(Call call) {
    return blocks(call, new String[] {});
  }

  public String toString() {
    return
      Call.printCalls(calls) + " with unblocks " + unblocks;
  }
}



