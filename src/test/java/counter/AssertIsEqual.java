package counter;

import es.upm.babel.sequenceTester.*;


public class AssertIsEqual extends Call<Void> {
  private int value;
  private Counter counter;

  AssertIsEqual(Counter counter, int value) {
    this.counter = counter;
    this.value = value;
    setUser("assertIsEqual");
  }

  public void toTry() {
    counter.assertIsEqual(value);
  }

  public String toString() {
    return "assertIsEqual("+value+")";
  }
}
