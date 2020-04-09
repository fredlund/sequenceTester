package es.upm.babel.sequenceTester;


/**
 * Class for defining oracles (methods which decide if the execution
 * of a call was successfull.
 */
public class Return implements Result {
    boolean hasReturnCheck = false;
    boolean shouldReturn = true;
    boolean checksValue = false;
    Object values[] = null;
    Class exceptionClass = null;

    public Return() { }

    static public Return returns(boolean shouldReturn, Object... values) {
	Return r = new Return();
	r.hasReturnCheck = true;
	r.shouldReturn = shouldReturn;
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

    static public Return raisesException(Class exceptionClass) {
      return returns(false, exceptionClass);
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

  public Object[] correctReturnValues() {
    return values;
  }

    public boolean correctException(Throwable exc) {
	return exceptionClass.isInstance(exc);
    }

  public Class correctExceptionClass() {
    return exceptionClass;
  }
}
