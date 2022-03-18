package counter;

import es.upm.babel.sequenceTester.*;
import java.util.Random;


public class Rand extends Call<Integer> {
  Rand() { }

  public Integer execute() {
    return new Random().nextInt();
  }

  public String toString() {
    return "rand()";
  }
}
