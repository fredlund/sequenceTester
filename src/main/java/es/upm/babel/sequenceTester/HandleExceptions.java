package es.upm.babel.sequenceTester;

import org.junit.jupiter.api.extension.TestExecutionExceptionHandler;
import org.junit.jupiter.api.extension.ExtensionContext;

public class HandleExceptions implements TestExecutionExceptionHandler {

    @Override
    public void handleTestExecutionException(ExtensionContext context, Throwable throwable) throws Throwable {
      if (throwable instanceof org.opentest4j.AssertionFailedError) {
        String msg = throwable.getMessage();
        if (msg == null) msg = "";
        msg += UnitTest.currentTest.mkErrorTrace();
        // System.out.println(msg);
        org.opentest4j.AssertionFailedError newThrowable = new org.opentest4j.AssertionFailedError(msg);
        newThrowable.setStackTrace(throwable.getStackTrace());
        throw newThrowable;
      } else throw throwable;
    }
}
