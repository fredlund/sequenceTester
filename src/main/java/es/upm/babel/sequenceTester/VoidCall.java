package es.upm.babel.sequenceTester;

/**
 * Represents a call to an API, which can block, return a value or raise an exception.
 * Note that this class is meant to be extended by commands which do not return a value,
 * i.e., are void.
 */
public abstract class VoidCall extends Call<Void> {
  public final void toTry() throws Throwable {
    execute();
  }

  abstract public void execute() throws Throwable;
}
