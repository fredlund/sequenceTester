package es.upm.babel.sequenceTester;

import es.upm.babel.cclib.Tryer;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;


/**
 * Represents a BasicCall together with an oracle for deciding if the call
 * executed correctly.
 */
public abstract class Call<V> extends Tryer {
  private static int counter = 1;
  private static Map<String,Call> names = null;

  final static protected int ESPERA_MIN_MS = 150;

  String name;
  boolean hasSymbolicName = false;
  Oracle<V> oracle;
  boolean started = false;
  private Object user = null;
  private Return<V> returner;
  private Object controller;
  private int waitTime;

  /**
   * Constructs a call. A call consists of a recipe for making a call,
   * and optionally a oracle that decides if an invocation of the call returned the
   * correct result, and optionally a symbolic name for the call.
   */
  public Call() {
    this.name = newName();
    this.user = getUser();
    // By default we check that the call returns normally.
    this.oracle = Check.returns();
    this.waitTime = ESPERA_MIN_MS;
    this.returner = new Return<>();
    addName(this.name,this);
  }

  /**
   * Associates an oracle with a call.
   * @param oracle an oracle which decides if the call returned the correct value.
   */
  public Call oracle(Oracle<V> oracle) {
    this.oracle = oracle;
    return this;
  }

  /**
   * Provides a short name for the oracle method.
   */
  public Call o(Oracle<V> oracle) {
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
  public Call user(Object user) {
    return this;
  }

  /**
   * A short name for the user method.
   */
  public Call u(Object user) {
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
  public Call name(String name) {
    this.name = name;
    deleteName(this.name);
    addName(this.name,this);
    this.hasSymbolicName = true;
    return this;
  }

  /**
   * A short name for the name method.
   */
  public Call n(String name) {
    return name(name);
  }

  /**
   * Sets wait time for calls until deciding they have blocked (in milliseconds)
   */
  public Call waitTime(int milliSecs) {
    this.waitTime = milliSecs;
    return this;
  }

  /**
   * Sets wait time for calls until deciding they have blocked (in milliseconds)
   */
  public Call w(int milliSecs) {
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
  public Call returnsTo(Return<V> returner) {
    this.returner = returner;
    return this;
  }

  /**
   * A short name for the returner method.
   */
  public Call r(Return<V> returner) {
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

  public void execute() {
    makeCall();

    // Wait a while before checking which calls blocked
    try { Thread.sleep(waitTime); }
    catch (InterruptedException exc) { };
  }

  static Set<Call<?>> execute(List<Call<?>> calls, Object controller, Set<Call<?>> allCalls, Set<Call<?>> blockedCalls) {
    int maxWaitTime = 0;

    for (Call<?> call : calls) {
      maxWaitTime = Math.max(maxWaitTime, call.getWaitTime());
    }

    for (Call<?> call : calls) {
      call.setController(controller);
      allCalls.add(call);
      call.makeCall();
    }

    // Wait a while before checking which calls blocked
    try { Thread.sleep(maxWaitTime); }
    catch (InterruptedException exc) { };

    // Compute unblocked (and change blockedCalls)
    Set<Call<?>> newUnblocked = Util.newUnblocked(calls, blockedCalls);
    return newUnblocked;
  }

  /**
   * Invoked by the call sequence in which the call resides.
   * This method is invoked before a call is made, thus
   * enabling an object to be passed from the call sequence to the actual call.
   *
   * @param controller The object passed from the call sequence to the call.
   */
  public void setController(Object controller) {
    this.controller = controller;
  }

  /**
   * Returns the controller.
   */
  public Object getController() {
    return controller;
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

  public static String printCalls(List<Call<?>> calls) {
    if (calls.size() == 1)
      return calls.get(0).toString();
    else {
      String callsString="";
      for (Call call : calls) {
        if (callsString != "") callsString += "\n  "+call;
        else callsString = call.toString();
      }
      return callsString;
    }
  }

  public String printCallWithReturn() {
    String callString = this.toString();
    if (raisedException())
      return callString + " raised " + getException();
    else {
      if (returner.hasReturnValue())
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
    return getSymbolicName().hashCode();
  }

  public boolean equals(Object obj) {
    if (obj instanceof Call) {
      Call otherCall = (Call) obj;
      return getSymbolicName().equals(otherCall.getSymbolicName());
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
  
  public static Object v(String callName) {
    return returnValue(callName);
  }

  private String newName() {
    return "$call_"+Integer.valueOf(counter++).toString();
  }
}
