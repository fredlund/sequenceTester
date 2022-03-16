package counter;

import es.upm.babel.sequenceTester.*;


public class Await extends Call<Void> {
  private int waitingFor;
  private Counter counter;

  Await(Counter counter, int waitingFor) {
    this.counter = counter;
    setUser("await");
  }

  public void toTry() {
    counter.await(waitingFor);
  }

  public String toString() {
    return "await("+waitingFor+")";
  }
}
