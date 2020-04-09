package counter;

import es.upm.babel.sequenceTester.*;


public class Set extends CounterCall implements GetValue {
  private int value;

  Set(int value) {
    this.value = value;
    setUser("set");
  }

  public void toTry() {
    controller.set(value);
  }

  public String toString() {
    return "set("+value+")";
  }
}
