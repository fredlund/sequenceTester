package counter;

import es.upm.babel.sequenceTester.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.TestInfo;
import java.time.Duration;
import static es.upm.babel.sequenceTester.Assertions.*;

// Permit getReturnValue and getException and other methods in Call to
// start executing a call if it has not started yet.
// Let asserts start executing calls to, if they have a call as an argument.

class Tests {
  UnitTest test;
  public Counter counter;

  @Test
  public void test_01() {
    Counter counter = new CreateCounter().unblocks().getReturnValue();
    new Set(counter,3).unblocks();
    new Await(counter,4).blocks();
    assertEquals(3,new Dec(counter).unblocks());
  }

  @Test
  public void test_02() {
    Counter counter = new CreateCounter().unblocks().getReturnValue();
    new Set(counter,3).unblocks();
    Call<Integer> whenEven = new WhenEven(counter).n("whenEven").blocks();
    assertEquals(2,new Dec(counter).unblocks(whenEven));
    assertEquals(2,whenEven);
  }

  @Test
  public void test_03() {
    Counter counter = new CreateCounter().unblocks().getReturnValue();
    new Set(counter,3).unblocks();
    assertEquals(2,new Dec(counter).unblocks());
    assertThrows(RuntimeException.class,new AssertIsEqual(counter,3).unblocks());
  }

  @Test
  public void test_04() {
    Counter counter = new CreateCounter().unblocks().getReturnValue();
    Integer rndInt = new Rand().unblocks().getReturnValue();
    assertEquals((rndInt % 2) == 0, new IsEven(rndInt).unblocks());
  }

  @Test
  public void test_par_1() {
    Counter counter = new CreateCounter().unblocks().getReturnValue();
    new Set(counter,3).unblocks();
    Call<Integer> inc = new Inc(counter);
    Call<Integer> dec = new Dec(counter);
    Call.execute(inc,dec); assertUnblocks(inc,dec);
    assertEquals(4,new Inc(counter).unblocks());
  }

  @Test
  public void test_repeat() {
    for (int i=0; i<2; i++) {
      Counter counter = new CreateCounter().unblocks().getReturnValue();
      new Set(counter,3).unblocks();
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

