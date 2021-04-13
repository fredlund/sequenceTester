package es.upm.babel.sequenceTester;

import java.util.Set;


/**
 * Represents a unit test statement composed by a test call followed by
 * a unit test statement.
 */
public class Prefix implements TestStmt {
    private TestCall testCall;
    private TestStmt testStmt;

    public Prefix(TestCall testCall, TestStmt testStmt) {
	this.testCall = testCall;
	this.testStmt = testStmt;
    }

    public void execute(Set<Call<?>> allCalls,
			Set<Call<?>> blockedCalls,
			Object controller,
			String trace,
			String configurationDescription) {
	trace = testCall.execute(allCalls, blockedCalls, controller, trace, configurationDescription);
	testStmt.execute(allCalls, blockedCalls, controller, trace, configurationDescription);
    }

    public TestCall testCall() {
	return testCall;
    }

    public TestStmt stmt() {
	return testStmt;
    }

    public String toString() {
	return "Prefix("+testCall+","+testStmt+")";
    }
}
