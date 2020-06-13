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
  static Map<String,Call> namedCalls;

  TestStmt stmt;
  String trace="\nCall trace:\n";
  String name;
  Object controller;
  String configurationDescription;
  
  Map<Integer,Call> allCalls=null;
  Map<Integer,Call> blockedCalls=null;
  
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

    allCalls = new HashMap<Integer,Call>();
    blockedCalls = new HashMap<Integer,Call>();
    namedCalls = new HashMap<String,Call>();
    
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
    
    Map<Integer,Call> active = new HashMap<Integer,Call>();
    Set<Object> blockedUsers = new HashSet<Object>();
    int counter = 1;
    checkSoundNess(name,stmt,active,blockedUsers,counter);
    if (checker != null) checker.check(name,stmt);
  }
  
  // blockedUsers are the users that are currently executing a call, and so cannot execute another until
  // that call returns.
  // active are the currently active calls.
  private int checkSoundNess(String name,TestStmt stmt,Map<Integer,Call> active,Set<Object> blockedUsers,int counter) {

    if (stmt instanceof Prefix) {
      Prefix prefix = (Prefix) stmt;
      TestCall testCall = prefix.testCall();
      TestStmt continuation = prefix.stmt();
      
      // Update blocked users and active calls
      counter =
        checkAndUpdateActiveBlocked
        (name,
         testCall.calls(),
         active,
         blockedUsers,
         counter);

      // Check that unblocks only refers to active calls and update blocked users
      checkUnblocksActive(testCall.calls(), testCall.unblocks(), blockedUsers, active);

      // Remove unblocked calls from active
      for (Pair<Integer,Oracle> pair : testCall.unblocks().mustUnblock()) {
        Integer unblockedId = pair.getLeft();
        active.remove(unblockedId);
      }
      checkSoundNess(name,continuation,active,blockedUsers,counter);
    }

    else if (stmt instanceof Branches) {
      Branches b = (Branches) stmt;
      
      // Update blocked and active calls
      counter =
        checkAndUpdateActiveBlocked
        (name,
         b.calls(),
         active,
         blockedUsers,
         counter);
      
      for (Alternative alt : b.alternatives()) {
        Map<Integer,Call> newActive = new HashMap<Integer,Call>();
        Set<Object> newBlockedUsers = new HashSet<Object>();
        
        // Copy active and blocked users
        for (Map.Entry<Integer,Call> mEntry : active.entrySet()) {
          newActive.put(mEntry.getKey(),mEntry.getValue());
        }
        for (Object obj : blockedUsers) {
          newBlockedUsers.add(obj);
        }

        // Check that unblocks only refers to active calls and update blocked users
        checkUnblocksActive(b.calls(), alt.unblocks(), newBlockedUsers, newActive);

        for (Pair<Integer,Oracle> pair : alt.unblocks().mustUnblock()) {
          Integer unblockedId = pair.getLeft();
          newActive.remove(unblockedId);
        }
        counter = checkSoundNess(name,alt.continuation(),newActive,newBlockedUsers,counter);
      }
    }
    
    return counter;
  }
  
  private void checkUnblocksActive(Call[] calls, Unblocks unblocks, Set<Object> blockedUsers, Map<Integer,Call> active) {
    for (Pair<Integer,Oracle> pair : unblocks.mustUnblock()) {
      Integer unblockedId = pair.getLeft();
      if (!active.containsKey(unblockedId)) {
        failTestSyntax
          ("*** Test "+name+" is incorrect:\n"+
           Call.printCalls(calls)+
           " unblocks "+unblockedId+
           " which is not in the active set "+active+"\n");
      }
      Call call = active.get(unblockedId);
      Object user = call.getUser();
      if (user != null)
        blockedUsers.remove(user);
    }
      
    for (Pair<Integer,Oracle> pair : unblocks.mayUnblock()) {
      Integer unblockedId = pair.getLeft();
      if (!active.containsKey(unblockedId)) {
        failTestSyntax
          ("*** Test "+name+" is incorrect:\n"+
           Call.printCalls(calls)+
           " may unblocks "+unblockedId+
           " which is not in the active set "+active+"\n");
      }
    }
  }
      

  private int checkAndUpdateActiveBlocked(String name,
                                          Call[] calls,
                                          Map<Integer,Call> active,
                                          Set<Object> blockedUsers,
                                          int counter) {
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
      if (call.getCallId() != counter) {
        System.out.println
          ("*** Test "+name+" is incorrect:\n"+
           "current counter is "+counter+
           " but call "+call+" has id "+call.getCallId()+"\n");
        try { Thread.sleep(100); }
        catch (InterruptedException exc) {};
        failTestSyntax
          ("*** Test "+name+" is incorrect:\n"+
           "current counter is "+counter+
           " but call "+call+" has id "+call.getCallId()+"\n");
      }
      active.put(counter,call);
      ++counter;
    }
    return counter;
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

  public static Object returnValue(String callName) {
    Call call = namedCalls.get(callName);
    
    if (call == null) {
      failTestFramework("no call named "+callName+" exists");
      return null;
    }

    if (!call.returned()) {
      failTestFramework("call "+callName+" has not returned");
      return null;
    }

    return call.returnValue();
  }
}

