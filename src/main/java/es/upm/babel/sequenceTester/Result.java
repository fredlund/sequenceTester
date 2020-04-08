package es.upm.babel.sequenceTester;

public interface Result {

    /**
     * Does the call provide an oracle which checks the return value?
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

  // Temporary and ugly
  public Object[] correctReturnValues();
  public Class correctExceptionClass();
}
