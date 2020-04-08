package es.upm.babel.sequenceTester;

import java.util.Map;


public class Nil implements TestStmt {
    public Nil() { }

    public void execute(Map<Integer,Call> allCalls,
			Map<Integer,Call> blockedCalls,
			Object controller,
			String trace,
			String configurationDescription) {
    }

    public String toString() {
	return "Nil";
    }
}

    
    
