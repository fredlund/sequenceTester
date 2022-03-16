package es.upm.babel.sequenceTester;

public class Assertions {
  public static <V> void assertEquals(V expected, Call<V> call) {
    V actual = call.getReturnValue();
    if (!expected.equals(actual))
      UnitTest.failTest(" la llamada "+call+" deberia haber devuelto el valor "+expected+
                        " pero devolvió "+actual); 
  }

  public static <V> void assertUnblocks(Call<V> call, String[] mayUnblock, String[] mustUnblock) {
    // UnitTest t = call.unitTest();
  }
}
