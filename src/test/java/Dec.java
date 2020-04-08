package counter;

import es.upm.babel.sequenceTester.*;


public class Dec extends CounterCall implements GetValue {
  private Object returnValue;

  Dec() {
    // Name of thread executing command
    setUser("dec");
  }

  public void toTry() {
    returnValue = controller.dec();
  }

  public Object returnValue() {
    return returnValue;
  }

  public String toString() {
    return "dec()";
  }
}
