package es.upm.babel.sequenceTester;

public class Alternative {
    int[] unblocks;
    TestStmt continuation;

    public Alternative(int[] unblocks, TestStmt continuation) {
	this.unblocks = unblocks;
	this.continuation = continuation;
    }

    public static Alternative alternative(TestStmt continuation, String... parms) {
	int intparms[] = new int[parms.length];
	for (int i=0; i<parms.length; i++)
	    intparms[i] = Call.lookupCall(parms[i]).name();
	return new Alternative(intparms, continuation);
    }

    public static Alternative alternative(String... parms) {
	int intparms[] = new int[parms.length];
	for (int i=0; i<parms.length; i++)
	    intparms[i] = Call.lookupCall(parms[i]).name();
	return new Alternative(intparms, new Nil());
    }

    public int[] unblocks() {
	return unblocks;
    }

    public TestStmt continuation() {
	return continuation;
    }
}

    
