package counter;

import es.upm.babel.sequenceTester.*;


public class Set extends Call<Void> {
  private final int value;
  private final Counter counter;

  Set(Counter counter, int value) {
    this.counter = counter;
    this.value = value;
  }

  public Void execute() {
    counter.set(value);
    return null;
  }

  public String toString() {
    return "set("+value+")";
  }
}
