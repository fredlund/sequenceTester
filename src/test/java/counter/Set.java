package counter;

import es.upm.babel.sequenceTester.*;


public class Set extends Call<Void> {
  private final int value;
  private final Counter counter;

  Set(Counter counter, int value) {
    this.counter = counter;
    this.value = value;
  }

  public void toTry() {
    counter.set(value);
  }

  public String toString() {
    return "set("+value+")";
  }
}
