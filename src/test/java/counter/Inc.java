package counter;

import es.upm.babel.sequenceTester.*;


public class Inc extends Call<Integer> {
  private Counter counter;

  Inc(Counter counter) {
    this.counter = counter;
    setUser("inc");
  }

  public void toTry() {
    setReturnValue(counter.inc());
  }

  public String toString() {
    return "inc()";
  }
}
