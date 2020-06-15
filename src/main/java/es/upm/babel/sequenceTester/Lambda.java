package es.upm.babel.sequenceTester;

import java.util.Map;
import java.util.HashMap;

public class Command extends Call {

  private Function<Object,Call> command;

  /**
   * Constructors a call that is suspended.
   * @param command a function returning an object which can execute the call.
   * @param name the (symbolic) name of the call which this call is parameteric on.
   */
  public Command(Runnable command) {
    super();
    this.command = command;
    this.called = null;
  }

  public void makeCall() {
    Call resolvedCall = resolveCommand();
    started = true;
    resolvedCall.makeCall();
  }

  Call resolveCommand() {
    Call paramCall = lookupCall(actualParameter);
    if (paramCall.hasStarted() && paramCall.returned()) {
      Object returnValue = paramCall.returnValue();
      this.called = command.apply(returnValue);
      called.setController(getController());
      if (oracle == null) oracle = called.getOracle();
      return called;
    } else {
      UnitTest.failTestSyntax
        ("Call "+ this + " depends on call " + paramCall + " which has not terminated yet");
      throw new RuntimeException();
    }
  }

  public void toTry() throws Throwable {
    UnitTest.failTestFramework("trying to executing a command abstraction "+this);
  }

  public boolean hasStarted() {
    return called != null && called.hasStarted();
  }

 public boolean returned() {
    return called != null && called.returned();
  }

  public boolean raisedException() {
    return called != null & called.raisedException();
  }

  public Throwable getException() {
    return called.getException();
  }

  public boolean hasBlocked() {
    return called != null && called.hasBlocked();
  }
  
  public Object returnValue() {
    return called.returnValue();
  }
  
  public String toString() {
    if (called != null)
      return "(\""+actualParameter+"\") -> "+called;
    else
      return "(\""+actualParameter+"\") -> "+command;
  }
}
