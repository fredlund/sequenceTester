package counter;

import es.upm.babel.sequenceTester.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.time.Duration;


class Tests {


  @Test
  public void test_01() {
    new UnitTest
      ("test_01",
       "",
       new Counter(),
       TestCall.unblocks(new Set(3))
       ,TestCall.blocks(new Call(new Await(4)).n("await"))
       ,TestCall.unblocks(new Call(new Dec()).o(Check.returns(2)))
       ).run();
  }

  @Test
  public void test_02() {
    new UnitTest
      ("test_02",
       "",
       new Counter(),
       TestCall.unblocks(new Set(3))
       ,TestCall.blocks(new Call(new WhenEven()).n("whenEven").o(Check.returns(2)))
       ,TestCall.unblocks(new Call(new Dec()).o(Check.returns(2)),"whenEven")
       ).run();
  }

  @Test
  public void test_03() {
    new UnitTest
      ("test_03",
       "",
       new Counter(),
       TestCall.unblocks(new Set(3))
       ,TestCall.unblocks(new Call(new Dec()).o(Check.returns(2)))
       ,TestCall.unblocks(new Call(new AssertIsEqual(3)).o(Check.raisesException(RuntimeException.class)))
       ).run();
  }

  @Test
  public void test_04() {
    assertTimeoutPreemptively
      (Duration.ofSeconds(2),
       () ->
       new UnitTest
       ("test_04",
        "",
        new Counter(),
        TestCall.unblocks(new Set(3))
        ,TestCall.unblocks(new Call(new Dec()).n("dec"))
        ,TestCall.unblocks(new Call((result) -> new Print("The value returned from Dec() is "+result),"dec"))
        ,TestCall.unblocks(new Call(new AssertIsEqual(3)).o(Check.raisesException(RuntimeException.class)))
        ).run());
  }

  @BeforeEach
  public void setup(TestInfo testInfo) throws Exception {
    UnitTest.setupTest(testInfo.getDisplayName());
  }
}

