package es.upm.babel.sequenceTester;


/**
 * The type of oracles, i.e., classes which checks the result of the call.
 */
public interface Oracle {
  
  /**
   * Does the oracle specify that the method should return normally,
   * i.e., without raising an exception.
   */
  public boolean returnsNormally();

  /**
   * Checks whether the valued returned by the call is correct.
   *
   * @return a boolean corresponding to whether the call
   * returned the correct value.
   */
  public boolean correctReturnValue(Object result);

  /**
   * Checks whether the exception raised by the call is correct.
   *
   * @return a boolean corresponding to whether the call
   * raised the correct exception.
   */
  public boolean correctException(Throwable exc);
  
  /**
   * Does the oracle accept a single return value, and is it capable of return it?
   */
  public boolean hasUniqueReturnValue();

  /**
   * Returns the single return value accepted by the oracle (if hasUniqueReturnValue returned true).
   */
  public Object uniqueReturnValue();

  /**
   * Return a class corresponding to the exception accepted by
   * the oracle.
   */
  public Class correctExceptionClass();
}
