package es.upm.babel.sequenceTester;

/**
 * Represents a call to an API which may block.
 * Note that this class is meant to be extended by commands which do not return a value,
 * i.e., are void.
 */
public abstract class VoidCall extends Call<Void> {
  public void toTry() throws Throwable {
    execute();
  }

  /**
   * Performs the actions of the call.
   */
  abstract public void execute() throws Throwable;
}
