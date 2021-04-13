package counter;

import es.upm.babel.sequenceTester.*;


public abstract class CounterCall<E> extends Call<E> {
  Counter controller;

  public CounterCall() { }

  public void setController(Object controller) {
    if (controller instanceof Counter)
      this.controller = (Counter) controller;
    else
      throw new RuntimeException();
  }
}

