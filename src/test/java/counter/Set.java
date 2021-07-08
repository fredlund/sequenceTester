package counter;

import es.upm.babel.sequenceTester.*;


public class Set extends CounterCall {
  private int value;

  Set(int value) {
    this.value = value;
    setUser("set");
  }

  public void toTry() {
    counter().set(value);
  }

  public String toString() {
    return "set("+value+")";
  }
}
