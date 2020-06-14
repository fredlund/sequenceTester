package es.upm.babel.sequenceTester;

import java.util.Set;


/**
 * A unit test statement that always succeeds.
 */
public class Nil implements TestStmt {
    public Nil() { }

    public void execute(Set<Call> allCalls,
                        Set<Call> blockedCalls,
                        Object controller,
                        String trace,
                        String configurationDescription) {
    }

    public String toString() {
	return "Nil";
    }
}

    
    
