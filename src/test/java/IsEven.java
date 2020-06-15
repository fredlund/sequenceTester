package counter;

import es.upm.babel.sequenceTester.*;


public class IsEven extends CounterCall {
  private int n;
  private boolean returnValue;

  IsEven(int n) {
    this.n = n;
    setUser("isEven");
  }

  public void toTry() {
    returnValue = (n % 2) == 0;
  }

  public Object returnValue() {
    return returnValue;
  }

  public String toString() {
    return "isEven("+n+")";
  }
}
