package counter;

import es.upm.babel.sequenceTester.*;


public class Dec extends CounterCall {
  private Object returnValue;

  Dec() {
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
