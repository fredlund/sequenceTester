package counter;

import es.upm.babel.sequenceTester.*;


public class WhenEven extends CounterCall<Integer> {

  WhenEven() {
    setUser("whenEven");
  }

  public void toTry() {
    setReturnValue(counter().whenEven());
  }

  public String toString() {
    return "whenEven()";
  }
}
