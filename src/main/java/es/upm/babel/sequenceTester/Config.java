package es.upm.babel.sequenceTester;

/** 
 * Permits a number of configuration parameters, either on a unit test basis or for the duration of 
 * a config class which is created by the UnitTest class constructor.
 */

public class Config {
  private static int WaitTime = 250;
  private static boolean randomize = false;
  static Config currentConfig = new Config();

  int testWaitTime;
  boolean testRandomize;

  Config() {
    testWaitTime = WaitTime;
    testRandomize = randomize;
  }

  /**
   * Installs a new test configuration
   */
  static void installTestConfig() {
    currentConfig = new Config();
  }

  /**
   * Returns the standard maximum wait time (in milliseconds)
   */
  public static int getWaitTime() {
    return WaitTime;
  }

  /**
   * Sets the standard maximum wait time (in milliseconds)
   */
  public static void setWaitTime(int waitTime) {
    WaitTime = waitTime;
  }

  /**
   * Returns the test maximum wait time (in milliseconds)
   */
  public static int getTestWaitTime() {
    return currentConfig.testWaitTime;
  }

  /**
   * Sets the confis maximum wait time (in milliseconds)
   */
  public static void setTestWaitTime(int waitTime) {
    currentConfig.testWaitTime = waitTime;
  }

  /**
   * Returns the value of the randomize flag (i.e., whether to randomize the 
   * order of running of multiple calls).
   */
  public static boolean getRandomize() {
    return randomize;
  }

  /**
   * Sets the randomize flag.
   */
  public static void setRandomize(boolean waitTime) {
    randomize = waitTime;
  }

  /**
   * Returns the value of the randomize flag (i.e., whether to randomize the 
   * order of running of multiple calls).
   */
  public static boolean getTestRandomize() {
    return currentConfig.testRandomize;
  }

  /**
   * Sets the randomize flag.
   */
  public static void setTestRandomize(boolean waitTime) {
    currentConfig.testRandomize = waitTime;
  }
}

