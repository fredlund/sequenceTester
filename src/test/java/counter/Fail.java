package counter;

import es.upm.babel.sequenceTester.*;


public class Fail extends Call<Integer> {
  Fail() { }

  public Integer execute() {
    throw new RuntimeException();
  }

  public String toString() {
    return "fail()";
  }
}
