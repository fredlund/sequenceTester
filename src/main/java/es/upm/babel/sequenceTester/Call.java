package es.upm.babel.sequenceTester;

import es.upm.babel.cclib.Tryer;

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
   * another call from the same user cannot be made.
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

  /**
   * Checks that the call has unblocked as a result of its execution, 
   * and that no other call was unblocked as a result
   * of executing the call (and its sibling calls).
   * Fails a test if the call did not unblock, or some other call unblocked.
   * If the call has not started executing, this method forces its execution.
   */
  public Call<V> unblocks() {
    forceExecute();
    SeqAssertions.assertUnblocks(this);
    return this;
  }

  /**
   * Checks that the call has unblocked, as a result of executing the call, and that precisely the
   * calls enumerated in the parameter list are the only additional calls that have unblocked
   * as a result of executing the call (and its sibling calls).
   * Fails a test if the call did not unblock, the parameter list calls did not all unblock,
   * or if some other call unblocked.
   * If the call has not started executing, this method forces its execution.
   */
  public Call<V> unblocks(Call... calls) {
    forceExecute();
    ArrayList<Call<?>> mustBlocks = new ArrayList<>();
    boolean addedThis = false;
    for (Call call : calls) {
      mustBlocks.add(call);
      addedThis = addedThis || call==this;
    }
    if (!addedThis) mustBlocks.add(this);
    SeqAssertions.assertBlocking(mustBlocks,Arrays.asList());
    return this;
  }

  /**
   * Checks that the call was blocked after executing it and its sibling calls,
   * and moreover that no other call has unblocked.
   * Fails a test if the call unblocked, or some other call unblocked.
   * If the call has not started executing, this method forces its execution.
   */
  public Call<V> blocks() {
    forceExecute();
    SeqAssertions.assertBlocks();
    return this;
  }

  /**
   * Checks that the call was blocked after its execution, and that precisely the calls in 
   * the parameter list are the only calls that have unblocked as a result of executing
   * the call and its sibling calls.
   * Fails a test if the call unblocked, a call in the parameter list is blocked,
   * or some call not in the parameter list have unblocked.
   * If the call has not started executing, this method forces its execution.
   */
  public Call<V> blocks(Call... calls) {
    forceExecute();
    SeqAssertions.assertBlocks(calls);
    return this;
  }

  /**
   * Checks whether the call raised an exception. 
   * If the call has not started executing, this method forces its execution.
   * If the call is still blocked, a test failure is indicated.
   */
  public boolean raisedException() {
    forceExecute();
    return super.raisedException();
  }

  /**
   * Checks whether the call raised an exception. 
   * If the call has not started executing, this method forces its execution.
   * If the call is still blocked, a test failure is indicated.
   */
  public Throwable getException() {
    forceExecute();
    if (!raisedException())
      UnitTest.failTest(this+" did not raise an exception");
    return super.getException();
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

  public Call<V> raises() {
    if (!raisedException())
      UnitTest.failTest(this+" did not raise an exception");
    return this;
  }

  public Call<V> returns() {
    if (!returned())
      UnitTest.failTest(this+" did not return normally");
    return this;
  }

  /**
   * Returns the return value of the call (if any).
   */
  public V getReturnValue() {
    forceExecute();
    if (!hasReturnValue)
      UnitTest.failTest(this+" did not return a value");
    return returnValue;
  }

  /**
   * Sets the return value of the call (if any).
   */
  private void setReturnValue(V returnValue) {
    this.hasReturnValue = true;
    this.returnValue = returnValue;
  }

  /**
   * Checks whether the call returned normally. That is, it is not blocked
   * and the call did not raise an exception.
   */
  public boolean returned() {
    forceExecute();
    return hasStarted() && !hasBlocked() && !raisedException();
  }

  // If a call is not executing force it to execute
  private void forceExecute() {
    if (!hasStarted())
      Execute.exec(this);
  }

  /**
   * Returns true if the execution of the call has started.
   */
  public boolean hasStarted() {
    return started;
  }

  public boolean hasReturnValue() {
    return hasReturnValue;
  }

  public boolean hasBlocked() {
    /**
     * In the "current" cclib a tryer may be:
     * - blocked (tryer.isBlocked())
     * - blocked because it terminated with an exception
     * (tryer.raisedException())
     * - or not blocked because it terminated normally.
     *
     * In the code below we instead consider a call blocked
     * if the call truly blocked AND it did not raise an
     * exception.
     **/
    forceExecute();
    return isBlocked() && !raisedException();
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

  void setUnitTest(UnitTest unitTest) {
    this.unitTest = unitTest;
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
