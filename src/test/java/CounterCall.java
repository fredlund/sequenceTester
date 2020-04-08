package counter;

import es.upm.babel.sequenceTester.*;


public class CounterCall extends BasicCall {
  Counter controller;

  public CounterCall() { }

  public void setController(Object controller) {
    if (controller instanceof Counter)
      this.controller = (Counter) controller;
    else
      throw new RuntimeException();
  }
}
