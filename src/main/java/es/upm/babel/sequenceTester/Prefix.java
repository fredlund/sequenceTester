package es.upm.babel.sequenceTester;

import java.util.Map;


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

    public void execute(Map<Integer,Call> allCalls,
			Map<Integer,Call> blockedCalls,
			Object controller,
			String trace,
			String configurationDescription) {
	trace = testCall.execute(allCalls, blockedCalls, controller, trace, configurationDescription);
	testStmt.execute(allCalls, blockedCalls, controller, trace, configurationDescription);
    }

    TestCall testCall() {
	return testCall;
    }

    TestStmt stmt() {
	return testStmt;
    }

    public String toString() {
	return "Prefix("+testCall+","+testStmt+")";
    }
}

    
    
