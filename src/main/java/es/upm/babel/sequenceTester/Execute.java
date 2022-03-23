package es.upm.babel.sequenceTester;

import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Random;


/**
 * Represents the parallel execution of a set of calls, i.e., a set of instances of the Call class.
 * Provides information regarding the outcome of the execution, e.g., which calls were unblocked
 * and which calls remain blocked.
 */
public class Execute {
  private static final Random rand = new Random();

  private final UnitTest t = UnitTest.getCurrentTest();
  private Set<Call<?>> unblockedCalls;
  private Set<Call<?>> blockedCalls;
  private final List<Call<?>> calls;

  private Execute(List<Call<?>> calls) {
    this.calls = calls;
  }
  
  /**
   * Execute the calls in paralell.
   */
  public static Execute exec(Call<?>... calls) {
    Execute e = new Execute(Arrays.asList(calls));
    e.exec();
    return e;
  }

  void exec() {
    if (calls.size() == 0) UnitTest.failTestSyntax("trying to execute 0 calls", UnitTest.ErrorLocation.AFTER);
    // First check if any previous completed calls raised an exception which has not been handled
    if (t.getAllUnblockedCalls() != null) Call.checkExceptions(t.getAllUnblockedCalls(), false);

    // Next check if there are if a user in the new calls is blocked
    Set<Object> blockedUsers = new HashSet<>();
    for (Call<?> call : t.getBlockedCalls()) {
      Object user = call.getUser();
      if (user != null) blockedUsers.add(user);
    }
    for (Call<?> call : calls) {
      Object user = call.getUser();
      if (user != null && blockedUsers.contains(user)) {
        UnitTest.failTest("user "+user+" is blocked in call "+call, true, UnitTest.ErrorLocation.AFTER);
      }
    }

    int maxWaitTime = 0;

    for (Call<?> call : calls) {
      maxWaitTime = Math.max(maxWaitTime, call.getWaitTime());
      call.setExecute(this);
    }

    t.prepareToRun(this);
    runCalls();

    // Busywait a while until either we wait the maxWaitTime, or all active
    // calls have been unblocked
    long remainingTime = maxWaitTime;
    do {
      long waitTime = Math.min(remainingTime, 50);
      try { Thread.sleep(waitTime); }
      catch (InterruptedException exc) {
        // best effort
      }
      // Compute unblocked (and change blockedCalls)
      t.calculateUnblocked();
      remainingTime -= waitTime;
    } while (!t.getBlockedCalls().isEmpty() && remainingTime > 0);

    t.afterRun(this);

    unblockedCalls = t.getLastUnblockedCalls();
    blockedCalls = new HashSet<>(t.getBlockedCalls());
  }

  void runCalls() {
    boolean randomize = Config.getTestRandomize();
    List<Call<?>> callsInOrder = calls;

    // Check if the starting order of calls should be randomized
    if (randomize) {
      callsInOrder = new ArrayList<>();
      ArrayList<Call<?>> copiedCalls = new ArrayList<>(calls);
      int remaining = copiedCalls.size();
      while (remaining > 0) {
        int nextToTake = rand.nextInt(remaining);
        callsInOrder.add(copiedCalls.get(nextToTake));
        copiedCalls.remove(nextToTake);
        --remaining;
      }
    }

    for (Call<?> call : callsInOrder) {
      call.makeCall();
    }
  }

  /**
   * Returns the set of calls unblocked by the execution of the calls
   * provided as parameter to the exec method. Such an unblocked call
   * may have been provided as a parameter to this Execute object, 
   * or in an earlier Execte object.
   */
  public Set<Call<?>> getUnblockedCalls() {
    return unblockedCalls;
  }

  /**
   * Returns the set of calls which were started by calling
   * the exec method, but did not unblock.
   */
  public Set<Call<?>> getBlockedCalls() {
    return blockedCalls;
  }

  /**
   * Returns the set of calls provided as arguments to the exec method.
   */
  public List<Call<?>> getCalls() {
    return calls;
  }
  
  public String toString() {
    return Call.printCalls(calls);
  }
}
