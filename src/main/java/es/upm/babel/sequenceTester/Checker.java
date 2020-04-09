package es.upm.babel.sequenceTester;

/**
 * The type of unit test case checkers (for determining whether test
 * cases are statically sound).
 */
public interface Checker {
    public void check(String name, TestStmt stmt);
}
