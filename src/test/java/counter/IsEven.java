package counter;

import es.upm.babel.sequenceTester.*;


public class IsEven extends CounterCall<Boolean> {
  private int n;

  IsEven(int n) {
    this.n = n;
    setUser("isEven");
  }

  public void toTry() {
    setReturnValue((n % 2) == 0);
  }

  public String toString() {
    return "isEven("+n+")";
  }
}
