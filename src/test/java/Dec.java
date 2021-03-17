package counter;

import es.upm.babel.sequenceTester.*;


public class Dec extends CounterCall<Integer> {
  Dec() {
    setUser("dec");
  }

  public void toTry() {
    setReturnValue(controller.dec());
  }

  public String toString() {
    return "dec()";
  }
}
