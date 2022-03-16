package counter;

import es.upm.babel.sequenceTester.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.TestInfo;
import java.time.Duration;
import static es.upm.babel.sequenceTester.Assertions.*;

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

  // @Test
  // public void test_04() {
  //   UnitTest.test
  //     ("test_04",
  //      Util.sequenceEndsWith
  //      (new Lambda(() ->
  //                  {
  //                    Integer rndInt = (Integer) Call.returnValue("rand");
  //                    return Util.seq(TestCall.unblocks(new IsEven(rndInt).oracle(Check.returns((rndInt % 2) == 0))));
  //                  }),
  //       TestCall.unblocks(new CreateCounter()),
  //       TestCall.unblocks(new Rand().n("rand")))
  //      ).run();
  // }

  // @Test
  // public void test_05() {
  //   Return<Integer> randR = new Return<>();
    
  //   UnitTest.test
  //     ("test_05",
  //      Util.sequenceEndsWith
  //      (new Lambda(() ->
  //                  {
  //                    int rndInt = randR.getReturnValue();
  //                    return Util.seq(TestCall.unblocks(new IsEven(rndInt).oracle(Check.returns((rndInt % 2) == 0))));
  //                  }),
  //       TestCall.unblocks(new CreateCounter()),
  //       TestCall.unblocks(new Rand().n("rand").r(randR)))
  //      ).run();
  // }

  // @Test
  // public void test_repeat() {
  //   UnitTest.repeatTest
  //     ("test_repeat",
  //      5,
  //      new Lambda
  //      (
  //       () -> {
  //         return
  //           Util.sequence
  //           (TestCall.unblocks(new CreateCounter())
  //            ,TestCall.unblocks(new Set(3))
  //            ,TestCall.blocks(new Await(4).n("await"))
  //            ,TestCall.unblocks(new Dec().o(Check.returns(2))));
  //       })
  //      ).run();
  // }

  @BeforeEach
  public void start(TestInfo testInfo) {
    test = new UnitTest(testInfo.getDisplayName());
  }

  @AfterEach
  public void finish(TestInfo testInfo) {
    test.finish();
  }

}

