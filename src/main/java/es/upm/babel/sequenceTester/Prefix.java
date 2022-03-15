package es.upm.babel.sequenceTester;

import java.util.Set;
import java.util.List;


/**
 * Represents a unit test statement composed by a test call followed by
 * a unit test statement.
 */
public class Prefix implements TestStmt {
  private List<Call<?>> calls;
  private Unblocks unblocks;
  private TestStmt testStmt;

  public Prefix(List<Call<?>> calls, Unblocks unblocks, TestStmt testStmt) {
    if (unblocks == null) {
      System.out.println("null unblocks for "+Call.printCalls(calls));
      throw new RuntimeException();
    }
    this.calls = calls;
    this.unblocks = unblocks;
  }
  
  public void execute(Set<Call<?>> allCalls,
                      Set<Call<?>> blockedCalls,
                      UnitTest unitTest,
                      String trace) {
    Set<Call<?>> newUnblocked = Call.execute(calls,unitTest,allCalls,blockedCalls);
    trace = Util.extendTrace(calls,newUnblocked,trace);
    unblocks.checkCalls(calls,newUnblocked,allCalls,blockedCalls,trace,unitTest.getConfigurationDescription(),true,false);
    testStmt.execute(allCalls, blockedCalls, unitTest, trace);
  }

  public List<Call<?>> calls() {
    return calls;
  }

  public TestStmt stmt() {
    return testStmt;
  }

  public Unblocks unblocks() {
    return unblocks;
  }

  public String toString() {
    return "Prefix("+Call.printCalls(calls)+","+testStmt+")";
  }
}
