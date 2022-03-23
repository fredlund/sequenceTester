package counter;

import es.upm.babel.sequenceTester.*;


public class Await extends VoidCall {
  private final int waitingFor;
  private final Counter counter;

  Await(Counter counter, int waitingFor) {
    this.counter = counter;
    this.waitingFor = waitingFor;
    setUser("await");
  }

  public void execute() {
    counter.await(waitingFor);
  }

  public String toString() {
    return "await("+waitingFor+")";
  }
}
