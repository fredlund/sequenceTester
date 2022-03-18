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

  public static <V> void assertEquals(V expected, Call<V> call) {
    V actual = call.getReturnValue();
    if (!expected.equals(actual))
      UnitTest.failTest(" la llamada "+call+" deberia haber devuelto el valor "+expected+
                        " pero devolvió "+actual); 
  }

  public static void assertBlocking(Execute e, List<Call<?>> mustCalls, List<Call<?>> mayCalls) {
    new Unblocks(mustCalls,mayCalls).checkCalls(e);
  }

  public static void assertBlocking(List<Call<?>> mustCalls, List<Call<?>> mayCalls) {
    assertBlocking(UnitTest.getCurrentTest().getLastExecute(), mustCalls, mayCalls);
  }
  
  public static void assertBlocking(Call<?> call, List<Call<?>> mustCalls, List<Call<?>> mayCalls) {
    assertBlocking(call.getExecute(), mustCalls, mayCalls);
  }
  
  public static void assertUnblocks(Execute e, Call... mustCalls) {
    List<Call<?>> mustCallsList = new ArrayList<Call<?>>();
    for (Call<?> mustCall : mustCalls) mustCallsList.add(mustCall);
    assertBlocking(e, mustCallsList, Arrays.asList());
  }

  public static void assertUnblocks(Call... mustCalls) {
    assertUnblocks(UnitTest.getCurrentTest().getLastExecute(), mustCalls);
  }

  public static void assertUnblocks(Call<?> call, Call... mustCalls) {
    assertUnblocks(call.getExecute(), mustCalls);
  }

  public static void assertBlocks(Execute e, Call... mustCalls) {
    assertBlocking(e, Arrays.asList(mustCalls), Arrays.asList());
  }

  public static void assertBlocks(Call... mustCalls) {
    assertBlocks(UnitTest.getCurrentTest().getLastExecute(), mustCalls);
  }

  public static void assertBlocks(Call<?> call, Call... mustCalls) {
    assertBlocks(call.getExecute(), mustCalls);
  }

  public static <V> void assertThrows(Class<?> excClass, Call<V> call) {
    if (!call.raisedException()) {
      UnitTest.failTest("la llamada "+call+" deberia haber lanzado una exception "+excClass);
    }

    Class<?> exceptionClass = call.getException().getClass();
    if (!excClass.isAssignableFrom(exceptionClass)) {
      UnitTest.failTest("la llamada "+call+" deberia haber lanzado una exception "+excClass+
                        " pero lanzo una excepcion "+exceptionClass);
    }
    
    call.checkedForException();
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
