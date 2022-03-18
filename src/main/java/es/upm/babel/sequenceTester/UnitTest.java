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
  
  private final int n = 1;
  private String trace = "\nCall trace:\n";
  private Object state = null;
  private String configurationDescription;
  private ArrayList<Pair<List<Call<?>>,Set<Call<?>>>> history;
  protected Set<Call<?>> allCalls = null;
  protected Set<Call<?>> allCreatedCalls = null;
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
    testName = name;
    if (testResults == null)
      testResults = new HashMap<>();
    allCalls = new HashSet<Call<?>>();
    allCreatedCalls = new HashSet<Call<?>>();
    blockedCalls = new HashSet<Call<?>>();
    unblockedCalls = new HashSet<Call<?>>();
    history = new ArrayList<Pair<List<Call<?>>,Set<Call<?>>>>();
    currentTest = this;
    Call.reset();
    Config.installTestConfig();
  }
  
  protected enum ErrorLocation {
    LASTLINE, INSIDE, AFTER
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
   * Indicates a syntactic error in a particular test (i.e., not an error
   * in the tested program but rather in the test suite).
   */
  public static void failTestSyntax(String msg, ErrorLocation loc) {
    failTestSyntax(msg, loc, false);
  }
  
  public static void failTestSyntax(String msg, ErrorLocation loc, boolean directFail) {
    if (directFail) {
      failTest("\n\n*** Test is syntactically incorrect (CONTACTA PROFESORES):\n"+msg, true, loc);
    } else throw new InternalException("\n\n*** Test is syntactically incorrect (CONTACTA PROFESORES):\n"+msg, loc);
  }
  
  /**
   * Indicate a failure in the testing framework (i.e., not an error
   * in the tested program but rather in the test system).
   */
  public static void failTestFramework(String msg, ErrorLocation loc) {
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
  
  protected void resetUnblocked() {
    unblockedCalls = new HashSet<Call<?>>();
  }

  /**
   * Returns the set of calls unblocked by executing this call -- or
   * other calls executed in parallel with this call.
   */
  public Set<Call<?>> unblockedCalls() {
    return unblockedCalls;
  }

  /**
   * Are there blocked calls?
   */
  public boolean hasBlockedCalls() {
    return !blockedCalls.isEmpty();
  }

  protected void addCalls(List<Call<?>> calls) {
    for (Call<?> call : calls) {
      allCalls.add(call);
      blockedCalls.add(call);
    }
  }
  
  protected void calculateUnblocked()
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
    history.add(new Pair<List<Call<?>>,Set<Call<?>>>(calls,newUnblocked));
  }

  protected static String mkTrace() {
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
  
  public void finish() {
    // Check if the last call resulted in an exception
    if (unblockedCalls != null && unblockedCalls.size() > 0)
      Call.checkExceptions(unblockedCalls, true);

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
