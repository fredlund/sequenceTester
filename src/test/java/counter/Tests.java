package counter;

import es.upm.babel.sequenceTester.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import java.time.Duration;


class Tests {
  public Counter counter;

  @Test
  public void test_01(TestInfo testInfo) {
    new UnitTest(testInfo.getDisplayName());
    Counter counter = new CreateCounter().exec().getReturnValue();
    new Set(counter,3).exec();
    new Await(counter,4).exec().n("await");
    Call<Integer> d = new Dec(counter).exec(); Assertions.assertEquals(3,d);
  }

  // @Test
  // public void test_02() {
  //   UnitTest.test
  //     ("test_02",
  //      Util.seq
  //      (TestCall.unblocks(new CreateCounter()),
  //       TestCall.unblocks(new Set(3))
  //       ,TestCall.blocks(new WhenEven().n("whenEven").o(Check.returns(2)))
  //       ,TestCall.unblocks(new Dec().o(Check.returns(2)),"whenEven"))
  //      ).run();
  // }

  // @Test
  // public void test_03() {
  //   UnitTest.test
  //     ("test_03",
  //      Util.seq
  //      (TestCall.unblocks(new CreateCounter()),
  //       TestCall.unblocks(new Set(3))
  //       ,TestCall.unblocks(new Dec().o(Check.returns(2)))
  //       ,TestCall.unblocks(new AssertIsEqual(3).o(Check.raisesException(RuntimeException.class))))
  //      ).run();
  // }

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

}

