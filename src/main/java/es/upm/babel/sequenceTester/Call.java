package es.upm.babel.sequenceTester;

import java.util.Collection;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.io.StringWriter;
import java.io.PrintWriter;


/**
 * Represents a call to an API, which can block, return a value or raise an exception.
 * Methods permit to inspect the call to decide if its execution has terminated (unblocked),
 * and how it terminated (with an exception or a normal return).
 */
public abstract class Call<V> extends Tryer {
  private static int counter = 1;

  private int id;
  private boolean started = false;
  private Object user;
  private int waitTime;
  private UnitTest unitTest;
  private boolean hasReturnValue = false;
  private V returnValue = null;
  private boolean checkedForException = false;
  private Execute execute;

  /**
   * Constructs a call. Often this constructor should be 
   * extended in classes which extend the abstrac Call class.
   */
  public Call() {
    this.id = counter++;
    this.user = getUser();
    // By default we check that the call returns normally.
    this.waitTime = Config.getTestWaitTime();
    unitTest = UnitTest.getCurrentTest();
    unitTest.getAllCreatedCalls().add(this);
  }

  /**
   * Sets the user (process) executing a call.
   * The library enforces that if a call from a user is blocked,
   * another call from the same user cannot be made. Specifying the default nil
   * user prevents this check.
   */
  public Call<V> user(Object user) {
    return this;
  }

  /**
   * Sets the user (process) executing a call.
   * The library fails a tests if a call is made from a user 
   * who has another blocking call. Specifying the default nil
   * user prevents this check.
   */
  public void setUser(Object user) {
    this.user = user;
  }

  /**
   * Retrieves the user of a call.
   */
  public Object getUser() {
    return user;
  }

  /**
   * Sets wait time for calls until deciding they have blocked (in milliseconds)
   */
  public Call<V> waitTime(int milliSecs) {
    this.waitTime = milliSecs;
    return this;
  }

  /**
   * Sets wait time for calls until deciding they have blocked (in milliseconds)
   */
  public Call<V> w(int milliSecs) {
    return waitTime(milliSecs);
  }

  /**
   * Returns wait time for calls until deciding they have blocked (in milliseconds)
   */
  public int getWaitTime() {
    return this.waitTime;
  }

  void checkedForException() {
    checkedForException = true;
  }

  static void checkExceptions(Set<Call<?>> calls, boolean calledFromAfter) {
    for (Call<?> call : calls) {
      if (call.raisedException() && !call.checkedForException) {
        Throwable exc = call.getException();
        StringWriter errors = new StringWriter();
        exc.printStackTrace(new PrintWriter(errors));
        String StackTrace = errors.toString();

	String msg = "the call to "+call+" raised an exception "+exc+"\nStacktrace:\n"+StackTrace+"\n";
	if (calledFromAfter) {
	  // Note that we have to fill in the execution trace here since this
	  // fail (in @AfterEach) does not seem to be caught by the exception handler
	  UnitTest.failTest(msg, true, UnitTest.ErrorLocation.LASTLINE);
	} else UnitTest.failTest(msg);
      }
    }
  }

  /**
   * Executes the call. The method waits a fixed interval of time before returning.
   */
   void makeCall() {
    started = true;
    start();
  }

  // If a call is not executing force it to execute
  private void forceExecute() {
    if (!hasStarted())
      Execute.exec(this);
  }

  public String printCall() {
    return id+": "+ this;
  }

  public static String printCalls(Collection<Call<?>> calls) {
    String callsString="";
    for (Call<?> call : calls) {
      if (callsString != "") callsString += "\n  "+call.printCall();
      else callsString = call.printCall();
    }
    return callsString;
  }

  public String printCallWithReturn() {
    String callString = printCall();
    if (raisedException())
      return callString + " raised " + getException();
    else {
      if (hasReturnValue())
        return callString + " returned " + getReturnValue();
      else
        return callString;
    }
  }

  /**
   * Returns true if the execution of the call has started.
   */
  public boolean hasStarted() {
    return started;
  }

  /**
   * Returns true if the call is blocked and false otherwise.
   * If the call has not yet started executing this method forces its execution.
   */
  public boolean blocked() {
    forceExecute();
    // In the "current" cclib a tryer may be:
    //  blocked (tryer.isBlocked())
    // - blocked because it terminated with an exception
    // (tryer.raisedException())
    // - or not blocked because it terminated normally.
    // In the code below we instead consider a call blocked
    // if the call truly blocked AND it did not raise an
    // exception.
    return isBlocked() && !raisedException();
  }

  /**
   * Returns true if the call is unblocked, i.e., either it has returned normally
   * or has raised an exception.
   * If the call has not yet started executing this method forces its execution.
   */
  public Call<V> unblocked() {
    if (!returned() && !raisedException())
      UnitTest.failTest(this+" is not unblocked");
    return this;
  }

  /**
   * Returns true if the call raised an exception.
   * If the call has not yet started executing this method forces its execution.
   */
  public Call<V> raised() {
    if (!raisedException())
      UnitTest.failTest(this+" did not raise an exception");
    return this;
  }

  /**
   * Returns true if the call returned normally, i.e., did not raise an exception.
   * If the call has not yet started executing this method forces its execution.
   */
  public boolean returned() {
    forceExecute();
    return hasStarted() && !blocked() && !raisedException();
  }

  /**
   * Returns the return value of the call (if any). If the call is still blocked,
   * or if the call raised an exception, or if the call did not return any value,
   * the method fails.
   * If the call has not yet started executing this method forces its execution.
   */
  public V getReturnValue() {
    forceExecute();
    if (!hasReturnValue)
      UnitTest.failTest(this+" did not return a value");
    return returnValue;
  }

  /**
   * Returns the return value of the call (if any). If the call is still blocked,
   * or if the call raised an exception, or if the call did not return any value,
   * the method fails.
   * If the call has not yet started executing this method forces its execution.
   */
  public Throwable getException() {
    forceExecute();
    if (!raisedException())
      UnitTest.failTest(this+" did not return a value");
    return super.getException();
  }

  /**
   * Checks if the call returned a value. 
   * If the call has not yet started executing this method forces its execution.
   */
  public boolean hasReturnValue() {
    forceExecute();
    return hasReturnValue;
  }

  /**
   * Sets the return value of the call (if any).
   */
  private void setReturnValue(V returnValue) {
    this.hasReturnValue = true;
    this.returnValue = returnValue;
  }

  public int hashCode() {
    return id;
  }

  public boolean equals(Object obj) {
    if (obj instanceof Call) {
      Call otherCall = (Call) obj;
      return id == otherCall.id;
    } else return false;
  }

  static void reset() {
    counter = 1;
  }

  UnitTest getUnitTest() {
    return unitTest;
  }

  void setExecute(Execute e) {
    execute = e;
  }

  Execute getExecute() {
    return execute;
  }

  public final void toTry() throws Throwable {
    setReturnValue(execute());
  }

  abstract public V execute() throws Throwable;
}
