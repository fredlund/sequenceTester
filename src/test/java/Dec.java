package counter;

import es.upm.babel.sequenceTester.*;


public class Dec extends CounterCall<Integer> {
  private Integer returnValue;

  Dec() {
    setUser("dec");
  }

  public void toTry() {
    returnValue = controller.dec();
  }

  public Integer returnValue() {
    return returnValue;
  }

  public String toString() {
    return "dec()";
  }
}
