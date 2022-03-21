package counter;

import es.upm.babel.sequenceTester.*;


public class CreateCounter extends ReturningCall<Counter> {
  CreateCounter() { }

  public Counter execute() {
    return new Counter();
  }

  public String toString() {
    return "createCounter()";
  }
}
