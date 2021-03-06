package es.upm.babel.sequenceTester;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;


/**
 * A Class implementing common oracles (methods which decide if the execution
 * of a call was successfull). 
 */
public class Check<V> implements Oracle<V> {
  boolean returnsNormally;
  boolean checksValue = false;
  List<V> values = null;
  Class exceptionClass = null;
  Predicate<V> pred = null;
  
  Check() { }
  
  /**
   * Factory method to create an oracle which checks that
   * the call returns normally, without an exception.
   */
  public static <V> Check<V> returns() {
    Check<V> r = new Check<>();
    r.returnsNormally = true;
    r.checksValue = false;
    return r;
  }
  
  /**
   * Factory method to create an oracle which checks that
   * the call returns normally, without an exception, 
   * and that the value returned is included in the parameter values.
   */
  public static <V> Check<V> returns(List<V> values) {
    if (values.size() < 1) {
      UnitTest.failTestSyntax("Call.returns(...) needs at least one argument value");
      throw new RuntimeException();
    }
    Check<V> r = new Check<>();
    r.returnsNormally = true;
    r.checksValue = true;
    r.values = values;
    return r;
  }
  
  /**
   * Factory method to create an oracle which checks that
   * the call returns normally, without an exception, 
   * and that the value returned is included in the parameter values.
   */
  public static <V> Check<V> returns(V value) {
    List<V> values = new ArrayList<>();
    values.add(value);
    return returns(values);
  }
  
  /**
   * Factory method to create an oracle which checks that
   * the call raises an exception where the exception class
   * is equal to the method parameter.
   */
  public static <V> Check<V> raisesException(Class exceptionClass) {
    Check<V> r = new Check<V>();
    r.returnsNormally = false;
    if (!(exceptionClass instanceof Class)) {
      UnitTest.failTestSyntax("Call.exceptionClass(class) needs a Class as argument");
      throw new RuntimeException();
    }
    r.exceptionClass = (Class) exceptionClass;
    return r;
  }
  
  public boolean returnsNormally() {
    return returnsNormally;
  }
  
  public boolean checksReturnValue() {
    return checksValue;
  }

  @SuppressWarnings("unchecked")
  public boolean correctReturnValue(Object result) {
    V returnValue = null;

    try {
      returnValue = (V) result;
    } catch (ClassCastException exc) {
      UnitTest.failTestSyntax("cannot convert return value to the correct type");
    }

    if (pred != null)
      return pred.test(returnValue);

    for (V element : values) {
      if (element.equals(returnValue)) return true;
    }

    return false;
  }
  
  public boolean correctException(Throwable exc) {
    return exceptionClass.isInstance(exc);
  }
  
  public boolean hasUniqueReturnValue() {
    return checksValue && values != null && values.size() == 1;
  }
  
  public V uniqueReturnValue() {
    return values.get(0);
  }
  
  public Class correctExceptionClass() {
    return exceptionClass;
  }

  public String error() {
    return null;
  }

  public static <V> Oracle<V> lambda(Predicate<V> pred) {
    Check<V> r = new Check<>();
    r.pred = pred;
    r.returnsNormally = true;
    r.checksValue = true;
    return r;
  }
}
