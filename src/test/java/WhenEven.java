package counter;

import es.upm.babel.sequenceTester.*;


public class WhenEven extends CounterCall implements GetValue {
  private Object returnValue;

  WhenEven() {
    // Name of thread executing command
    setUser("whenEven");
  }

  public void toTry() {
    returnValue = controller.whenEven();
  }

  public Object returnValue() {
    return returnValue;
  }

  public String toString() {
    return "whenEven()";
  }
}
