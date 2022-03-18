package counter;

import es.upm.babel.sequenceTester.*;


public class AssertIsEqual extends Call<Void> {
  private final int value;
  private final Counter counter;

  AssertIsEqual(Counter counter, int value) {
    this.counter = counter;
    this.value = value;
  }

  public Void execute() {
    counter.assertIsEqual(value);
    return null;
  }

  public String toString() {
    return "assertIsEqual("+value+")";
  }
}
