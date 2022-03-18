package counter;

import es.upm.babel.sequenceTester.*;


public class Dec extends Call<Integer> {
  private final Counter counter;

  Dec(Counter counter) {
    this.counter = counter;
  }

  public Integer execute() {
    return counter.dec();
  }

  public String toString() {
    return "dec()";
  }
}
