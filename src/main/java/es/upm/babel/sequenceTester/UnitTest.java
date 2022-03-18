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
  private boolean shortFailureMessages = false;
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
    org.junit.jupiter.api.Assertions.fail(msg);
  }
  
  void setShortFailureMessages(boolean mode) {
    shortFailureMessages = mode;
  }

  /**
   * Indicate a failure in the testing framework (i.e., not an error
   * in the tested program but rather in the test system).
   */
  public static void failTestFramework(String msg) {
    failTest("\n\n*** Failure in testing framework: (CONTACTA PROFESORES):\n"+msg+"\nError context:\nCall trace:\n"+mkTrace());
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
    failTest("\n\n*** Test is syntactically incorrect (CONTACTA PROFESORES):\n"+msg+"\nCall trace:\n"+mkTrace());
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

  public static String mkTrace() {
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
  
  public static String mkErrorTrace() {
    return "\nTrace (error detectado en la ultima linea):\n\n"+mkTrace()+"\n\n";
  }

  public void finish() {
    if (unblockedCalls != null && unblockedCalls.size() > 0)
      Call.checkExceptions(unblockedCalls);
    for (Call<?> call : allCreatedCalls) {
      if (!call.hasStarted()) {
        failTestSyntax("call "+call+" was created but never executed");
      }
    }
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
