package es.upm.babel.sequenceTester;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.io.StringWriter;
import java.io.PrintWriter;
import org.junit.jupiter.api.*;


/**
 * Represents a unit test which embedding a unit test statement.
 */
public class UnitTest {
  static String testName;
  static Map<String,Boolean> testResults;
  static UnitTest currentTest = null;
  
  private int n = 1;
  private String trace = "\nCall trace:\n";
  private Object state = null;
  private String configurationDescription;
  protected Set<Call<?>> allCalls = null;
  protected Set<Call<?>> blockedCalls = null;
  protected Set<Call<?>> unblockedCalls = null;
  protected Call<?> lastCalls = null;
  protected Set<Call<?>> calls = null;
  
  /**
   * Constructs a unit test.
   * 
   * @param name the name of the call sequence.
   * @param stmt a test statement
   * @throws RuntimeException if the test statement is syntactically
   * invalid (e.g., unblocks calls that have already been unblocked).
   * @see TestStmt
   * @see Call
   */
  public UnitTest(String name) {
    this.testName = name;
    if (testResults == null)
      testResults = new HashMap<>();
    allCalls = new HashSet<Call<?>>();
    blockedCalls = new HashSet<Call<?>>();
    unblockedCalls = new HashSet<Call<?>>();
    currentTest = this;
    Call.reset();
    Config.installTestConfig();
  }
  
  public UnitTest setConfigurationDescription(String desc) {
    configurationDescription = desc;
    return this;
  }
  
  public String getConfigurationDescription(String desc) {
    return configurationDescription;
  }
  

  /**
   * Returns the configuration description.
   * @return the configuration description.
   */
  public String getConfigurationDescription() {
    return configurationDescription;
  }
  
  /**
   * Indicate a unit test failer.
   */
  public static void failTest(String msg) {
    String failMessage = "\n\n*** Error en la prueba "+testName+":\n"+msg;
    System.out.println(failMessage);
    System.out.println(currentTest.trace);
    org.junit.jupiter.api.Assertions.assertTrue(false,failMessage);
  }
  
  /**
   * Indicate a failure in the testing framework (i.e., not an error
   * in the tested program but rather in the test system).
   */
  public static void failTestFramework(String msg) {
    StringWriter errors = new StringWriter();
    new Throwable().printStackTrace(new PrintWriter(errors));
    String stackTrace = errors.toString();
    String message = "\n\n*** Failure in testing framework: (CONTACTA PROFESORES):\n"+msg+"\nError context:\n"+stackTrace+"\n";
    failTest(message);
  }
  
  public void resetUnblocked() {
    unblockedCalls = new HashSet<Call<?>>();
  }

  public Set<Call<?>> unblockedCalls() {
    return unblockedCalls;
  }

  public boolean hasBlockedCalls() {
    return !blockedCalls.isEmpty();
  }

  public void addCalls(List<Call<?>> calls) {
    for (Call<?> call : calls) {
      allCalls.add(call);
      blockedCalls.add(call);
    }
  }
  
  public void calculateUnblocked()
  {
    Set<Call<?>> newUnblockedCalls = new HashSet<Call<?>>();

    for (Call<?> blockedCall : blockedCalls) {
      if (!blockedCall.hasBlocked()) {
        newUnblockedCalls.add(blockedCall);
      }
    }
    blockedCalls.removeAll(newUnblockedCalls);
    unblockedCalls.addAll(newUnblockedCalls);
  }
  
  /**
   * Indicates a syntactic error in a particular test (i.e., not an error
   * in the tested program but rather in the test suite).
   */
  public static void failTestSyntax(String msg) {
    StringWriter errors = new StringWriter();
    new Throwable().printStackTrace(new PrintWriter(errors));
    String stackTrace = errors.toString();
    String message = "\n\n*** Test is syntactically incorrect (CONTACTA PROFESORES):\n"+msg+"\nError context:\n"+stackTrace+"\n";
    failTest(message);
  }
  
  public static void reportTestResults() {
    ArrayList<String> successes = new ArrayList<>();
    ArrayList<String> failures = new ArrayList<>();
    boolean hasErrors = false;
    
    for (Map.Entry<String,Boolean> entry : testResults.entrySet()) {
      String name = entry.getKey();
      Boolean result = entry.getValue();
      if (result) successes.add(name);
      else {
        failures.add(name);
        hasErrors = true;
      }
    }
    
    System.out.println("\n\n========================================\n");
    if (!hasErrors) System.out.println("All tests successful.\n");
    else System.out.println("Some tests failed.\n");
    
    System.out.print("Successes: ");
    for (String testName : successes) System.out.print(testName+" ");
    System.out.println();
    System.out.print("\nFailures: ");
    for (String testName : failures) System.out.print(testName+" ");
    System.out.println("\n\n========================================");
  }
  
  public void extendTrace(List<Call<?>> calls, Set<Call<?>> newUnblocked) {
    // Compute a new trace
    String unblocksString="";
    for (Call<?> unblockedCall : newUnblocked) {
      String callString = unblockedCall.printCallWithReturn();
      if (unblocksString=="") unblocksString=callString;
      else unblocksString+=", "+callString;
    }
    if (unblocksString!="")
      unblocksString = " -- unblocked "+unblocksString;
    
    String callsString="";
    for (Call<?> call : calls) {
      if (callsString != "") callsString += "\n  "+call.printCall();
      else callsString = call.printCall();
    }
    
    String callPlusUnblock;
    if (calls.size() > 1)
      callPlusUnblock = "parallel\n  {\n  "+callsString+"\n  }"+unblocksString;
    else
      callPlusUnblock = callsString+unblocksString;
    
    if (trace != "") 
      trace += "\n  "+callPlusUnblock;
    else
      trace = "  "+callPlusUnblock;
  }
  
  public static String mkTrace() {
    return "\nTrace (error detectado en la ultima linea):\n"+currentTest.trace+"\n\n";
  }

  public void finish() {
    if (unblockedCalls != null && unblockedCalls.size() > 0)
      Call.checkExceptions(unblockedCalls);
  }

  /**
   * Return the test (context) state.
   */
  public Object getTestState() {
    return state;
  }
  
  /**
   * Sets the test (context) state.
   */
  public void setTestState(Object state) {
    this.state = state;
  }
}
