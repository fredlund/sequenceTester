package es.upm.babel.sequenceTester;

/**
 * Represents a call to an API, which can block, return a value or raise an exception.
 * Note that this class is meant to be extended by commands which returns a value,
 * i.e., are non-void.
 */
public abstract class ReturningCall<V> extends Call<V> {
  public final void toTry() throws Throwable {
    setReturnValue(execute());
  }

  abstract public V execute() throws Throwable;
}
