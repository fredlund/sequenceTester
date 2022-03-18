package es.upm.babel.sequenceTester;

import org.junit.jupiter.api.extension.TestExecutionExceptionHandler;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * Provides call traces for test failures.
 */
public class HandleExceptions implements TestExecutionExceptionHandler {

    @Override
    public void handleTestExecutionException(ExtensionContext context, Throwable throwable) throws Throwable {
      if (throwable instanceof org.opentest4j.AssertionFailedError) {
        String msg = throwable.getMessage();
        if (msg == null) msg = "";
        msg += "\n"+UnitTest.getCurrentTest().errorTrace(UnitTest.ErrorLocation.LASTLINE);
        // System.out.println(msg);
        org.opentest4j.AssertionFailedError newThrowable = new org.opentest4j.AssertionFailedError(msg);
        newThrowable.setStackTrace(throwable.getStackTrace());
        throw newThrowable;
      } else if (throwable instanceof InternalException) {
	InternalException error = (InternalException) throwable;
        String msg = "*** INTERNAL ERROR ***"+error.getMessage();
	UnitTest.ErrorLocation loc = error.getErrorLocation();
        if (msg == null) msg = "";
	if (loc == null) loc = UnitTest.ErrorLocation.LASTLINE;
        msg += "\n"+UnitTest.getCurrentTest().errorTrace(loc);
        // System.out.println(msg);
        org.opentest4j.AssertionFailedError newThrowable = new org.opentest4j.AssertionFailedError(msg);
        newThrowable.setStackTrace(throwable.getStackTrace());
        throw newThrowable;
      } else throw throwable;
    }
}
