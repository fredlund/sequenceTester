package counter;

import es.upm.babel.sequenceTester.*;


public class AssertIsEqual extends CounterCall implements GetValue {
  private int value;

  AssertIsEqual(int value) {
    this.value = value;
    setUser("assertIsEqual");
  }

  public void toTry() {
    controller.assertIsEqual(value);
  }

  public String toString() {
    return "assertIsEqual("+value+")";
  }
}
