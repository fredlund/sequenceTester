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
 * Represents a call to an API, which can block, return a value or raise an exception.
 * Methods permit to inspect the call to decide if its execution has terminated (unblocked),
 * and how it terminated (with an exception or a normal return).
 * Note that a number of predicates, e.g., call.unblocks(...) and call.blocks(...), are defined relative
 * to its call argument. That is, they check that immediately after executing call,
 * the call itself was unblocked or blocked, and the arguments calls were all unblocked.
 * Thus, suppose that initially call blocks, e.g., call.blocking(...) is true,
 * and that later another call' unblocks call, then call.blocking(...) remains true.
 */
public abstract class Call<V> extends Tryer {
  private static int counter = 1;
  private static final Random rand = new Random();

  private int id;
  private boolean started = false;
  private Object user;
  private int waitTime;
  private UnitTest unitTest;
  private boolean hasReturnValue = false;
  private V returnValue = null;
  private boolean checkedForException = false;
  private Set<Call<?>> unblockedCalls;
  private Set<Call<?>> blockedCalls;
  private List<Call<?>> partnerCalls;

  /**
   * Constructs a call. Often this constructor should be 
   * extended in classes which extend the abstrac Call class.
   */
  public Call() {
    this.id = counter++;
    this.user = getUser();
    // By default we check that the call returns normally.
    this.waitTime = Config.getTestWaitTime();
    unitTest = UnitTest.currentTest;
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
   * Commences the execution of the call.
   */
  public Call<V> exec() {
    exec(Arrays.asList(this));
    return this;
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

  public void checkedForException() {
    checkedForException = true;
  }

  protected static void checkExceptions(Set<Call<?>> calls, boolean calledFromAfter) {
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

  public static void exec(Call... calls) {
    exec(Arrays.asList(calls));
  }

  public static void exec(List<Call<?>> calls) {
    if (calls.size() == 0) UnitTest.failTestSyntax("trying to execute 0 calls", UnitTest.ErrorLocation.AFTER);
    UnitTest t = calls.get(0).unitTest;
    
    // First check if any previous completed calls raised an exception which has not been handled
    if (t.getAllUnblockedCalls() != null) checkExceptions(t.getAllUnblockedCalls(), false);

    // Next check if there are if a user in the new calls is blocked
    Set<Object> blockedUsers = new HashSet<>();
    for (Call<?> call : t.getBlockedCalls()) {
      Object user = call.getUser();
      if (user != null) blockedUsers.add(user);
    }
    for (Call<?> call : calls) {
      Object user = call.getUser();
      if (user != null && blockedUsers.contains(user)) {
        UnitTest.failTestSyntax("user "+user+" is blocked in call "+call, UnitTest.ErrorLocation.AFTER);
      }
    }

    int maxWaitTime = 0;

    for (Call<?> call : calls) {
      maxWaitTime = Math.max(maxWaitTime, call.getWaitTime());
    }

    t.prepareToRun(calls);
    runCalls(calls);

    // Busywait a while until either we wait the maxWaitTime, or all active
    // calls have been unblocked
    long remainingTime = maxWaitTime;
    do {
      long waitTime = Math.min(remainingTime, 10);
      try { Thread.sleep(waitTime); }
      catch (InterruptedException exc) { }
      // Compute unblocked (and change blockedCalls)
      t.calculateUnblocked();
      remainingTime -= waitTime;
    } while (t.hasBlockedCalls() && remainingTime > 0);

    for (Call<?> call : calls) {
      call.unblockedCalls = new HashSet<>(t.getLastUnblockedCalls());
      call.blockedCalls = new HashSet<>(t.getBlockedCalls());
      call.partnerCalls = calls;
    }
    t.extendTrace(calls, t.getLastUnblockedCalls());
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
    return id+": "+ this;
  }

  public Set<Call<?>> getUnblockedCalls() {
    forceExecute();
    return unblockedCalls;
  }

  public Set<Call<?>> getBlockedCalls() {
    forceExecute();
    return blockedCalls;
  }

  public List<Call<?>> getPartnerCalls() {
    forceExecute();
    return partnerCalls;
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
    if (!hasStarted())
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

  public void setUnitTest(UnitTest unitTest) {
    this.unitTest = unitTest;
  }

  public UnitTest getUnitTest() {
    return unitTest;
  }

  public void toTry() throws Throwable {
    setReturnValue(execute());
  }

  abstract public V execute() throws Throwable;
}
