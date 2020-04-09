package es.upm.babel.sequenceTester;

import es.upm.babel.cclib.Tryer;

/**
 * Implements a bit more logic on top of the cclib Tryer class.
 * Provides information whether a call has returned normally,
 * the return value, provides a feature (setController) to pass along an
 * object to all calls, and provides a feature to declare the origin
 * (process) of a call, to prevent new calls being made from a blocked user
 * (setUser).
 * This class should be extended in classes representing
 * the different methods calls of the API being tested.
 */
public abstract class BasicCall extends Tryer implements GetValue {
    private Object returnValue;
    private Object user;

  /**
   * Default constructor.
   */
    public BasicCall() { returnValue = null; }

  /**
   * Abstract method that should be implemented in an extended class.
   */
  public abstract void setController(Object controller);
      

  /**
   * Abstract method that executes the call (in extended classes).
   */
    public void toTry() throws Throwable { }

  /**
   * Returns the return value of the call (if any).
   */
    public Object returnValue() {
	return returnValue;
    }

  /**
   * Sets the return value of the call (if any).
   */
    public void setReturnValue(Object returnValue) {
	this.returnValue = returnValue;
    }

  /**
   * Checks whether the call returned normally. That is, it is not blocked
   * and the call did not raise an exception.
   */
    public boolean returned() {
	return !isBlocked() && !raisedException();
    }

  /**
   * Sets the user (process) executing a call.
   * The library enforces that if a call from a user is blocked, 
   * another call from the same user cannot be made.
   */
    public void setUser(Object user) {
	this.user = user;
    }

  /**
   * Returns the user of the call.
   */
    public Object user() {
	return user;
    }
}
