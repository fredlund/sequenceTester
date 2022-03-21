package counter;

import es.upm.babel.sequenceTester.*;


public class IsEven extends ReturningCall<Boolean> {
  private final int n;

  IsEven(int n) {
    this.n = n;
  }

  public Boolean execute() {
    return (n % 2) == 0;
  }

  public String toString() {
    return "isEven("+n+")";
  }
}
