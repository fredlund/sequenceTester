package counter;

import es.upm.babel.sequenceTester.*;


public class Await extends Call<Void> {
  private int waitingFor;
  private final Counter counter;

  Await(Counter counter, int waitingFor) {
    this.counter = counter;
    setUser("await");
  }

  public Void execute() {
    counter.await(waitingFor);
    return null;
  }

  public String toString() {
    return "await("+waitingFor+")";
  }
}
