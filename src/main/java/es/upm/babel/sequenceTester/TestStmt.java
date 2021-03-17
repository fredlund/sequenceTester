package es.upm.babel.sequenceTester;

import java.util.Set;

/**
 * The type of a unit test statement -- what unit test cases
 * execute.
 */
public interface TestStmt {

  /**
   * Executes a test statement.
   */
  void execute(Set<Call<?>> allCalls,
               Set<Call<?>> blockedCalls,
               Object controller,
               String trace,
               String configurationDescription);
}
