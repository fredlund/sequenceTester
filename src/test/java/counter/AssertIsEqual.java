package counter;

import es.upm.babel.sequenceTester.*;


public class AssertIsEqual extends VoidCall {
  private final int value;
  private final Counter counter;

  AssertIsEqual(Counter counter, int value) {
    this.counter = counter;
    this.value = value;
  }

  public void execute() {
    counter.assertIsEqual(value);
  }

  public String toString() {
    return "assertIsEqual("+value+")";
  }
}
