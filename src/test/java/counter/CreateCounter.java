package counter;

import es.upm.babel.sequenceTester.*;


public class CreateCounter extends CounterCall<Counter> {
  CreateCounter() {
    setUser("createCounter");
  }

  public void toTry() {
    Counter counter = new Counter();
    setTestState(counter);
    setReturnValue(counter);
  }

  public String toString() {
    return "createCounter()";
  }
}
