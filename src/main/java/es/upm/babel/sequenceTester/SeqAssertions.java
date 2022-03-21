package es.upm.babel.sequenceTester;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Provides conventient test assertions.
 */
public class SeqAssertions {
  private static ArrayList<String> alternatives;

  public static void assertFail(Runnable assertion, boolean showFailure) {
    boolean failed = false;
    try {
      System.out.println("will run "+assertion);
      assertion.run();
      System.out.println(assertion+" terminated normally");
    } catch (org.opentest4j.AssertionFailedError exc) {
      System.out.println(assertion+" terminated with an exception");
      failed = true;
      if (showFailure) {
        String msg = exc.getMessage();
        if (msg == null) msg = "";
        msg += "\n"+UnitTest.getCurrentTest().errorTrace(UnitTest.ErrorLocation.LASTLINE);
        System.out.println("As expected the test failed. Message:\n"+msg);
      }
    }
    if (!failed) UnitTest.failTest("The test did not fail");
  }

  public static <V> void assertEquals(V expected, Call<V> call) {
    V actual = call.getReturnValue();
    if (!expected.equals(actual))
      UnitTest.failTest(" la llamada "+call+" deberia haber devuelto el valor "+expected+
                        " pero devolvió "+actual); 
  }

  public static <V> void assertThrown(Class<?> excClass, Call<V> call) {
    if (!call.raisedException()) {
      UnitTest.failTest("la llamada "+call+" deberia haber lanzado una exception "+excClass);
    }

    Class<?> exceptionClass = call.getException().getClass();
    if (!excClass.isAssignableFrom(exceptionClass)) {
      UnitTest.failTest("la llamada "+call+" deberia haber lanzado una exception "+excClass+
                        " pero lanzo una excepcion "+exceptionClass);
    }

    // Note that we checked whether it raised an exception
    call.checkedForException();
  }

  public static <V> void assertThrown(Call<V> call) {
    if (!call.raisedException()) {
      UnitTest.failTest("la llamada "+call+" deberia haber lanzado una exception");
    }

    // Note that we checked whether it raised an exception
    call.checkedForException();
  }
  
  public static void assertIsUnblocked(Call<?> call) {
    if (call.isBlocked())
      UnitTest.failTest("la llamada "+call+" deberia haber sido desbloqueada pero es bloqueada todavia");
  }

  public static void assertIslocked(Call<?> call) {
    if (call.isBlocked())
      UnitTest.failTest("la llamada "+call+" todavia deberia ser bloqueada pero fue desbloqueda");
  }
  
  public static void assertMustMayUnblocked(Execute e, List<Call<?>> mustCalls, List<Call<?>> mayCalls) {
    new Unblocks(mustCalls,mayCalls).checkCalls(e);
  }

  public static void assertMustMayUnblocked(Call<?> call, List<Call<?>> mustCalls, List<Call<?>> mayCalls) {
    assertMustMayUnblocked(call.getExecute(), mustCalls, mayCalls);
  }
  
  public static void assertMustMayUnblocked(List<Call<?>> mustCalls, List<Call<?>> mayCalls) {
    assertMustMayUnblocked(UnitTest.getCurrentTest().getLastExecute(), mustCalls, mayCalls);
  }
  
  public static void assertUnblocks(Execute e, List<Call<?>> mustCallsList) {
    assertMustMayUnblocked(e, mustCallsList, Arrays.asList());
  }

  public static void assertUnblocks(Call<?> call, List<Call<?>> mustCallsList) {
    assertUnblocks(call.getExecute(), mustCallsList);
  }

  public static void assertUnblocks(List<Call<?>> mustCallsList) {
    assertUnblocks(UnitTest.getCurrentTest().getLastExecute(), mustCallsList);
  }

  public static void checkAlternatives() {
    alternatives = new ArrayList<String>();
  }

  public static boolean checkAlternative(Runnable assertions) {
    try {
      assertions.run();
      return true;
    } catch (org.opentest4j.AssertionFailedError exc) {
      String msg = exc.getMessage();
      alternatives.add(msg);
      return false;
    }
  }

  public static void endAlternatives() {
    String msg = "All possible alternative executions failed:\n";
    for (int i=0; i<alternatives.size(); i++) {
      if (alternatives.get(i) != null)
        msg += "Alternative "+(i+1)+":\n  "+alternatives.get(i)+"\n";
    }
    UnitTest.failTest(msg+"\n");
  }

}
