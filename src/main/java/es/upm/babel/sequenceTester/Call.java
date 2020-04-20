package es.upm.babel.sequenceTester;

import es.upm.babel.cclib.Tryer;

import java.util.Map;
import java.util.HashMap;


/**
 * Represents a BasicCall together with an oracle for deciding if the call
 * executed correctly.
 */
public abstract class Call extends Tryer {
  private static int counter = 1;
  private static Map<String,Call> symbolicVars = null;
  
  final static protected int ESPERA_MIN_MS = 100;
  
  // The internal id of the action -- this is too fragile and should change
  int callId;
  String symbolicName;
  Oracle oracle;
  boolean started = false;
  private Object user = null;
  private Object returnValue;
  private Object controller;
  
  
  /**
   * Constructs a call. A call consists of a recipe for making a call,
   * and optionally a oracle that decides if an invocation of the call returned the
   * correct result, and optionally a symbolic name for the call.
   */
  public Call() {
    this.symbolicName = null;
    this.callId = newCallCounter();
    this.user = getUser();

    // By default we check that the call returns normally.
    this.oracle = Check.returns();
  }

  /**
   * Associates an oracle with a call.
   * @param oracle an oracle which decides if the call returned the correct value.
   */
  public Call oracle(Oracle oracle) {
    this.oracle = oracle;
    return this;
  }
                
  /**
   * Provides a short name for the oracle method.
   */
  public Call o(Oracle oracle) {
    return oracle(oracle);
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
   * The symbolic variable
   * may be used in a continuation (in the TestStmt where the call resides)
   * to specify that this call was unblocked by a later call.
   */
  public Call name(String symbolicName) {
    this.symbolicName = symbolicName;
    addSymbolicVar(this.symbolicName,this);
    return this;
  }

  /**
   * A short name for the name method.
   */
  public Call n(String symbolicName) {
    return name(symbolicName);
  }
  
  /**
   * Returns the oracle of the call (otherwise null).
   */
  public Oracle getOracle() {
    return oracle;
  }
  
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
    return !hasBlocked() && !raisedException();
  }
  
  int getCallId() {
    return this.callId;
  }
  
  /**
   * Returns the symbolic name of the call (otherwise null).
   */
  String getSymbolicName() {
    return this.symbolicName;
  }
  
  public void execute() {
    makeCall();
    
    // Wait a while before checking which calls blocked
    try { Thread.sleep(ESPERA_MIN_MS); }
    catch (InterruptedException exc) { };
  }
  
  static void execute(Call[] calls, Object controller,Map<Integer,Call> allCalls) {
    for (Call call : calls) {
      call.setController(controller);
      allCalls.put(call.getCallId(),call);
      call.makeCall();
    }
    
    // Wait a while before checking which calls blocked
    try { Thread.sleep(ESPERA_MIN_MS); }
    catch (InterruptedException exc) { };
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
  
  static String printCalls(Call[] calls) {
    if (calls.length == 1)
      return calls[0].toString();
    else {
      String callsString="";
      for (Call call : calls) {
        if (callsString != "") callsString += "\n  "+call;
        else callsString = call.toString();
      }
      return callsString;
    }
  }
  
  String printCallWithReturn() {
    String callString = this.toString();
    if (raisedException())
      return callString + " raised " + getException();
    else {
      Object returnValue = returnValue();
      if (returnValue != null)
        return callString + " returned " + returnValue();
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
    return getCallId();
  }
  
  public boolean equals(Object obj) {
    if (obj instanceof Call) {
      Call otherCall = (Call) obj;
      return getCallId() == otherCall.getCallId();
    } else return false;
  }
  
  static int newCallCounter() {
    int result = counter;
    ++counter;
    return result;
  }
  
  static void reset() {
    counter = 1;
    symbolicVars = new HashMap<String,Call>();
  }
  
  static void addSymbolicVar(String var, Call call) {
    Call result = symbolicVars.get(var);
    if (result != null) {
      UnitTest.failTestFramework
        ("symbolic variable "+var+" already has a value "+result);
    }
    symbolicVars.put(var,call);
  }
  
  static Call lookupCall(String var) {
    Call result = symbolicVars.get(var);
    if (result == null) {
      UnitTest.failTestFramework("symbolic variable "+var+" missing\nmap="+symbolicVars);
    }
    return result;
  }
}
