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
 * Support code for executing a Junit 5 tests composed of calls, i.e., instances
 * of the Call class.
 */
public class UnitTest {
  static String testName;
  static Map<String,Boolean> testResults;
  static UnitTest currentTest = null;
  
  private final int n = 1;
  private String trace = "\nCall trace:\n";
  private Object state = null;
  private String configurationDescription;
  private ArrayList<Pair<List<Call<?>>,Set<Call<?>>>> history =
    new ArrayList<Pair<List<Call<?>>,Set<Call<?>>>>();

  // All calls created through invoking the Call constructor
  private Set<Call<?>> allCreatedCalls = new HashSet<>();

  // All executed calls
  private Set<Call<?>> allCalls = new HashSet<>();

  // All calls unblocked
  private Set<Call<?>> allUnblockedCalls = new HashSet<>();

  // All calls currently blocked
  private Set<Call<?>> blockedCalls = new HashSet<>();

  // All calls that were unblocked by the execution of the last calls
  private Set<Call<?>> lastUnblockedCalls = null;

  // The last calls executed
  private List<Call<?>> lastCalls = null;

  
  /**
   * Constructs a unit test.
   * This constructor should be called at the begining of each individual test,
   * or in a @BeforeEach method.
   * 
   * @param name the name of the call sequence.
   */
  public UnitTest(String name) {
    testName = name;
    if (testResults == null)
      testResults = new HashMap<>();
    currentTest = this;
    Call.reset();
    Config.installTestConfig();
  }
  
  protected enum ErrorLocation {
    LASTLINE, INSIDE, AFTER
  }

  /**
   * Provides a description of the current test.
   */
  public UnitTest setConfigurationDescription(String desc) {
    configurationDescription = desc;
    return this;
  }
  
  /**
   * Returns the configuration description.
   * @return the configuration description.
   */
  public String getConfigurationDescription() {
    return configurationDescription;
  }
  
  /**
   * Indicates a syntactic error in a particular test (i.e., not an error
   * in the tested program but rather in the test suite).
   */
  static void failTestSyntax(String msg, ErrorLocation loc) {
    failTestSyntax(msg, loc, false);
  }
  
  static void failTestSyntax(String msg, ErrorLocation loc, boolean directFail) {
    if (directFail) {
      failTest("\n\n*** Test is syntactically incorrect (CONTACTA PROFESORES):\n"+msg, true, loc);
    } else throw new InternalException("\n\n*** Test is syntactically incorrect (CONTACTA PROFESORES):\n"+msg, loc);
  }
  
  /**
   * Indicate a failure in the testing framework (i.e., not an error
   * in the tested program but rather in the test system).
   */
  static void failTestFramework(String msg, ErrorLocation loc) {
    throw new InternalException("\n\n*** Failure in testing framework: (CONTACTA PROFESORES):\n"+msg, loc);
  }
  
  /**
   * Indicate a unit test fail.
   */
  protected static void failTest(String msg) {
    failTest(msg, false, ErrorLocation.LASTLINE);
  }
  
  /**
   * Indicate a unit test fail.
   */
  protected static void failTest(String msg, boolean includeTrace, ErrorLocation loc) {
    if (includeTrace) msg += "\n"+errorTrace(loc);
    org.junit.jupiter.api.Assertions.fail(msg);
  }
  
  Set<Call<?>> getAllCreatedCalls() {
    return allCreatedCalls;
  }

  /**
   * Returns the set of all calls unblocked at this point in the execution of the test.
   */
  public Set<Call<?>> getAllUnblockedCalls() {
    return allUnblockedCalls;
  }

  /**
   * Returns the set of all calls that were unblocked by the latest command.
   */
  public Set<Call<?>> getLastUnblockedCalls() {
    return lastUnblockedCalls;
  }

  /**
   * Returns the latest calls executed.
   */
  public List<Call<?>> getLastCalls() {
    if (lastCalls == null)
      UnitTest.failTestSyntax("asserting blocking behaviour before first call",UnitTest.ErrorLocation.INSIDE,false);
    return lastCalls;
  }

  /**
   * Returns the set of all calls blocked at this point in the execution of the test.
   */
  public Set<Call<?>> getBlockedCalls() {
    return blockedCalls;
  }

  /**
   * Are there blocked calls?
   */
  boolean hasBlockedCalls() {
    return !blockedCalls.isEmpty();
  }

  void prepareToRun(List<Call<?>> calls) {
    for (Call<?> call : calls) {
      allCalls.add(call);
      blockedCalls.add(call);
    }
    lastCalls = calls;
    lastUnblockedCalls = new HashSet<Call<?>>();
  }
  
  protected void calculateUnblocked()
  {
    lastUnblockedCalls = new HashSet<Call<?>>();

    for (Call<?> blockedCall : blockedCalls) {
      if (!blockedCall.hasBlocked()) {
        lastUnblockedCalls.add(blockedCall);
      }
    }
    blockedCalls.removeAll(lastUnblockedCalls);
    allUnblockedCalls.addAll(lastUnblockedCalls);
  }
  
  static void reportTestResults() {
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
  
  void extendTrace(List<Call<?>> calls, Set<Call<?>> newUnblocked) {
    history.add(new Pair<List<Call<?>>,Set<Call<?>>>(calls,newUnblocked));
  }

  static String mkTrace() {
    StringBuffer trace = new StringBuffer();
    for (Pair<List<Call<?>>,Set<Call<?>>> historyPair : currentTest.history) {
      List<Call<?>> calls = historyPair.getLeft();
      Set<Call<?>> newUnblocked = historyPair.getRight();
      
      String unblocksString="";
      for (Call<?> unblockedCall : newUnblocked) {
        String callString = unblockedCall.printCallWithReturn();
        if (unblocksString=="") unblocksString=callString;
        else unblocksString+=", "+callString;
      }
      if (unblocksString!="")
        unblocksString = " --> unblocked "+unblocksString;
    
      String callsString = "";
      String indent = calls.size() > 1 ? "  " : "";
      
      for (Call<?> call : calls) {
        if (callsString != "") callsString += "\n"+indent+call.printCall();
        else callsString = indent+call.printCall();
      }
      
      String callPlusUnblock;
      if (calls.size() > 1)
        callPlusUnblock = "===  calls executed in parallel: \n"+callsString+unblocksString;
      else
        callPlusUnblock = callsString+unblocksString;
      
      trace.append(callPlusUnblock+"\n");
    }
    return trace.toString();
  }
  
  /**
   * Method obligatory to call in an @AfterEach clause. 
   */
  public void finish() {
    // Check if the last call resulted in an exception
    if (allUnblockedCalls != null && allUnblockedCalls.size() > 0)
      Call.checkExceptions(allUnblockedCalls, true);

    // Check for created calls that were never executed -- a test syntax error
    for (Call<?> call : allCreatedCalls) {
      if (!call.hasStarted()) {
        failTestSyntax("call "+call+" was created but never executed", ErrorLocation.INSIDE, true);
      }
    }
  }

  protected static String errorTrace(ErrorLocation loc) {
    String locString = "";
    if (loc == ErrorLocation.LASTLINE)
      locString = " (detectado en la ultima linea)";
    else if (loc == ErrorLocation.INSIDE)
      locString = " (detectado dentro la traza)";
    else if (loc == ErrorLocation.AFTER)
      locString = " (detectado despues de la traza)";
    return "Call trace"+locString+":\n\n"+mkTrace()+"\n";
  }

}
