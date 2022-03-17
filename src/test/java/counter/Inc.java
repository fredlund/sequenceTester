package counter;

import es.upm.babel.sequenceTester.*;


public class Inc extends Call<Integer> {
  private final Counter counter;

  Inc(Counter counter) {
    this.counter = counter;
  }

  public void toTry() {
    setReturnValue(counter.inc());
  }

  public String toString() {
    return "inc()";
  }
}
