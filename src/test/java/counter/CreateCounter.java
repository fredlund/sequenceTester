package counter;

import es.upm.babel.sequenceTester.*;


public class CreateCounter extends Call<Counter> {
  CreateCounter() {
    setUser("createCounter");
  }

  public void toTry() {
    Counter counter = new Counter();
    setReturnValue(counter);
  }

  public String toString() {
    return "createCounter()";
  }
}
