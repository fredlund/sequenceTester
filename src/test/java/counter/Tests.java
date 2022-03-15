package counter;

import es.upm.babel.sequenceTester.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.time.Duration;


class Tests {

  @Test
  public void test_01() {
    UnitTest.test
      ("test_01",
       Util.seq
       (Calls.unblocks(new CreateCounter()),
        Calls.unblocks(new Set(3))
        ,Calls.blocks(new Await(4).n("await"))
        ,Calls.unblocks(new Dec().o(Check.returns(2))))
       ).run();
  }

  @Test
  public void test_02() {
    UnitTest.test
      ("test_02",
       Util.seq
       (Calls.unblocks(new CreateCounter()),
        Calls.unblocks(new Set(3))
        ,Calls.blocks(new WhenEven().n("whenEven").o(Check.returns(2)))
        ,Calls.unblocks(new Dec().o(Check.returns(2)),"whenEven"))
       ).run();
  }

  @Test
  public void test_03() {
    UnitTest.test
      ("test_03",
       Util.seq
       (Calls.unblocks(new CreateCounter()),
        Calls.unblocks(new Set(3))
        ,Calls.unblocks(new Dec().o(Check.returns(2)))
        ,Calls.unblocks(new AssertIsEqual(3).o(Check.raisesException(RuntimeException.class))))
       ).run();
  }

  @Test
  public void test_04() {
    UnitTest.test
      ("test_04",
       Util.seqEndsWith
       (new Lambda(() ->
                   {
                     Integer rndInt = (Integer) Call.returnValue("rand");
                     return Util.seq(Calls.unblocks(new IsEven(rndInt).oracle(Check.returns((rndInt % 2) == 0))));
                   }),
        Calls.unblocks(new CreateCounter()),
        Calls.unblocks(new Rand().n("rand")))
       ).run();
  }

  @Test
  public void test_05() {
    Return<Integer> randR = new Return<>();
    
    UnitTest.test
      ("test_05",
       Util.seqEndsWith
       (new Lambda(() ->
                   {
                     int rndInt = randR.getReturnValue();
                     return Util.seq(Calls.unblocks(new IsEven(rndInt).oracle(Check.returns((rndInt % 2) == 0))));
                   }),
        Calls.unblocks(new CreateCounter()),
        Calls.unblocks(new Rand().n("rand").r(randR)))
       ).run();
  }

  @Test
  public void test_repeat() {
    UnitTest.repeatTest
      ("test_repeat",
       5,
       new Lambda
       (
        () -> {
          return
            Util.seq
            (Calls.unblocks(new CreateCounter())
             ,Calls.unblocks(new Set(3))
             ,Calls.blocks(new Await(4).n("await"))
             ,Calls.unblocks(new Dec().o(Check.returns(2))));
        })
       ).run();
  }

  @BeforeEach
  public void setup(TestInfo testInfo) throws Exception {
    UnitTest.setupTest(testInfo.getDisplayName());
  }
}

