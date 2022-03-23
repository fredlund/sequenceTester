package es.upm.babel.sequenceTester;

class InternalException extends RuntimeException {
  private final boolean hasTrace;
  private final String trace;
  private UnitTest.ErrorLocation errorLocation;

  public InternalException(String msg, String trace) {
    super(msg);
    this.hasTrace = true;
    this.trace = trace;
  }

  public InternalException(String msg, UnitTest.ErrorLocation errorLocation) {
    super(msg);
    this.hasTrace = false;
    this.trace = null;
    this.errorLocation = errorLocation;
  }

  public UnitTest.ErrorLocation getErrorLocation() {
    return errorLocation;
  }
}

