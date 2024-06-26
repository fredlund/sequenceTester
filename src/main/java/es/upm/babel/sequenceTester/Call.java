package es.upm.babel.sequenceTester;

import java.util.*;
import java.io.StringWriter;
import java.io.PrintWriter;


/**
 * Represents a call to an API, which can block, return a value or raise an exception.
 * Methods permit to inspect the call to decide if its execution has terminated (unblocked),
 * and how it terminated (with an exception or a normal return).
 */
public abstract class Call<V> extends Tryer {
  private static int counter = 1;

  private final int id;
  private volatile boolean started = false;
  private Object user;
  private int waitTime;
  private final UnitTest unitTest;
  private volatile boolean hasReturnValue = false;
  private volatile V returnValue = null;
  private boolean checkedForException = false;
  private boolean checkedForUnblocks = false;
  private Execute execute;

  /**
   * Constructs a call. Often this constructor should be 
   * extended in classes which extend the abstract Call class.
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
   * another call from the same user cannot be made. Specifying the default null
   * user prevents this check.
   */
  public Call<V> user(Object user) {
    this.user = user;
    return this;
  }

  /**
   * Sets the user (process) executing a call.
   * The library fails a tests if a call is made from a user 
   * who has another blocking call. Specifying the default null
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

  void checkedForUnblocks() {
    checkedForUnblocks = true;
  }

  boolean didCheckForUnblocks() {
    return checkedForUnblocks;
  }

  static void checkExceptions(Set<Call<?>> calls, boolean calledFromAfter) {
    for (Call<?> call : calls) {
      if (call.raisedException() && !call.checkedForException) {
        Throwable exc = call.getException();
        StringWriter errors = new StringWriter();
        exc.printStackTrace(new PrintWriter(errors));
        String StackTrace = errors.toString();
        call.checkedForException();

	String msg = Texts.getText("the_call_to","S")+call+Texts.getText("raised_an_exception","SP")+exc+"\nStacktrace:\n"+StackTrace+"\n";
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
  void forceExecute() {
    if (!hasStarted())
      Execute.exec(this);
  }

  String printCall() {
    return id+": "+ this;
  }

  /**
   * Prints the list of calls.
   */
  public static String printCalls(Collection<Call<?>> calls) {
    StringBuilder callsString= new StringBuilder();
    for (Call<?> call : calls) {
      if (!callsString.toString().equals("")) callsString.append("\n  ").append(call.printCall());
      else callsString = new StringBuilder(call.printCall());
    }
    return callsString.toString();
  }

  String printCallWithReturn() {
    String callString = printCall();
    if (raisedException())
      return callString + Texts.getText("raised","SP") + intGetException();
    else {
      if (returnsValue())
        return callString + Texts.getText("returned","SP") + intGetReturnValue();
      else
        return callString;
    }
  }

  /**
   * Returns true if the execution of the call has started.
   */
  boolean hasStarted() {
    return started;
  }

  // In the "current" cclib a tryer may be:
  //  - blocked because it did not terminate, OR because it with an exception
  // (tryer.raisedException())
  // In the code below we instead consider a call blocked
  // if the call truly blocked AND it did not raise an
  // exception.
  boolean isBlocked() {
    return super.isBlocked() && !raisedException();
  }

  boolean returnsValue() {
    return hasStarted() && !isBlocked() && hasReturnValue;
  }

  void setReturnValue(V returnValue) {
    this.hasReturnValue = true;
    this.returnValue = returnValue;
  }

  V intGetReturnValue() {
    return returnValue;
  }

  Throwable intGetException() {
    return super.getException();
  }

  public int hashCode() {
    return id;
  }

  public boolean equals(Object obj) {
    if (obj instanceof Call) {
      Call<?> otherCall = (Call<?>) obj;
      return id == otherCall.id;
    } else return false;
  }

  static void reset() {
    counter = 1;
  }

  UnitTest getUnitTest() { return unitTest; }

  void setExecute(Execute e) {
    execute = e;
  }

  Execute getExecute() {
    return execute;
  }

  //////////////////////////////////////////////////////////////////////

  /**
   * Asserts that the call unblocked immediately after starting it (possibly running
   * in parallel with other calls), 
   * and moreover that the calls mentioned in calls were unblocked too,
   * and moreover that no other calls were unblocked.
   * If the call has not yet started executing this method forces its execution.
   */
  public Call<V> assertUnblocks(Call<?>... calls) {
    forceExecute();
    List<Call<?>> mustUnblocks = new ArrayList<>();
    Collections.addAll(mustUnblocks, calls);
    mustUnblocks.add(this);
    SeqAssertions.assertUnblocks(this.getExecute(),mustUnblocks);
    return this;
  }

  /**
   * Asserts that the call was blocked immediately after starting it (possibly
   * running in parallel with other calls), 
   * and moreover that the calls mentioned in calls were unblocked,
   * and moreover that no other calls were unblocked.
   * If the call has not yet started executing this method forces its execution.
   */
  public Call<V> assertBlocks(Call<?>... calls) {
    forceExecute();
    List<Call<?>> mustUnblocks = new ArrayList<>();
    Collections.addAll(mustUnblocks, calls);
    SeqAssertions.assertUnblocks(this.getExecute(),mustUnblocks);
    return this;
  }

  /**
  /**
   * Returns the return value of the call (if any). If the call is still blocked,
   * or if the call raised an exception, or if the call did not return any value,
   * the method fails,
   * If the call has not yet started executing this method forces its execution.
   */
  public V getReturnValue() {
    forceExecute();
    checkedForException();
    if (!returnsValue())
      UnitTest.failTest(Texts.getText("the_call","S")+this+Texts.getText("did_not","SP")+Texts.getText("return_a_value"));
    return returnValue;
  }

  /**
   * Returns the exception raised during the execution of the call. If the call is still blocked,
   * or if the call returned normally, the method fails.
   * Moreover the method checks that the calls mentioned in calls were unblocked,
   * and moreover checks that no other calls were unblocked.
   * If the call has not yet started executing this method forces its execution.
   */
  public Throwable getExceptionRaised() {
    forceExecute();
    checkedForException();
    if (!raisedException())
      UnitTest.failTest(Texts.getText("the_call","S")+this+Texts.getText("did_not","SP")+Texts.getText("raise_an_exception"));
    return super.getException();
  }

  /**
   * Asserts that the call is blocked.
   * If the call has not yet started executing this method forces its execution.
   */
  public Call<V> assertIsBlocked() {
    forceExecute();
    checkedForUnblocks();
    checkedForException();
    if (!isBlocked())
      UnitTest.failTest(Texts.getText("the_call","S")+this+Texts.getText("is_not_blocked","P"));
    return this;
  }

  /**
   * Asserts that the call is unblocked, i.e., returned normally or raised an exception.
   * If the call has not yet started executing this method forces its execution.
   */
  public Call<V> assertIsUnblocked() {
    forceExecute();
    checkedForUnblocks();
    if (isBlocked())
      UnitTest.failTest(Texts.getText("the_call","S")+this+Texts.getText("is_still_blocked","P"));
    List<Call<?>> mustUnblocks = new ArrayList<>();
    return this;
  }

  /**
   * Asserts that the call raised an exception,
   * and moreover that the calls mentioned in calls were unblocked too,
   * and moreover that no other calls were unblocked.
   * If the call has not yet started executing this method forces its execution.
   */
  public Call<V> assertRaisedException(Call<?>... calls) {
    forceExecute();
    checkedForException();
    if (!super.raisedException())
      UnitTest.failTest(Texts.getText("the_call","S")+this+Texts.getText("did_not","SP")+
                        Texts.getText("raise_an_exception"));
    assertUnblocks(calls);
    return this;
  }

  /**
   * Asserts that the call returned normally, i.e., did not raise an exception,
   * and moreover that the calls mentioned in calls were unblocked,
   * and moreover that no other calls were unblocked.
   * If the call has not yet started executing this method forces its execution.
   */
  public Call<V> assertReturns(Call<?>... calls) {
    forceExecute();
    checkedForException();
    if (hasStarted() && isBlocked()) {
      UnitTest.failTest(Texts.getText("the_call","S")+this+
                        Texts.getText("should_have_returned","SP")+
                        Texts.getText("but","S")+Texts.getText("is_still_blocked"));
    } else if (hasStarted() && raisedException()) {
      if (!(hasStarted() && !isBlocked() && !raisedException()))
        UnitTest.failTest(Texts.getText("the_call", "S")+this+
                        Texts.getText("should_have_returned","SP")+
                          Texts.getText("but","S")+Texts.getText("raised_an_exception","S")+this.getException());
    }
    assertUnblocks(calls);
    return this;
  }

  /**
   * Asserts that the call returned concrete value,
   * and moreover that the calls mentioned in calls were unblocked,
   * and moreover that no other calls were unblocked.
   * If the call has not yet started executing this method forces its execution.
   */
  public Call<V> assertReturnsValue(V expected, Call<?>... calls) {
    assertReturns();
    V actual = getReturnValue();
    if (!expected.equals(actual))
      UnitTest.failTest(Texts.getText("the_call","S")+this+Texts.getText("should_have_returned","SP")+Texts.getText("the_value","S")+expected+Texts.getText("but","SP")+Texts.getText("returned","S")+actual); 
    return this;
  }

  /**
   * Asserts that the call raised an exception of class excClass,
   * and moreover that the calls mentioned in calls were unblocked,
   * and moreover that no other calls were unblocked.
   * If the call has not yet started executing this method forces its execution.
   */
  public Call<V> assertRaisedException(Class<?> excClass, Call<?>... calls) {
    assertRaisedException(calls);
    Class<?> exceptionClass = getExceptionRaised().getClass();
    if (!excClass.isAssignableFrom(exceptionClass)) {
      UnitTest.failTest(Texts.getText("the_call","S")+this+Texts.getText("should_have","SP")+
                        Texts.getText("raised_the_exception","S")+excClass+
                        Texts.getText("but","SP")+Texts.getText("raised_the_exception","S")+exceptionClass);
    }
    return this;
  }

  /**
   * Asserts that the call returned a value.
   * and moreover that the calls mentioned in calls were unblocked,
   * and moreover that no other calls were unblocked.
   * If the call has not yet started executing this method forces its execution.
   */
  public Call<V> assertReturnsValue(Call<?>... calls) {
    forceExecute();
    checkedForException();
    if (!returnsValue())
      UnitTest.failTest(Texts.getText("the_call","S")+this+Texts.getText("did_not","SP")+
                        Texts.getText("return_a_value"));
    assertUnblocks(calls);
    return this;
  }

  /**
   * Returns the return value of the call (if any). If the call is still blocked,
   * or if the call raised an exception, or if the call did not return any value,
   * the method fails,
   * Moreover the method checks that the calls mentioned in calls were unblocked,
   * and moreover checks that no other calls were unblocked.
   * If the call has not yet started executing this method forces its execution.
   */
  public V assertGetReturnValue(Call<?>... calls) {
    forceExecute();
    checkedForException();
    assertReturnsValue(calls);
    return returnValue;
  }

  /**
   * Returns the exception raised during the execution of the call. If the call is still blocked,
   * or if the call returned normally, the method fails.
   * Moreover the method checks that the calls mentioned in calls were unblocked,
   * and moreover checks that no other calls were unblocked.
   * If the call has not yet started executing this method forces its execution.
   */
  public Throwable assertGetException(Call<?>... calls) {
    forceExecute();
    checkedForException();
    assertRaisedException(calls);
    return super.getException();
  }


  //////////////////////////////////////////////////////////////////////

//  String intToString() {
//    return this+"{id="+id+",started="+started+",hasReturnValue="+hasReturnValue+",returnValue="+returnValue+",raisedException="+super.raisedException()+"}";
//  }
}
