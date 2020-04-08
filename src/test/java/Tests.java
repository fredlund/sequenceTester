package counter;

import es.upm.babel.sequenceTester.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

class Tests {
  @Test
  public void test_01() {
    new UnitTest
      ("test_01",
       configurationDescription(),
       new Counter(),
       TestCall.unblocks(new Set(3)),
       TestCall.blocks(Call.returns("whenEven",new WhenEven(),2)), 
       TestCall.unblocks(Call.returns(new Dec(),2),"whenEven")).run();
  }

  @BeforeEach
  public void setup() throws Exception {
    // UnitTest.installChecker(new RoboFabTestChecker());
    Call.reset();
  }

  private String configurationDescription() {
    return "";
  }
}

