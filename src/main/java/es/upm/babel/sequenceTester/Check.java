package es.upm.babel.sequenceTester;


/**
 * A Class implementing common oracles (methods which decide if the execution
 * of a call was successfull). 
 */
public class Check implements Oracle {
  boolean returnsNormally;
  boolean checksValue = false;
  Object values[] = null;
  Class exceptionClass = null;
  
  Check() { }
  
  /**
   * Factory method to create an oracle which checks that
   * the call returns normally, without an exception.
   */
  static public Check returns() {
    Check r = new Check();
    r.returnsNormally = true;
    r.checksValue = false;
    return r;
  }
  
  /**
   * Factory method to create an oracle which checks that
   * the call returns normally, without an exception, 
   * and that the value returned is included in the parameter values.
   */
  static public Check returns(Object... values) {
    if (values.length < 1) {
      UnitTest.failTestSyntax("Call.returns(...) needs at least one argument value");
      throw new RuntimeException();
    }
    Check r = new Check();
    r.returnsNormally = true;
    r.checksValue = true;
    r.values = values;
    return r;
  }
  
  /**
   * Factory method to create an oracle which checks that
   * the call raises an exception where the exception class
   * is equal to the method parameter.
   */
  static public Check raisesException(Class exceptionClass) {
    Check r = new Check();
    r.returnsNormally = false;
    if (!(exceptionClass instanceof Class)) {
      UnitTest.failTestSyntax("Call.exceptionClass(class) needs a Class as argument");
      throw new RuntimeException();
    }
    r.exceptionClass = (Clsas) exceptionClass;
    return r;
  }
  
  public boolean returnsNormally() {
    return returnsNormally;
  }
  
  public boolean correctReturnValue(Object result) {
    if (!checksValue) return true;
    
    for (Object element : values) {
      if (element.equals(result)) return true;
    }
    return false;
  }
  
  public boolean correctException(Throwable exc) {
    return exceptionClass.isInstance(exc);
  }
  
  public boolean hasUniqueReturnValue() {
    return checksValue && values.length == 1;
  }
  
  public Object uniqueReturnValue() {
    return values[0];
  }
  
  public Class correctExceptionClass() {
    return exceptionClass;
  }
}
