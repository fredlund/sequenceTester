package counter;

import es.upm.babel.sequenceTester.*;


public class WhenEven extends ReturningCall<Integer> {
  private final Counter counter;

  WhenEven(Counter counter) {
    this.counter = counter;
  }

  public Integer execute() {
    return counter.whenEven();
  }

  public String toString() {
    return "whenEven()";
  }
}
