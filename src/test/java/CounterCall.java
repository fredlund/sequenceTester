package counter;

import es.upm.babel.sequenceTester.*;


public abstract class CounterCall<E> extends Call<E> {
  public CounterCall() { }

  public Counter counter() {
    Object state = getTestState();

    if (state instanceof Counter)
      return (Counter) state;
    else
      throw new RuntimeException();
  }
}

