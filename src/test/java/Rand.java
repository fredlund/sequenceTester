package counter;

import es.upm.babel.sequenceTester.*;
import java.util.Random;


public class Rand extends CounterCall {
  private Object returnValue;

  Rand() {
    setUser("rand");
  }

  public void toTry() {
    returnValue = new Random().nextInt();
  }

  public Object returnValue() {
    return returnValue;
  }

  public String toString() {
    return "set("+value+")";
  }
}
