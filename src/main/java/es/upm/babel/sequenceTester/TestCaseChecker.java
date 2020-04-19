package es.upm.babel.sequenceTester;

/**
 * The type of unit test case checkers (for determining whether test
 * cases are statically sound).
 */
public interface TestCaseChecker {
  /**
   * Checks a test statement.
   */
  public void check(String name, TestStmt stmt);
}
