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
			UnitTest unitTest,
			String trace) {
	trace = testCall.execute(allCalls, blockedCalls, unitTest, trace);
	testStmt.execute(allCalls, blockedCalls, unitTest, trace);
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
