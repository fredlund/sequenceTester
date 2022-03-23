package es.upm.babel.sequenceTester;

import java.util.*;

/**
 * Provides conventient test assertions.
 */
public class SeqAssertions {
  private static ArrayList<String> alternatives;

  /**
   * Asserts that the assertion argument fails, and succeeds if it does. Optionally shows the
   * message supplied in the assertion exception.
   * Note that failures in an @AfterEach method cannot be handled by this assertion.
   */
  public static void assertFail(Runnable assertion, boolean showFailure) {
    boolean failed = false;
    try {
      assertion.run();
    } catch (org.opentest4j.AssertionFailedError exc) {
      failed = true;
      if (showFailure) {
        String msg = exc.getMessage();
        if (msg == null) msg = "";
        msg += "\n\n"+UnitTest.errorTrace(UnitTest.ErrorLocation.LASTLINE);
        System.out.println(Texts.getText("as_expected_the_test_failed","C")+".\n"+Texts.getText("message","C")+": "+msg);
      }
    }
    if (!failed) UnitTest.failTest("the_test_did_not_fail");
  }

  /**
   * Asserts that call returns a value equal to expected.
   * Otherwise (e.g., the call is blocked, it raises an exception, it returns
   * an unequal value, etc) the assertion fails.
   * If call has not been executed, it will be executed by this assertion.
   */
  public static <V> void assertEquals(V expected, Call<V> call) {
    V actual = call.getReturnValue();
    if (!expected.equals(actual))
      UnitTest.failTest(Texts.getText("the_call","S")+call+Texts.getText("should_have_returned","SP")+Texts.getText("the_value","S")+expected+Texts.getText("but","SP")+Texts.getText("returned","S")+actual); 
  }

  /**
   * Asserts that the call raises an exception of class excClass.
   * If call has not been executed, it will be executed by this assertion.
   */
  public static <V> void assertThrown(Class<?> excClass, Call<V> call) {
    call.assertRaisedException();
    Class<?> exceptionClass = call.getException().getClass();
    if (!excClass.isAssignableFrom(exceptionClass)) {
      UnitTest.failTest(Texts.getText("the_call","S")+call+Texts.getText("should_have","SP")+
                        Texts.getText("raised_an_exception","S")+excClass+
                        Texts.getText("but","SP")+Texts.getText("raised_the_exception","S")+exceptionClass);
    }

    // Note that we checked whether it raised an exception
    call.checkedForException();
  }

  /**
   * Asserts that the call raises an exception.
   * If call has not been executed, it will be executed by this assertion.
   */
  public static <V> void assertThrown(Call<V> call) {
    call.assertRaisedException();
    // Note that we checked whether it raised an exception
    call.checkedForException();
  }
  
  /**
   * Asserts that the calls executed by the parameter e unblocked the calls in mustCall,
   * and that no calls not listed in mustCalls or mayCalls were unblocked.
   */
  public static void assertMustMayUnblocked(Execute e, List<Call<?>> mustCalls, List<Call<?>> mayCalls) {
    new Unblocks(mustCalls,mayCalls).checkCalls(e);
  }

  /**
   * Asserts that the execution of call and its sibling calls (executing in parallel with
   * call) unblocked the calls in mustCall,
   * and that no calls not listed in mustCalls or mayCalls were unblocked.
   */
  public static void assertMustMayUnblocked(Call<?> call, List<Call<?>> mustCalls, List<Call<?>> mayCalls) {
    assertMustMayUnblocked(call.getExecute(), mustCalls, mayCalls);
  }
  
  /**
   * Asserts that during the last execution of calls the calls in mustCall were unblocked,
   * and that no calls not listed in mustCalls or mayCalls were unblocked.
   */
  public static void assertMustMayUnblocked(List<Call<?>> mustCalls, List<Call<?>> mayCalls) {
    assertMustMayUnblocked(UnitTest.getCurrentTest().getLastExecute(), mustCalls, mayCalls);
  }
  
  /**
   * Asserts that the calls executed by the parameter e unblocked the calls in mustCallsList,
   * and that other calls were unblocked.
   */
  public static void assertUnblocks(Execute e, List<Call<?>> mustCallsList) {
    assertMustMayUnblocked(e, mustCallsList, Collections.emptyList());
  }

  /**
   * Asserts that the execution of call and its sibling calls (executing in parallel with
   * call) unblocked the calls in mustCallsList,
   * and that other calls were unblocked.
   */
  public static void assertUnblocks(Call<?> call, List<Call<?>> mustCallsList) {
    assertUnblocks(call.getExecute(), mustCallsList);
  }

  /**
   * Asserts that during the last execution of calls the calls in mustCallsList were unblocked,
   * and that no calls were unblocked.
   */
  public static void assertUnblocks(List<Call<?>> mustCallsList) {
    assertUnblocks(UnitTest.getCurrentTest().getLastExecute(), mustCallsList);
  }

  /**
   * Marks the beginning of a "branching" assertion.
   */
  public static void checkAlternatives() {
    alternatives = new ArrayList<String>();
  }

  /**
   * Asserts an alternative assertion. If the assertion fails, the 
   * next alternative assertion is executed.
   */
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

  /**
   * Signals that the last alternative assertion has been checked,
   * and that if no alternative succeeded, the branching assertion failed.
   */
  public static void endAlternatives() {
    String msg = Texts.getText("all_possible_alternatives_failed","C")+":\n";
    for (int i=0; i<alternatives.size(); i++) {
      if (alternatives.get(i) != null)
        msg += Texts.getText("alternative","SC")+(i+1)+":\n  "+alternatives.get(i)+"\n";
    }
    UnitTest.failTest(msg+"\n");
  }

}
