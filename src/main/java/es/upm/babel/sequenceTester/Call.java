package es.upm.babel.sequenceTester;

import java.util.Map;
import java.util.HashMap;
import java.util.function.Function;


/**
 * Represents a BasicCall together with an oracle for deciding if the call
 * executed correctly.
 */
public class Call {
  private static int counter = 1;
  private static Map<String,Call> symbolic_vars = null;
  
  final static protected int ESPERA_MIN_MS = 100;
  
  // The internal name of the action -- this is too fragile and should change
  int name;
  String symbolicName;
  Oracle r;
  BasicCall bc;
  Function<Object,BasicCall> bcLambda;
  String actualParameter;
  boolean started = false;
  private Object user = null;

  
  /**
   * Constructors a call. A call consists of a recipe for making a call,
   * and optionally a oracle that decides if an invocation of the call returned the
   * correct result, and optionally a symbolic name for the call.
   *
   * @param bc an object which can execute the call.
   */
  public Call(BasicCall bc) {
    this.bc = bc;
    this.bcLambda = null;
    // By default we check that the call returns normally.
    this.r = Check.returns();
    this.symbolicName = null;
    this.name = new_call_counter();
    this.user = bc.user();
  }

  /**
   * Constructors a call that is parametetric on the result from a previous call, referenced
   * by its symbolic name.
   * A call consists of a recipe for making a call,
   * and optionally a oracle that decides if an invocation of the call returned the
   * correct result, and optionally a symbolic name for the call.
   *
   * @param bcLambda a function returning an object which can execute the call.
   * @param name the (symbolic) name of the call which this call is parameteric on.
   */
  public Call(Function<Object,BasicCall> bcLambda, String name) {
    this.bc = null;
    this.bcLambda = bcLambda;
    // By default we check that the call returns normally.
    this.r = Check.returns();
    this.symbolicName = null;
    this.name = new_call_counter();
    this.actualParameter = name;
  }

  /**
   * Associates an oracle with a call.
   * @param r an oracle which decides if the call returned the correct value.
   */
  public Call oracle(Oracle r) {
    this.r = r;
    return this;
  }
                
  /**
   * A short name for the oracle method.
   */
  public Call o(Oracle r) {
    return oracle(r);
  }

  /**
   * Sets the user (process) executing a call.
   * Note: calling user on a Call overrides any user
   * defined for the BasicCall .
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
    add_symbolic_var(this.symbolicName,this);
    return this;
  }

  /**
   * A short name for the name method.
   */
  public Call n(String symbolicName) {
    return name(symbolicName);
  }
  
  public BasicCall bc() {
    return bc;
  }
  
  public Oracle oracle() {
    return r;
  }
  
  public int name() {
    return this.name;
  }
  
  public String symbolicName() {
    return this.symbolicName;
  }
  
  public void execute() {
    makeCall();
    
    // Wait a while before checking which calls blocked
    try { Thread.sleep(ESPERA_MIN_MS); }
    catch (InterruptedException exc) { };
  }
  
  static void resolveLambda(Call call) {
    if (call.bc() == null) {
      Call paramCall = Call.lookupCall(call.actualParameter);
      if (paramCall.hasStarted() && paramCall.bc.returned()) {
        Object returnValue = paramCall.returnValue();
        call.bc = call.bcLambda.apply(returnValue);
      } else {
        UnitTest.failTestSyntax
          ("Call "+ call + " depends on call " + paramCall + " which has not terminated yet");
      }
    }
  }

  static void execute(Call[] calls, Object controller,Map<Integer,Call> allCalls) {
    for (Call call : calls) {
      resolveLambda(call);
      call.setController(controller);
      allCalls.put(call.name(),call);
      call.makeCall();
    }
    
    // Wait a while before checking which calls blocked
    try { Thread.sleep(ESPERA_MIN_MS); }
    catch (InterruptedException exc) { };
  }
  
  /**
   * Invoked by the call sequence in which the call resides. 
   * This method is invoked before a call is made, and
   * the method in turn invokes the {@link BasicCall#setController(Object)}
   * method on the object that implements the call, thus
   * enabling an object to be passed from the call sequence to the actual call.
   *
   * @param controller The object passed from the call sequence to the call.
   */
  public void setController(Object controller) {
    bc.setController(controller);
  }
  
  /**
   * Returns true if the execution of the call has started.
   */
  public boolean hasStarted() {
    return started;
  }

  /**
   * Does the call represent a lambda abstraction?
   */
  public boolean hasLambda() {
    return bcLambda != null;
  }

  /**
   * Executes the call. The method waits a fixed interval of time before returning.
   */
  public void makeCall() {
    started = true;
    bc.start();
  }
  
  /**
   * Did a call raise an exception?
   *
   * @return a boolean corresponding to whether the call raised an exception or not.
   */
  public boolean raisedException() {
    return bc.raisedException();
  }
  
  /**
   * The value returned from the call (if any).
   *
   * @return an object corresponding to the value returned by the call.
   */
  public Object returnValue() {
    return bc.returnValue();
  }
  
  /**
   * The exception raised by the call (if any).
   *
   * @return an object corresponding to the exception raised by the call.
   */
  public Throwable getException() {
    return bc.getException();
  }
  
  public String toString() {
    if (bc == null)
      return "() -> ...";
    else
      return bc.toString();
  }
  
  public static String printCalls(Call[] calls) {
    if (calls.length == 1)
      return calls[0].printCall();
    else {
      String callsString="";
      for (Call call : calls) {
        if (callsString != "") callsString += "\n  "+call.printCall();
        else callsString = call.printCall();
      }
      return callsString;
    }
  }
  
  public String printCall() {
    return name()+":"+this.toString();
  }
  
  public String printCallWithReturn() {
    String callString = printCall();
    if (bc.raisedException())
      return callString + " raised " + bc.getException();
    else {
      Object returnValue = bc.returnValue();
      if (returnValue != null)
        return callString + " returned " + bc.returnValue();
      else
        return callString;
    }
  }
  
  public boolean isBlocked() {
    /**
     * In the "current" cclib a call may be:
     * - blocked (call.isBlocked())
     * - blocked because it terminated with an exception 
     * (call.raisedException())
     * - or not blocked because it terminated normally.
     *
     * In the code below we instead consider a call blocked
     * if the call truly blocked AND it did not raise an
     * exception.
     **/
    return bc.isBlocked() && !bc.raisedException();
  }
  
  public int hashCode() {
    return name;
  }
  
  public boolean equals(Object obj) {
    if (obj instanceof Call) {
      Call otherCall = (Call) obj;
      return name() == otherCall.name();
    } else return false;
  }
  
  static int new_call_counter() {
    int result = counter;
    ++counter;
    return result;
  }
  
  public static void reset() {
    counter = 1;
    symbolic_vars = new HashMap<String,Call>();
  }
  
  static void add_symbolic_var(String var, Call call) {
    Call result = symbolic_vars.get(var);
    if (result != null) {
      UnitTest.failTestFramework
        ("symbolic variable "+var+" already has a value "+result);
    }
    symbolic_vars.put(var,call);
  }
  
  public static Call lookupCall(String var) {
    Call result = symbolic_vars.get(var);
    if (result == null) {
      UnitTest.failTestFramework("symbolic variable "+var+" missing\nmap="+symbolic_vars);
    }
    return result;
  }
  
  public static Call[] parallel(Call... calls) {
    return calls;
  }
}
