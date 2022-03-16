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
  private static Map<String,Call> names = null;
  private static Random rand = new Random();

  String name;
  int id;
  boolean hasSymbolicName = false;
  boolean started = false;
  private Object user = null;
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
    this.name = newName();
    this.user = getUser();
    // By default we check that the call returns normally.
    this.waitTime = Config.getTestWaitTime();;
    addName(this.name,this);
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
   * A short name for the user method.
   */
  public Call<V> u(Object user) {
    return user(user);
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
   * Associates a symbolic name with a call.
   * The symbolic name
   * may be used in a continuation (in the TestStmt where the call resides)
   * to specify that this call was unblocked by a later call.
   */
  public Call<V> name(String name) {
    this.name = name;
    deleteName(this.name);
    addName(this.name,this);
    this.hasSymbolicName = true;
    return this;
  }

  /**
   * A short name for the name method.
   */
  public Call<V> n(String name) {
    return name(name);
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
   * Returns the symbolic name of the call
   */
  String getCallName() {
    return this.name;
  }

  public Call<V> exec() {
    execute(Arrays.asList(this));
    return this;
  }

  public Call<V> unblocks() {
    execute(Arrays.asList(this));
    Assertions.assertUnblocks(this);
    return this;
  }

  public Call<V> unblocks(Call... calls) {
    execute(Arrays.asList(this));    
    Assertions.assertUnblocks(calls);
    return this;
  }

  public Call<V> blocks() {
    execute(Arrays.asList(this));
    Assertions.assertBlocks();
    return this;
  }

  public Call<V> blocks(Call... calls) {
    execute(Arrays.asList(this));    
    Assertions.assertBlocks(calls);
    return this;
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

  static void execute(List<Call<?>> calls) {
    UnitTest t = calls.get(0).unitTest;
    // First check if any previous completed calls raised an exception which has not been handled
    if (t.unblockedCalls != null) checkExceptions(t.unblockedCalls);

    int maxWaitTime = 0;

    for (Call<?> call : calls) {
      maxWaitTime = Math.max(maxWaitTime, call.getWaitTime());
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

  /**
   * Returns the return value of the call (if any).
   */
  public V getReturnValue() {
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
    return hasStarted() && !hasBlocked() && !raisedException();
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
    if (!hasStarted())
        UnitTest.failTestSyntax("cannot check if call "+this+" is blocked because it has not started yet");
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
    names = new HashMap<String,Call>();
  }

  static void deleteName(String name) {
    names.remove(name);
  }

  static void addName(String name, Call call) {
    names.put(name,call);
  }

  static Call<?> byName(String name) {
    Call result = names.get(name);
    if (result == null) {
      UnitTest.failTestFramework("no call named "+name+" exists in\nmap="+names);
    }
    return result;
  }

  public static Object getReturnValue(String callName) {
    Call call = names.get(callName);
    
    if (call == null) {
      UnitTest.failTestFramework("no call named "+callName+" exists");
      return null;
    }

    if (!call.returned()) {
      UnitTest.failTestFramework("call "+callName+" has not returned");
      return null;
    }

    return call.getReturnValue();
  }
  
  public void setUnitTest(UnitTest unitTest) {
    this.unitTest = unitTest;
  }

  public UnitTest getUnitTest() {
    return unitTest;
  }

  public static Object v(String callName) {
    return getReturnValue(callName);
  }

  private String newName() {
    return "$call_"+Integer.valueOf(this.id).toString();
  }
}
