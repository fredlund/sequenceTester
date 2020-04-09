package es.upm.babel.sequenceTester;

import java.util.Map;
import java.util.Map.Entry;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;


/**
 * Represents a unit test which embedding a unit test statement.
 */
public class UnitTest {
  TestStmt stmt;
  String trace="\nCall trace:\n";
  String name;
  Object controller;
  String configurationDescription;
  static Checker checker = null;
  
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
    checkSoundNess(name,stmt);
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
    this.stmt = sequence(testCalls);
    checkSoundNess(name,stmt);
  }
  
  /**
   * Executes a test statement. The method checks that calls return correct values
   * (according to the call sequence specification), and that calls are blocked
   * and unblocked correctly.
   */
  public void run() {
    allCalls = new HashMap<Integer,Call>();
    blockedCalls = new HashMap<Integer,Call>();
    
    System.out.println("\nTesting test "+name);
    
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
  public static void installChecker(Checker checker) {
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
  
  private int checkSoundNess(String name,TestStmt stmt,Map<Integer,Call> active,Set<Object> blockedUsers,int counter) {
    if (stmt instanceof Prefix) {
      Prefix prefix = (Prefix) stmt;
      TestCall testCall = prefix.testCall();
      TestStmt continuation = prefix.stmt();
      
      counter =
        checkAndUpdateActiveBlocked
        (name,
         testCall.calls(),
         active,
         blockedUsers,
         counter);
      
      for (Integer unblockedId : testCall.mustUnblock()) {
        if (!active.containsKey(unblockedId)) {
          throw new RuntimeException
            ("*** Test "+name+" is incorrect:\n"+
             Call.printCalls(testCall.calls())+
             " unblocks "+unblockedId+
             " which is not in the active set "+active);
        }
        Call call = active.get(unblockedId);
        if (call.bc().user() != null)
          blockedUsers.remove(call.bc().user());
      }
      
      for (Integer unblockedId : testCall.mayUnblock()) {
        if (!active.containsKey(unblockedId)) {
          throw new RuntimeException
            ("*** Test "+name+" is incorrect:\n"+
             Call.printCalls(testCall.calls())+
             " may unblocks "+unblockedId+
             " which is not in the active set "+active);
        }
      }
      
      for (Integer unblockedId : testCall.mustUnblock()) {
        active.remove(unblockedId);
      }
      checkSoundNess(name,continuation,active,blockedUsers,counter);
      
    } else if (stmt instanceof Branches) {
      Branches b = (Branches) stmt;
      
      counter =
        checkAndUpdateActiveBlocked
        (name,
         b.calls(),
         active,
         blockedUsers,
         counter);
      
      // We have to check that unblock sets are disjoint
      Set<Set<Integer>> unblockSets = new HashSet<Set<Integer>>();
      for (Alternative alt : b.alternatives()) {
        Set<Integer> unblockSet = new HashSet<Integer>();
        for (int i : alt.unblocks) unblockSet.add(i);
        if (unblockSets.contains(unblockSet)) {
          throw new RuntimeException
            ("*** Test "+name+" is incorrect:\n"+
             "identical unblock sets are used in alternatives for "+
             "calls "+Call.printCalls(b.calls()));
        } else unblockSets.add(unblockSet);
      }
      
      for (Alternative alt : b.alternatives()) {
        Map<Integer,Call> newActive = new HashMap<Integer,Call>();
        Set<Object> newBlockedUsers = new HashSet<Object>();
        
        for (Map.Entry<Integer,Call> mEntry : active.entrySet()) {
          newActive.put(mEntry.getKey(),mEntry.getValue());
        }
        
        for (Object obj : blockedUsers) {
          newBlockedUsers.add(obj);
        }
        
        for (Integer unblockedId : alt.unblocks) {
          Call call = newActive.get(unblockedId);
          if (call == null) {
            throw new RuntimeException
              ("Internal testing error (alternatives): test "+
               name+
               " has an unblockedId "+unblockedId+
               " which is not found in "+newActive);
          }
          if (call.bc().user() != null)
            newBlockedUsers.remove(call.bc().user());
          newActive.remove(unblockedId);
        }
        
        counter =
          checkSoundNess
          (name,alt.continuation,newActive,newBlockedUsers,counter);
      }
    }
    return counter;
  }
  
  private int checkAndUpdateActiveBlocked(String name,
                                          Call[] calls,
                                          Map<Integer,Call> active,
                                          Set<Object> blockedUsers,
                                          int counter) {
    for (Call call : calls) {
      if (call.bc().user() != null &&
          blockedUsers.contains(call.bc().user())) {
        throw new RuntimeException
          ("*** Test "+name+" is incorrect:\n"+
           "user "+call.bc().user()+" in call "+call.printCall()+
           " is blocked");
      }
      blockedUsers.add(call.bc().user());
    }
    
    for (Call call : calls) {
      if (call.name() != counter) {
        System.out.println
          ("*** Test "+name+" is incorrect:\n"+
           "current counter is "+counter+
           " but call "+call+" has id "+call.name());
        try { Thread.sleep(100); }
        catch (InterruptedException exc) {};
        throw new RuntimeException
          ("*** Test "+name+" is incorrect:\n"+
           "current counter is "+counter+
           " but call "+call+" has id "+call.name());
      }
      active.put(counter,call);
      ++counter;
    }
    return counter;
  }
  
  /*
   * Returns a test statement composed of a sequence of calls.
   */
  public static TestStmt sequence(TestCall... testCalls) {
    return sequenceEndsWith(testCalls, new Nil());
  }
  
  /*
   * Returns a test statement composed of a sequence of calls,
   * and ending with test statement parameter.
   */
  public static TestStmt sequenceEndsWith(TestCall[] testCalls, TestStmt endStmt) {
    int index = testCalls.length-1;
    TestStmt stmt = endStmt;
    while (index >= 0) {
      stmt = new Prefix(testCalls[index--],stmt);
    }
    return stmt;
  }
  
  /**
   * Sequential composition of two test statements.
   */
  public static TestStmt compose(TestStmt stmt1, TestStmt stmt2) {
    if (stmt1 instanceof Prefix) {
      Prefix prefix = (Prefix) stmt1;
      TestCall testCall = prefix.testCall();
      TestStmt stmt = prefix.stmt();
      return new Prefix(testCall,compose(stmt,stmt2));
    } else if (stmt1 instanceof Nil) {
      return stmt2;
    } else {
      UnitTest.failTestFramework("cannot compose statements "+stmt1+" and "+stmt2);
      return stmt1;
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
    Assertions.assertTrue(false,"\n*** Test failure: "+msg);
  }
  
  /**
   * Indicate a failure in the testing framework (i.e., not an error
   * in the tested program but rather in the test system).
   */
  public static void failTestFramework(String msg) {
    throw new TestErrorException("\n*** Failure in testing framework: "+msg);
  }
  
  /**
   * Starts a controller with a given name and the call to create it.
   */
  public static Object startController(String name,BasicCall bc) {
    Call call = new Call(bc);
    call.execute();
    if (call.isBlocked())
      UnitTest.failTest("creating an instance of "+name+" blocks");
    if (call.raisedException())
      UnitTest.failTest
        ("when creating an instance of "+name+
         " the exception "+call.getException()+" was raised");
    return call.returnValue();
  }
}

