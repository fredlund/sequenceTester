package counter;

import es.upm.babel.sequenceTester.*;


public class WhenEven extends CounterCall<Integer> {

  WhenEven() {
    setUser("whenEven");
  }

  public void toTry() {
    setReturnValue(controller.whenEven());
  }

  public String toString() {
    return "whenEven()";
  }
}
