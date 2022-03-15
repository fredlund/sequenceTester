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
  private List<Pair<Unblocks,TestStmt>> alternatives;

  public Branches(List<Call<?>> calls, List<Pair<Unblocks,TestStmt>> alternatives) {
    this.calls = calls;
    this.alternatives = alternatives;
    if (calls.size() <= 0 || alternatives.size() < 0) {
      UnitTest.failTest
        ("badly formed branches with calls.size()="+calls.size()+
         " and alternatives.size()="+alternatives.size());
    }
  }

  public void execute(Set<Call<?>> allCalls,
                      Set<Call<?>> blockedCalls,
                      UnitTest unitTest,
                      String trace) {
    Set<Call<?>> newUnblocked = Call.execute(calls,unitTest,allCalls,blockedCalls);
    trace = Util.extendTrace(calls, newUnblocked, trace);

    // Check that there exists an alternative that explains the execution result
    int index = 0; boolean found=false; 

    while (index < alternatives.size() && !found) {
      Unblocks unblocks = alternatives.get(index).getLeft();
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

    alternatives.get(index).getRight().execute(allCalls,blockedCalls,unitTest,trace);
  }

  public List<Call<?>> calls() {
    return calls;
  }

  public List<Pair<Unblocks,TestStmt>> alternatives() {
    return alternatives;
  }

  public String toString() {
    return
      "Branches (" + Call.printCalls(calls) +
      ")\n [" + alternatives + "]";
  }
}
