package es.upm.babel.sequenceTester;

/**
 * The type of oracles, i.e., classes which check the execution of a call.
 */
public interface Result {
  
  /**
   * Does the oracle implement a return value check?
   *
   * @return a boolean corresponding to whether the call specified a return value oracle.
   */
  public boolean hasReturnCheck();
  /**
   * Should the call (ever) return? 
   * Note that a call to this method will return true even if the call
   * will initially block, and later be unblocked by a later call.
   *
   * @return a boolean corresponding to whether the call specified that the call
   * should (ever) return. 
   */
  public boolean shouldReturn();
  /**
   * Does the call provide an oracle to check the 
   * returned value from the call? 
   *
   * @return a boolean corresponding to whether the call specified
   * an oracle to check the value returned by the call.
   */
  public boolean checksValue();
  
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
   * Returns the single return value accepted by the oracle.
   */
  public Object uniqueReturnValue();
  /**
   * Return a class corresponding to the exception accepted by
   * the oracle.
   */
  public Class correctExceptionClass();
}
