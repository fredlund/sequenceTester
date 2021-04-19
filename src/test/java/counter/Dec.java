package counter;

import es.upm.babel.sequenceTester.*;


public class Dec extends CounterCall<Integer> {
  Dec() {
    setUser("dec");
  }

  public void toTry() {
    setReturnValue(counter().dec());
  }

  public String toString() {
    return "dec()";
  }
}
