package es.upm.babel.sequenceTester;

import java.util.Map;
import java.util.Set;


/**
 * Represents a unit test statement branch in an unit test.
 */
public class Branches implements TestStmt {
  private Call[] calls;
  private Alternative[] alternatives;

  public Branches(Call[] calls, Alternative[] alternatives) {
    this.calls = calls;
    this.alternatives = alternatives;
    if (calls.length <= 0 || alternatives.length < 0) {
      UnitTest.failTest
        ("badly formed branches with calls.length="+calls.length+
         " and alternatives.length="+alternatives.length);
    }
  }

  public void execute(Map<Integer,Call> allCalls,
                      Map<Integer,Call> blockedCalls,
                      Object controller,
                      String trace,
                      String configurationDescription) {
    Call.execute(calls,controller,allCalls);
    Set<Call> newUnblocked = Util.newUnblocked(calls, blockedCalls);

    trace = Util.extendTrace(calls, newUnblocked, trace);

    int index = 0; boolean found=false;
    while (index < alternatives.length && !found) {
      Alternative alt = alternatives[index];
      if (newUnblocked.size() == alt.unblocks.length) {
        found = true;
        for (Call call : newUnblocked) {
          boolean matches = false;
          int i = 0;
          while (i < alt.unblocks.length && !matches) {
            if (call.getCallId() == alt.unblocks[i]) matches=true;
            else i++;
          }
          if (!matches) {
            found=false;
            break;
          }
        }
      }
      if (!found) ++index;
    }

    if (found) {
      alternatives[index].continuation.execute
        (allCalls,blockedCalls,controller,trace,configurationDescription);
    } else {
      String unblocksString="";
      for (Call unblockedCall : newUnblocked) {
        if (unblocksString=="") unblocksString=unblockedCall.toString();
        else unblocksString+=", "+unblockedCall;
      }

      UnitTest.failTest
        ("ninguno de los alternativos desbloquea todos las llamadas "+
         unblocksString+
         " que fueron desbloqueados"+
         "\n"+Util.mkTrace(trace));
    }
  }

  public Call[] calls() {
    return calls;
  }

  public Alternative[] alternatives() {
    return alternatives;
  }

  public static Branches branches(Call[] calls, Alternative... alternatives) {
    return new Branches(calls,alternatives);
  }
}
