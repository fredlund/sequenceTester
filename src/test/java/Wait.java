package counter;

import es.upm.babel.sequenceTester.*;


public class Wait extends CounterCall implements GetValue {
  private int waitingFor;


  Wait(int waitingFor) {
    // Name of thread executing command
    setUser("wait");
  }

  public void toTry() {
    controller.wait(waitingFor);
  }

  public String toString() {
    return "whenEven("+waitingFor+")";
  }
}
