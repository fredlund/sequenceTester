package counter;

import es.upm.babel.sequenceTester.*;
import java.util.Random;


public class Rand extends CounterCall<Integer> {
  Rand() {
    setUser("rand");
  }

  public void toTry() {
    setReturnValue(new Random().nextInt());
  }

  public String toString() {
    return "rand()";
  }
}
