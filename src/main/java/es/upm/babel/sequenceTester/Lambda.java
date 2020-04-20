package es.upm.babel.sequenceTester;

import java.util.Map;
import java.util.HashMap;
import java.util.function.Function;

public class Lambda extends Call {

  private Function<Object,Call> lambda;
  private String actualParameter;
  private Call called;

  /**
   * Constructors a call that is parametetric on the result from a previous call, referenced
   * by its symbolic name.
   * A call consists of a recipe for making a call,
   * and optionally a oracle that decides if an invocation of the call returned the
   * correct result, and optionally a symbolic name for the call.
   *
   * @param lambda a function returning an object which can execute the call.
   * @param name the (symbolic) name of the call which this call is parameteric on.
   */
  public Lambda(Function<Object,Call> lambda, String name) {
    super();
    this.lambda = lambda;
    this.actualParameter = name;
    this.called = null;
    this.oracle = null;
  }

  public void makeCall() {
    Call resolvedCall = resolveLambda();
    started = true;
    resolvedCall.makeCall();
  }

  Call resolveLambda() {
    Call paramCall = lookupCall(actualParameter);
    if (paramCall.hasStarted() && paramCall.returned()) {
      Object returnValue = paramCall.returnValue();
      this.called = lambda.apply(returnValue);
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
    UnitTest.failTestFramework("trying to executing a lambda abstraction "+this);
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
      return "(\""+actualParameter+"\") -> "+lambda;
  }
}
