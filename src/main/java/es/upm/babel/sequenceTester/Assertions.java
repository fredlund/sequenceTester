package es.upm.babel.sequenceTester;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;


public class Assertions {
  public static <V> void assertEquals(V expected, Call<V> call) {
    V actual = call.getReturnValue();
    if (!expected.equals(actual))
      UnitTest.failTest(" la llamada "+call+" deberia haber devuelto el valor "+expected+
                        " pero devolvió "+actual); 
  }

  public static void assertBlocking(List<Call<?>> mustCalls, List<Call<?>> mayCalls) {
    new Unblocks(mustCalls,mayCalls).checkCalls(true,true);
  }
  
  public static void assertUnblocks(Call... mustCalls) {
    List<Call<?>> mustCallsList = new ArrayList<Call<?>>();
    for (Call<?> call : UnitTest.currentTest.calls) mustCallsList.add(call);
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
  
}
