package es.upm.babel.sequenceTester;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

public class SeqAssertions {
  private static ArrayList<String> alternatives;

  public static <V> void assertEquals(V expected, Call<V> call) {
    V actual = call.getReturnValue();
    if (!expected.equals(actual))
      UnitTest.failTest(" la llamada "+call+" deberia haber devuelto el valor "+expected+
                        " pero devolvió "+actual); 
  }

  public static void assertBlocking(List<Call<?>> mustCalls, List<Call<?>> mayCalls) {
    new Unblocks(mustCalls,mayCalls).checkCalls();
  }
  
  public static void assertUnblocks(Call... mustCalls) {
    List<Call<?>> mustCallsList = new ArrayList<Call<?>>();
    for (Call<?> call : mustCalls) mustCallsList.add(call);
    assertBlocking(mustCallsList, Arrays.asList());
  }

  public static void assertBlocks(Call... mustCalls) {
    assertBlocking(Arrays.asList(mustCalls), Arrays.asList());
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
    UnitTest.currentTest.setShortFailureMessages(true);
    alternatives = new ArrayList<String>();
  }

  public static boolean checkAlternative(Runnable assertions) {
    try {
      assertions.run();
      UnitTest.currentTest.setShortFailureMessages(true);
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
    UnitTest.currentTest.setShortFailureMessages(false);
    UnitTest.failTest(msg+"\n");
  }

}
