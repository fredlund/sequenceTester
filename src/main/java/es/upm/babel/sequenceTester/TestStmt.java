package es.upm.babel.sequenceTester;

import java.util.Map;

/**
 * The type of a unit test statement -- what unit test cases
 * execute.
 */
public interface TestStmt {

  /**
   * Executes a test statement.
   */
  void execute(Map<Integer,Call> allCalls,
               Map<Integer,Call> blockedCalls,
               Object controller,
               String trace,
               String configurationDescription);
}
