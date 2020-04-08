package counter;

import es.upm.babel.sequenceTester.*;


public class Wait extends CounterCall implements GetValue {
  private Object returnValue;

  Wait() {
    // Name of thread executing command
    setUser("wait");
  }

  public void toTry() {
    controller.whenEven();
  }

  public String toString() {
    return "whenEven()";
  }
}
