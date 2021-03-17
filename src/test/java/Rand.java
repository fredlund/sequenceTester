package counter;

import es.upm.babel.sequenceTester.*;
import java.util.Random;


public class Rand extends CounterCall<Integer> {
  private Integer returnValue;

  Rand() {
    setUser("rand");
  }

  public void toTry() {
    returnValue = new Random().nextInt();
  }

  public Integer returnValue() {
    return returnValue;
  }

  public String toString() {
    return "rand()";
  }
}
