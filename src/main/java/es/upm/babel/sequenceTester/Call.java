package es.upm.babel.sequenceTester;

import es.upm.babel.cclib.Tryer;

import java.util.Random;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;


/**
 * Represents a BasicCall together with an oracle for deciding if the call
 * executed correctly.
 */
public abstract class Call<V> extends Tryer {
  private static int counter = 1;
  private static Map<String,Call> names = null;
  private static Random rand = new Random();

  String name;
  int id;
  boolean hasSymbolicName = false;
  Oracle<V> oracle;
  boolean started = false;
  private Object user = null;
  private Return<V> returner;
  private int waitTime;
  private UnitTest unitTest;

  /**
   * Constructs a call. A call consists of a recipe for making a call,
   * and optionally a oracle that decides if an invocation of the call returned the
   * correct result, and optionally a symbolic name for the call.
   */
  public Call() {
    this.id = counter++;
    this.name = newName();
    this.user = getUser();
    // By default we check that the call returns normally.
    this.oracle = Check.returns();
    this.waitTime = Config.getTestWaitTime();;
    this.returner = new Return<>();
    addName(this.name,this);
  }

  /**
   * Associates an oracle with a call.
   * @param oracle an oracle which decides if the call returned the correct value.
   */
  public Call<V> oracle(Oracle<V> oracle) {
    this.oracle = oracle;
    return this;
  }

  /**
   * Provides a short name for the oracle method.
   */
  public Call<V> o(Oracle<V> oracle) {
    return oracle(oracle);
  }

  /**
   * Returns the oracle of the call (otherwise null).
   */
  public Oracle<V> getOracle() {
    return oracle;
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
   * Sets the returner for the call.
   */
  public Call<V> returnsTo(Return<V> returner) {
    this.returner = returner;
    return this;
  }

  /**
   * A short name for the returner method.
   */
  public Call<V> r(Return<V> returner) {
    return returnsTo(returner);
  }

  /**
   * Returns the return value of the call (if any).
   */
  public V returnValue() {
    return returner.getReturnValue();
  }

  /**
   * Sets the return value of the call (if any).
   */
  public void setReturnValue(V returnValue) {
    returner.setReturnValue(returnValue);
  }

  /**
   * Checks whether the call returned normally. That is, it is not blocked
   * and the call did not raise an exception.
   */
  public boolean returned() {
    return hasStarted() && !hasBlocked() && !raisedException();
  }

  /**
   * Returns the symbolic name of the call
   */
  String getSymbolicName() {
    return this.name;
  }

  public void execute(UnitTest unitTest) {
    this.unitTest = unitTest;
    makeCall();

    // Busywait a while until either we wait the maxWaitTime, or the calls has been unblocked
    long remainingTime = getWaitTime();;
    do {
      long waitTime = Math.min(remainingTime, 10);
      try { Thread.sleep(waitTime); }
      catch (InterruptedException exc) { };
      remainingTime -= waitTime;
    } while (hasBlocked() && remainingTime > 0);
  }

  static Set<Call<?>> execute(List<Call<?>> calls, UnitTest unitTest, Set<Call<?>> allCalls, Set<Call<?>> blockedCalls) {
    int maxWaitTime = 0;

    for (Call<?> call : calls) {
      maxWaitTime = Math.max(maxWaitTime, call.getWaitTime());
    }

    for (Call<?> call : calls) {
      call.setUnitTest(unitTest);
      allCalls.add(call);
      blockedCalls.add(call);
    }

    runCalls(calls);

    Set<Call<?>> unblocked = new HashSet<Call<?>>();
    
    // Busywait a while until either we wait the maxWaitTime, or all active
    // calls have been unblocked
    long remainingTime = maxWaitTime;
    do {
      long waitTime = Math.min(remainingTime, 10);
      try { Thread.sleep(waitTime); }
      catch (InterruptedException exc) { };
      // Compute unblocked (and change blockedCalls)
      Util.unblocked(blockedCalls,unblocked);
      remainingTime -= waitTime;
    } while (blockedCalls.size() > 0 && remainingTime > 0);
    return unblocked;
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

  public void setTestState(Object state) {
    this.unitTest.setTestState(state);
  }

  public Object getTestState() {
    return this.unitTest.getTestState();
  }

  /**
   * Returns true if the execution of the call has started.
   */
  public boolean hasStarted() {
    return started;
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

  public static String printCalls(List<Call<?>> calls) {
    if (calls.size() == 1)
      return calls.get(0).printCall();
    else {
      String callsString="";
      for (Call call : calls) {
        if (callsString != "") callsString += "\n  "+call.printCall();
        else callsString = call.printCall();
      }
      return callsString;
    }
  }

  public boolean hasReturnValue() {
    return returner.hasReturnValue();
  }

  public String printCallWithReturn() {
    String callString = printCall();
    if (raisedException())
      return callString + " raised " + getException();
    else {
      if (hasReturnValue())
        return callString + " returned " + returner.getReturnValue();
      else
        return callString;
    }
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

  static Call lookupCall(String name) {
    Call result = names.get(name);
    if (result == null) {
      UnitTest.failTestFramework("no call named "+name+" exists in\nmap="+names);
    }
    return result;
  }

  public static Object returnValue(String callName) {
    Call call = names.get(callName);
    
    if (call == null) {
      UnitTest.failTestFramework("no call named "+callName+" exists");
      return null;
    }

    if (!call.returned()) {
      UnitTest.failTestFramework("call "+callName+" has not returned");
      return null;
    }

    return call.returnValue();
  }
  
  public void setUnitTest(UnitTest unitTest) {
    this.unitTest = unitTest;
  }

  public UnitTest getUnitTest() {
    return unitTest;
  }

  public static Object v(String callName) {
    return returnValue(callName);
  }

  private String newName() {
    return "$call_"+Integer.valueOf(this.id).toString();
  }
}
