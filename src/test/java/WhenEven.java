package counter;

import es.upm.babel.sequenceTester.*;


public class WhenEven extends CounterCall {
  private Object returnValue;

  WhenEven() {
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
