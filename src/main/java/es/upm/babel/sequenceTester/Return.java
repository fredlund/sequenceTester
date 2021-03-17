package es.upm.babel.sequenceTester;

public class Return<E> {
  boolean hasReturnValue = false;
  private E returnValue;

  /**
   * Sets the return value of the call (if any).
   */
  public void setReturnValue(E returnValue) {
    if (hasReturnValue) {
      UnitTest.failTestSyntax("setting return value twice");
    } else {
      this.returnValue = returnValue;
      this.hasReturnValue = true;
    }
  }

  /**
   * Is there a return value?
   */
  public boolean hasReturnValue() {
    return this.hasReturnValue;
  }
  
  /**
   * Gets the return value of the call (and otherwise fails).
   */
  public E getReturnValue() {
    if (!hasReturnValue) {
      UnitTest.failTestSyntax("no return value set");
      return null;
    } else {
      return this.returnValue;
    }
  }

  
}
