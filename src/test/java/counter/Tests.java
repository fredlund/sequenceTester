package counter;

import es.upm.babel.sequenceTester.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.TestInfo;
import java.time.Duration;
import org.junit.jupiter.api.extension.ExtendWith;


// Please tell the Java people to permit package aliases to get away from this madness
import org.junit.jupiter.api.Assertions;


// Runtime SeqAssertions.checking:

// getUnblockedCalls() from Call -- SeqAssertions.check that it was the latest executed command

// Confirm that SeqAssertions.checking exception failures in after works.

// Possibly SeqAssertions.check that all created Calls have been executed when the
// test ends; we have to remember all Calls in this way.

// SeqAssertions.Check that we do not try to run a command from a user that is blocked;
// if so fail. If no user is specified let every command use a new one.

// For each call, keep a list of unblocked calls.
// In UnitTest keep a history if the execution of the test.

@ExtendWith(HandleExceptions.class)
class Tests {
  UnitTest test;
  public Counter counter;

  @Test
  public void test_01() {
    Counter counter = new CreateCounter().getReturnValue();
    new Set(counter,3).returns();
    new Await(counter,4).blocks();
    SeqAssertions.assertEquals(3,new Dec(counter));
  }

  @Test
  public void test_02() {
    Counter counter = new CreateCounter().getReturnValue();
    new Set(counter,3).returns();
    Call<Integer> whenEven = new WhenEven(counter).blocks();
    SeqAssertions.assertEquals(2,new Dec(counter).unblocks(whenEven));
    SeqAssertions.assertEquals(2,whenEven);
  }

  @Test
  public void test_03() {
    Counter counter = new CreateCounter().getReturnValue();
    new Set(counter,3).returns();
    SeqAssertions.assertEquals(2,new Dec(counter));
    SeqAssertions.assertThrows(RuntimeException.class,new AssertIsEqual(counter,3));
  }

  @Test
  public void test_04() {
    Counter counter = new CreateCounter().getReturnValue();
    Integer rndInt = new Rand().getReturnValue();
    SeqAssertions.assertEquals((rndInt % 2) == 0, new IsEven(rndInt));
  }

  @Test
  public void test_05() {
    Counter counter = new CreateCounter().getReturnValue();
    new Set(counter,3).returns();
    new Await(counter,4).blocks();
    Assertions.assertEquals(3,new Dec(counter).getReturnValue());
  }

  @Test
  public void test_06() {
    Counter counter = new CreateCounter().getReturnValue();
    new Set(counter,3).returns();
    new Await(counter,4).blocks();
    Assertions.assertEquals(2,new Dec(counter).getReturnValue());
    new Fail().unblocks();
  }

  @Test
  public void test_par_1() {
    Counter counter = new CreateCounter().getReturnValue();
    new Set(counter,3).returns();
    Call<Integer> inc = new Inc(counter);
    Call<Integer> dec = new Dec(counter);
    Call.execute(inc,dec); SeqAssertions.assertUnblocks(inc,dec);
    SeqAssertions.assertEquals(4,new Inc(counter));
  }

  @Test
  public void test_par_2() {
    Counter counter = new CreateCounter().getReturnValue();
    new Set(counter,3).returns();
    Call<Integer> inc = new Inc(counter);
    Call<Integer> dec = new Dec(counter);
    Call.execute(inc,dec); 
    SeqAssertions.checkAlternatives();
    if (SeqAssertions.checkAlternative(() -> { inc.unblocks(); }))
      ;
    else if (SeqAssertions.checkAlternative(() -> { dec.unblocks(); }))
      ;
    else
      SeqAssertions.endAlternatives();
  }

  @Test
  public void test_repeat() {
    for (int i=0; i<2; i++) {
      Counter counter = new CreateCounter().getReturnValue();
      new Set(counter,3).returns();
      new Await(counter,4).blocks();
      SeqAssertions.assertEquals(2,new Dec(counter).unblocks());
    }
  }

  @BeforeEach
  public void start(TestInfo testInfo) {
    test = new UnitTest(testInfo.getDisplayName());
  }

  @AfterEach
  public void finish(TestInfo testInfo) {
    test.finish();
  }

}

