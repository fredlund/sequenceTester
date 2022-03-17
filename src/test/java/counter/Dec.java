package counter;

import es.upm.babel.sequenceTester.*;


public class Dec extends Call<Integer> {
  private Counter counter;

  Dec(Counter counter) {
    this.counter = counter;
  }

  public void toTry() {
    setReturnValue(counter.dec());
  }

  public String toString() {
    return "dec()";
  }
}
