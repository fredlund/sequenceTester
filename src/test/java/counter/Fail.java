package counter;

import es.upm.babel.sequenceTester.*;


public class Fail extends Call<Integer> {
  Fail() { }

  public void toTry() {
    throw new RuntimeException();
  }

  public String toString() {
    return "fail()";
  }
}
