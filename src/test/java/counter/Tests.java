package counter;

import es.upm.babel.sequenceTester.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.TestInfo;
import java.time.Duration;
import static es.upm.babel.sequenceTester.Assertions.*;

// Runtime checking:

// getUnblockedCalls() from Call -- check that it was the latest executed command

// Confirm that checking exception failures in after works.

// Possibly check that all created Calls have been executed when the
// test ends; we have to remember all Calls in this way.

// Check that we do not try to run a command from a user that is blocked;
// if so fail. If no user is specified let every command use a new one.

class Tests {
  UnitTest test;
  public Counter counter;

  @Test
  public void test_01() {
    Counter counter = new CreateCounter().getReturnValue();
    new Set(counter,3).returns();
    new Await(counter,4).blocks();
    assertEquals(3,new Dec(counter));
  }

  @Test
  public void test_02() {
    Counter counter = new CreateCounter().getReturnValue();
    new Set(counter,3).returns();
    Call<Integer> whenEven = new WhenEven(counter).n("whenEven").blocks();
    assertEquals(2,new Dec(counter).unblocks(whenEven));
    assertEquals(2,whenEven);
  }

  @Test
  public void test_03() {
    Counter counter = new CreateCounter().getReturnValue();
    new Set(counter,3).returns();
    assertEquals(2,new Dec(counter));
    assertThrows(RuntimeException.class,new AssertIsEqual(counter,3));
  }

  @Test
  public void test_04() {
    Counter counter = new CreateCounter().getReturnValue();
    Integer rndInt = new Rand().getReturnValue();
    assertEquals((rndInt % 2) == 0, new IsEven(rndInt));
  }

  @Test
  public void test_05() {
    Counter counter = new CreateCounter().getReturnValue();
    new Set(counter,3).returns();
    new Await(counter,4).blocks();
    junitAssertion(() -> { org.junit.jupiter.api.Assertions.assertEquals(3,new Dec(counter).getReturnValue()); });
  }

  @Test
  public void test_par_1() {
    Counter counter = new CreateCounter().getReturnValue();
    new Set(counter,3).returns();
    Call<Integer> inc = new Inc(counter);
    Call<Integer> dec = new Dec(counter);
    Call.execute(inc,dec); assertUnblocks(inc,dec);
    assertEquals(4,new Inc(counter));
  }

  @Test
  public void test_par_2() {
    Counter counter = new CreateCounter().getReturnValue();
    new Set(counter,3).returns();
    Call<Integer> inc = new Inc(counter);
    Call<Integer> dec = new Dec(counter);
    Call.execute(inc,dec); 
    checkAlternatives();
    if (checkAlternative(() -> { inc.unblocks(); }))
      ;
    else if (checkAlternative(() -> { dec.unblocks(); }))
      ;
    else
      endAlternatives();
  }

  @Test
  public void test_repeat() {
    for (int i=0; i<2; i++) {
      Counter counter = new CreateCounter().getReturnValue();
      new Set(counter,3).returns();
      new Await(counter,4).blocks();
      assertEquals(2,new Dec(counter).unblocks());
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

