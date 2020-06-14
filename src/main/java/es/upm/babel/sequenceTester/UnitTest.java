package es.upm.babel.sequenceTester;

import java.util.Map;
import java.util.Map.Entry;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.io.StringWriter;
import java.io.PrintWriter;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;


/**
 * Represents a unit test which embedding a unit test statement.
 */
public class UnitTest {
  static TestCaseChecker checker = null;
  static String testName;

  TestStmt stmt;
  String trace="\nCall trace:\n";
  String name;
  Object controller;
  String configurationDescription;
  
  Set<Call> allCalls=null;
  Set<Call> blockedCalls=null;
  
  /**
   * Constructs a call sequence
   * 
   * @param name the name of the call sequence.
   * @param controller an object that is communicated to every call using the method
   * {@link Call#setController(Object)}.
   * @param stmt a test statement
   * @param configurationDescription a string describing the configuration
   * @throws RuntimeException if the test statement is syntactically
   * invalid (e.g., unblocks calls that have already been unblocked).
   * @see TestStmt
   * @see Call
   */
  public UnitTest(String name, String configurationDescription, Object controller, TestStmt stmt) {
    this.name = name;
    this.configurationDescription = configurationDescription;
    this.controller = controller;
    this.stmt = stmt;
  }
  
  /**
   * Constructs a call sequence.
   * 
   * @param name the name of the call sequence.
   * @param controller an object that is communicated to every call using the method
   * {@link Call#setController(Object)}.
   * @param testCalls an array of test calls
   * @param configurationDescription a string describing the configuration
   * @throws RuntimeException if the call sequence is syntactically
   * invalid (e.g., unblocks calls that have already been unblocked).
   * @see Call
   */
  public UnitTest(String name, String configurationDescription, Object controller, TestCall... testCalls) {
    this.name = name;
    this.configurationDescription = configurationDescription;
    this.controller = controller;
    this.stmt = Util.sequence(testCalls);
  }
  
  /**
   * Defines the test name and does other internal bookkeeping that is required before
   * the arguments of the UnitTest constructor is called in the tests.
   */
  public static void setupTest(String name) {
    System.out.println("\n\nTesting "+name);
    
    // Set the test name (needed for the evaluation of arguments to the UnitTest constructor)
    testName = name;
    
    // This creates a new map for the symbolic variables.
    Call.reset();
  }

  /**
   * Executes a test statement. The method checks that calls return correct values
   * (according to the call sequence specification), and that calls are blocked
   * and unblocked correctly.
   */
  public void run() {
    testName = name;
    checkSoundNess(name,stmt);

    allCalls = new HashSet<Call>();
    blockedCalls = new HashSet<Call>();
    
    if (name.equals("desarollo")) {
      System.out.println
        ("\n*** Error: el sistema de entrega todavia esta en desarollo.");
      throw new RuntimeException();
    }
    
    stmt.execute
      (allCalls,
       blockedCalls,
       controller,
       "",
       configurationDescription);

    System.out.println("\nFinished testing "+name+"\n");
  }
  
  boolean contains(int i, int[] calls) {
    for (Integer elem : calls)
      if (i==elem) return true;
    return false;
  }
  
  /**
   * Installs a custom checker to use for deciding whether a test statement
   * is (syntactically) valid, in addition to the standard syntactic checker.
   * @param checker the name of the checker.
   */
  public static void installChecker(TestCaseChecker checker) {
    UnitTest.checker = checker;
  }
  
  // Soundness checks for sequences of calls
  private void checkSoundNess(String name, TestStmt stmt) {
    
    Set<String> active = new HashSet<String>();
    Set<Object> blockedUsers = new HashSet<Object>();
    checkSoundNess(name,stmt,active,blockedUsers);
    if (checker != null) checker.check(name,stmt);
  }
  
  // blockedUsers are the users that are currently executing a call, and so cannot execute another until
  // that call returns.
  // active are the currently active calls.
  private void checkSoundNess(String name,TestStmt stmt,Set<String> active,Set<Object> blockedUsers) {

    if (stmt instanceof Prefix) {
      Prefix prefix = (Prefix) stmt;
      TestCall testCall = prefix.testCall();
      TestStmt continuation = prefix.stmt();
      
      // Update blocked users and active calls
      checkAndUpdateActiveBlocked(name,testCall.calls(),active,blockedUsers);

      // Check that unblocks only refers to active calls and update blocked users
      checkUnblocksActive(testCall.calls(), testCall.unblocks(), blockedUsers, active);

      // Remove unblocked calls from active
      for (String unblocked : testCall.unblocks().mustUnblock().keySet()) {
        active.remove(unblocked);
      }
      checkSoundNess(name,continuation,active,blockedUsers);
    }

    else if (stmt instanceof Branches) {
      Branches b = (Branches) stmt;
      
      // Update blocked and active calls
      checkAndUpdateActiveBlocked(name,b.calls(),active,blockedUsers);
      
      for (Alternative alt : b.alternatives()) {
        Set<String> newActive = new HashSet<>(active);
        Set<Object> newBlockedUsers = new HashSet<Object>(blockedUsers);

        // Check that unblocks only refers to active calls and update blocked users
        checkUnblocksActive(b.calls(), alt.unblocks(), newBlockedUsers, newActive);

        for (String unblocked : alt.unblocks().mustUnblock().keySet()) {
          newActive.remove(unblocked);
        }
        checkSoundNess(name,alt.continuation(),newActive,newBlockedUsers);
      }
    }
  }
  
  private void checkUnblocksActive(Call[] calls, Unblocks unblocks, Set<Object> blockedUsers, Set<String> active) {
    for (String unblocked : unblocks.mustUnblock().keySet()) {
      if (!active.contains(unblocked)) {
        failTestSyntax
          ("*** Test "+name+" is incorrect:\n"+
           Call.printCalls(calls)+
           " unblocks "+unblocked+
           " which is not in the active set "+active+"\n");
      }
      Call call = Call.lookupCall(unblocked);
      Object user = call.getUser();
      if (user != null)
        blockedUsers.remove(user);
    }
      
    for (String unblocked : unblocks.mayUnblock().keySet()) {
      if (!active.contains(unblocked)) {
        failTestSyntax
          ("*** Test "+name+" is incorrect:\n"+
           Call.printCalls(calls)+
           " may unblocks "+unblocked+
           " which is not in the active set "+active+"\n");
      }
    }
  }
      

  private void checkAndUpdateActiveBlocked(String name, Call[] calls, Set<String> active, Set<Object> blockedUsers) {
    for (Call call : calls) {
      Object user = call.getUser();
      if (user != null && blockedUsers.contains(user)) {
        failTestSyntax
          ("*** Test "+name+" is incorrect:\n"+
           "user "+user+" in call "+call+
           " is blocked"+"\n");
      }
      blockedUsers.add(user);
    }
    
    for (Call call : calls) {
      active.add(call.getSymbolicName());
    }
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
    Assertions.assertTrue(false,failMessage);
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
  
  /**
   * Starts a controller with a given name and the call to create it.
   */
  public static Object startController(String name, Call call) {
    // For now we have to reset the call counter since startController actions are not counted
    // This is ugly and should be changed to a more flexible policy for action naming...
    Call.reset();

    call.execute();
    if (call.hasBlocked())
      UnitTest.failTest("creating an instance of "+name+" blocks");
    if (call.raisedException())
      UnitTest.failTest
        ("when creating an instance of "+name+
         " the exception "+call.getException()+" was raised");
    return call.returnValue();
  }
}

