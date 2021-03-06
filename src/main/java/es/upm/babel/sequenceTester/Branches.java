package es.upm.babel.sequenceTester;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Arrays;


/**
 * Represents a unit test statement branch in an unit test.
 */
public class Branches implements TestStmt {
  private List<Call<?>> calls;
  private Alternative[] alternatives;

  public Branches(List<Call<?>> calls, Alternative[] alternatives) {
    this.calls = calls;
    this.alternatives = alternatives;
    if (calls.size() <= 0 || alternatives.length < 0) {
      UnitTest.failTest
        ("badly formed branches with calls.size()="+calls.size()+
         " and alternatives.length="+alternatives.length);
    }
  }

  public void execute(Set<Call<?>> allCalls,
                      Set<Call<?>> blockedCalls,
                      UnitTest unitTest,
                      String trace) {
    Set<Call<?>> newUnblocked = Call.execute(calls,unitTest,allCalls,blockedCalls);
    trace = Util.extendTrace(calls, newUnblocked, trace);

    // Check that there exists an alternative that explains the execution result
    int index = 0; boolean found=false; Alternative alternative = null;

    while (index < alternatives.length && !found) {
      alternative = alternatives[index];
      Unblocks unblocks = alternative.unblocks();
      if (unblocks.checkCalls(calls,newUnblocked,allCalls,blockedCalls,trace,unitTest.getConfigurationDescription(),false,false))
        found = true;
      else 
        ++index;
    }

    if (!found) {
      String unblocksString="";
      for (Call unblockedCall : newUnblocked) {
        if (unblocksString=="") unblocksString=unblockedCall.toString();
        else unblocksString+=", "+unblockedCall;
      }

      UnitTest.failTest
        ("ninguno de los alternativos desbloquea correctamente todos las llamadas "+
         unblocksString+
         " que fueron desbloqueados"+
         "\n"+Util.mkTrace(trace));
    }

    alternative.continuation().execute(allCalls,blockedCalls,unitTest,trace);
  }

  public List<Call<?>> calls() {
    return calls;
  }

  public Alternative[] alternatives() {
    return alternatives;
  }

  public static Branches branches(List<Call<?>> calls, Alternative... alternatives) {
    return new Branches(calls,alternatives);
  }

  public String toString() {
    return
      "Branches (" + Call.printCalls(calls) +
      ")\n [" + Arrays.toString(alternatives) + "]";
  }
}
