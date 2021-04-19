package es.upm.babel.sequenceTester;

import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.function.Supplier;

public class Lambda implements TestStmt {
  private Supplier<TestStmt> stmtSupplier;

  public Lambda(Supplier<TestStmt> stmtSupplier) {
    this.stmtSupplier = stmtSupplier;
  }

  public void execute(Set<Call<?>> allCalls,
                      Set<Call<?>> blockedCalls,
                      UnitTest unitTest,
                      String trace) {

    stmtSupplier.get().execute(allCalls, blockedCalls, unitTest, trace);
  }

  public String toString() {
    return "Lambda ... ";
  }
}
