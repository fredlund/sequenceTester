package es.upm.babel.sequenceTester;


/**
 * A Class implementing an oracle predicate.
 */
public class Predicate implements Oracle {
  java.util.function.Predicate<Object> pred;

  Predicate(java.util.function.Predicate<Object> pred) {
    this.pred = pred;
  }

  public boolean returnsNormally() {
    return true;
  }

  public boolean correctReturnValue(Object result) {
    return pred.test(result);
  }

  public boolean correctException(Throwable exc) {
    return false;
  }
  
  public String error() {
    return null;
  }
  
  public boolean hasUniqueReturnValue() {
    return false;
  }

  public Object uniqueReturnValue() {
    return null;
  }

  public Class correctExceptionClass() {
    return null;
  }
  
}
  
