package es.upm.babel.sequenceTester;

import java.util.Map;
import java.util.HashMap;
import java.util.function.Supplier;

public class Lambda<V> extends Call<V> {

  private Supplier<Call<V>> call;
  private Call<V> called;

  /**
   * Constructors a call that is suspended.
   * @param call a Java supplier returning an call.
   */
  public Lambda(Supplier<Call<V>> call) {
    super();
    this.call = call;
    this.called = null;
    this.oracle = null;
  }

  public void makeCall() {
    Call<V> resolvedCall = resolveCommand();
    started = true;
    resolvedCall.makeCall();
  }

  Call<V> resolveCommand() {
    this.called = call.get();
    called.setController(getController());
    if (oracle == null) oracle = called.getOracle();
    return called;
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
  
  public V returnValue() {
    return called.returnValue();
  }
  
  public String toString() {
    if (called != null)
      return called.toString();
    else
      return "() -> "+call;
  }
}
