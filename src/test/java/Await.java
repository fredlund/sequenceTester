package counter;

import es.upm.babel.sequenceTester.*;


public class Await extends CounterCall implements GetValue {
  private int waitingFor;


  Await(int waitingFor) {
    // Name of thread executing command
    setUser("await");
  }

  public void toTry() {
    controller.await(waitingFor);
  }

  public String toString() {
    return "await("+waitingFor+")";
  }
}
