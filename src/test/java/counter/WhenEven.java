package counter;

import es.upm.babel.sequenceTester.*;


public class WhenEven extends Call<Integer> {
  private Counter counter;

  WhenEven(Counter counter) {
    this.counter = counter;
  }

  public void toTry() {
    setReturnValue(counter.whenEven());
  }

  public String toString() {
    return "whenEven()";
  }
}
