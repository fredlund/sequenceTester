package es.upm.babel.sequenceTester;


/**
 * A Class implementing common oracles (methods which decide if the execution
 * of a call was successfull). 
 */
public class Return implements Result {
  // Does the oracle check the return value?
  boolean hasReturnCheck = false;
  // Should the call return?
  boolean shouldReturn = true;
  // Does the oracle check the return value?
  boolean checksValue = false;
  // Permitted return values
  Object values[] = null;
  // Permitted exception
  Class exceptionClass = null;
  
  Return() { }
  
  /**
   * Factory method to create an oracle which checks that
   * the call returns normally, without an exception, 
   * and the value returned is included in the parameter values.
   */
  static public Return returns(Object... values) {
    Return r = new Return();
    r.hasReturnCheck = true;
    r.shouldReturn = true;
    r.checksValue = true;
    if (shouldReturn)
      r.values = values;
    else if (values.length != 1) {
      System.out.println
        ("*** Error in test: shouldReturn=false requires one argument");
      throw new RuntimeException();
    } else {
      r.exceptionClass = (Class) values[0];
    }
    return r;
  }
  
  /**
   * Factory method to create an oracle which checks that
   * the call raises an exception where the exception class
   * is equal to the method parameter.
   */
  static public Return raisesException(Class exceptionClass) {
    Return r = new Return();
    r.hasReturnCheck = true;
    r.shouldReturn = true;
    r.checksValue = true;
    if (shouldReturn)
      r.values = values;
    else if (values.length != 1) {
      System.out.println
        ("*** Error in test: shouldReturn=false requires one argument");
      throw new RuntimeException();
    } else {
      r.exceptionClass = (Class) values[0];
    }
    return r;
    exceptionClass;
  }
  
  static public Return shouldReturn(boolean shouldReturn) {
    Return r = new Return();
    r.hasReturnCheck = true;
    r.shouldReturn = shouldReturn;
    r.checksValue = false;
    return r;
  }
  
  static public Return noCheck() {
    Return r = new Return();
    r.hasReturnCheck = false;
    return r;
  }
  
  public boolean hasReturnCheck() {
    return hasReturnCheck;
  }
  
  public boolean shouldReturn() {
    return shouldReturn;
  }
  
  public boolean checksValue() {
    return checksValue;
  }
  
  public boolean correctReturnValue(Object result) {
    for (Object element : values) {
      if (element.equals(result)) return true;
    }
    return false;
  }
  
  public boolean correctException(Throwable exc) {
    return exceptionClass.isInstance(exc);
  }
  
  
  public boolean hasUniqueReturnValue() {
    return values != null && values.length == 1;
  }
  
  public Object uniqueReturnValue() {
    return values[0];
  }
  
  public Class correctExceptionClass() {
    return exceptionClass;
  }
}
