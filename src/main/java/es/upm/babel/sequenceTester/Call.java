package es.upm.babel.sequenceTester;

import es.upm.babel.cclib.Tryer;

import java.util.Collection;
import java.util.Random;
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
 * A Call.
 */
public abstract class Call<V> extends Tryer {
  private static int counter = 1;
  private static Random rand = new Random();

  int id;
  boolean hasSymbolicName = false;
  boolean started = false;
  boolean executing = false;
  private Object user;
  private int waitTime;
  private UnitTest unitTest;
  private boolean hasReturnValue = false;
  private V returnValue = null;
  private boolean checkedForException = false;

  /**
   * Constructs a call. A call consists of a recipe for making a call,
   * and optionally a symbolic name for the call.
   */
  public Call() {
    this.id = counter++;
    this.user = getUser();
    // By default we check that the call returns normally.
    this.waitTime = Config.getTestWaitTime();;
    unitTest = UnitTest.currentTest;
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
   * The library enforces that if a call from a user is blocked,
   * another call from the same user cannot be made.
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

  public Call<V> exec() {
    execute(Arrays.asList(this));
    return this;
  }

  public Call<V> unblocks() {
    forceExecute();
    Assertions.assertUnblocks(this);
    return this;
  }

  public Call<V> unblocks(Call... calls) {
    forceExecute();
    ArrayList<Call<?>> mustBlocks = new ArrayList<>();
    boolean addedThis = false;
    for (Call call : calls) {
      mustBlocks.add(call);
      addedThis = addedThis || call==this;
    }
    if (!addedThis) mustBlocks.add(this);
    Assertions.assertBlocking(mustBlocks,Arrays.asList());
    return this;
  }

  public Call<V> blocks() {
    forceExecute();
    Assertions.assertBlocks();
    return this;
  }

  public Call<V> blocks(Call... calls) {
    forceExecute();
    Assertions.assertBlocks(calls);
    return this;
  }

  public boolean raisedException() {
    forceExecute();
    return super.raisedException();
  }

  public Throwable getException() {
    forceExecute();
    if (!raisedException())
      UnitTest.failTest(this+" did not raise an exception");
    return super.getException();
  }

  public void checkedForException() {
    checkedForException = true;
  }

  public static void checkExceptions(Set<Call<?>> calls) {
    for (Call<?> call : calls) {
      if (call.raisedException() && !call.checkedForException) {
        Throwable exc = call.getException();
        StringWriter errors = new StringWriter();
        exc.printStackTrace(new PrintWriter(errors));
        String StackTrace = errors.toString();
        UnitTest.failTest("the call to "+call+" raised an exception "+exc+"\nStacktrace:\n"+StackTrace+"\n");
      }
    }
  }

  public static void execute(Call... calls) {
    execute(Arrays.asList(calls));
  }

  public static void execute(List<Call<?>> calls) {
    UnitTest t = calls.get(0).unitTest;
    // First check if any previous completed calls raised an exception which has not been handled
    if (t.unblockedCalls != null) checkExceptions(t.unblockedCalls);

    int maxWaitTime = 0;

    for (Call<?> call : calls) {
      maxWaitTime = Math.max(maxWaitTime, call.getWaitTime());
      call.executing = true;
    }

    t.addCalls(calls);
    t.resetUnblocked();
    t.calls = new HashSet<Call<?>>(calls);

    runCalls(calls);

    // Busywait a while until either we wait the maxWaitTime, or all active
    // calls have been unblocked
    long remainingTime = maxWaitTime;
    do {
      long waitTime = Math.min(remainingTime, 10);
      try { Thread.sleep(waitTime); }
      catch (InterruptedException exc) { };
      // Compute unblocked (and change blockedCalls)
      t.calculateUnblocked();
      remainingTime -= waitTime;
    } while (t.hasBlockedCalls() && remainingTime > 0);

    t.extendTrace(calls, t.unblockedCalls());
  }

  static void runCalls(List<Call<?>> calls) {
    boolean randomize = Config.getTestRandomize();
    List<Call<?>> callsInOrder = calls;

    // Check if the starting order of calls should be randomized
    if (randomize) {
      callsInOrder = new ArrayList<>();
      ArrayList<Call<?>> copiedCalls = new ArrayList<>(calls);
      int remaining = copiedCalls.size();
      while (remaining > 0) {
        int nextToTake = rand.nextInt(remaining);
        callsInOrder.add(copiedCalls.get(nextToTake));
        copiedCalls.remove(nextToTake);
        --remaining;
      }
    }

    for (Call<?> call : callsInOrder) {
      call.makeCall();
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
    return id+":"+this.toString();
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
  public void setReturnValue(V returnValue) {
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
    if (!executing)
      exec();
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
    //return getSymbolicName().hashCode();
  }

  public boolean equals(Object obj) {
    if (obj instanceof Call) {
      Call otherCall = (Call) obj;
      return id == otherCall.id;
      //return getSymbolicName().equals(otherCall.getSymbolicName());
    } else return false;
  }

  static void reset() {
    counter = 1;
  }

  public void setUnitTest(UnitTest unitTest) {
    this.unitTest = unitTest;
  }

  public UnitTest getUnitTest() {
    return unitTest;
  }

}
