package es.upm.babel.sequenceTester;

import java.util.Map;
import java.util.HashMap;


/**
 * Represents a BasicCall together with an oracle for deciding if the call
 * executed correctly.
 */
public class Call {
  private static int counter = 1;
  private static Map<String,Call> symbolic_vars = null;
  
  final static protected int ESPERA_MIN_MS = 100;
  
  int name;
  String symbolicName = null;
  BasicCall bc;
  Result r;
  
  /**
   * Constructors a call. A call consists of a recipe for making a call,
   * and an oracle that decides if an invocation of the call returned the
   * correct result.
   *
   * @param bc an object which can execute the call.
   * @param r an oracle which decides if the call returned the correct value.
   * @param symbolicName a symbolic name for the call. The symbolic variable
   * may be used in a continuation (in the TestStmt where the call resides)
   * to specify that this call was unblocked by a later call.
   */
  public Call(String symbolicName, BasicCall bc, Result r) {
    this.name = new_call_counter();
    this.symbolicName = symbolicName;
    this.bc = bc;
    this.r = r;
    add_symbolic_var(this.symbolicName,this);
  }
  
  public Call(BasicCall bc, Result r) {
    this.name = new_call_counter();
    this.bc = bc;
    this.r = r;
  }
  
  public Call(BasicCall bc) {
    this.name = new_call_counter();
    this.bc = bc;
    this.r = Return.shouldReturn(true);
  }
  
  public Call(String symbolicName, BasicCall bc) {
    this.name = new_call_counter();
    this.symbolicName = symbolicName;
    this.bc = bc;
    this.r = Return.shouldReturn(true);
    add_symbolic_var(this.symbolicName,this);
  }
  
  public BasicCall bc() {
    return bc;
  }
  
  public Result result() {
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
  
  static void execute(Call[] calls, Object controller,Map<Integer,Call> allCalls) {
    for (Call call : calls) {
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
   * Executes the call. The method waits a fixed interval of time before returning.
   */
  public void makeCall() {
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
  
  public static Call returns(BasicCall bc) {
    return new Call(bc,Return.shouldReturn(true));
  }
  
  public static Call raisesException(BasicCall bc, Class exceptionClass) {
    return new Call(bc,Return.raisesException(exceptionClass));
  }
  
  public static Call returns(String name, BasicCall bc) {
    return new Call(name,bc,Return.shouldReturn(true));
  }
  
  public static Call returns(BasicCall bc, Object returnValue) {
    return new Call(bc,Return.returns(true,returnValue));
  }
  
  public static Call returns(String name, BasicCall bc, Object returnValue) {
    return new Call(name,bc,Return.returns(true,returnValue));
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
    System.out.println("lookupCall("+var+") => "+result);
    return result;
  }
  
  public static Call[] parallel(Call... calls) {
    return calls;
  }
  
}
