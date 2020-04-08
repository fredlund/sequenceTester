package es.upm.babel.sequenceTester;

import java.util.Map;

public interface TestStmt {
    public void execute(Map<Integer,Call> allCalls,
			Map<Integer,Call> blockedCalls,
			Object controller,
			String trace,
			String configurationDescription);
}
