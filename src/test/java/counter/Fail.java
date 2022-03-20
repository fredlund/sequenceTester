package counter;

import es.upm.babel.sequenceTester.*;


public class Fail extends VoidCall {
  Fail() { }

  public void execute() {
    throw new RuntimeException();
  }

  public String toString() {
    return "fail()";
  }
}
