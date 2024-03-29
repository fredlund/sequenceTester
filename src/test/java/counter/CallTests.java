package counter;

import java.util.Arrays;
import es.upm.babel.sequenceTester.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
// Please tell the Java people to permit package aliases
import static es.upm.babel.sequenceTester.SeqAssertions.*;

@ExtendWith(HandleExceptions.class)
class CallTests {
  UnitTest test;
  public Counter counter;

  @Test
  public void test_01() {
    assertFail(() -> {
        Counter counter = new CreateCounter().assertGetReturnValue();
        new Set(counter,3).assertReturns();
        new Await(counter,4).assertBlocks();
        assertEquals(3,new Dec(counter));
      }, true);
  }

  @Test
  public void test_02() {
    Counter counter = new CreateCounter().assertGetReturnValue();
    new Set(counter,3).assertReturns();
    Call<Integer> whenEven = new WhenEven(counter).assertBlocks();
    assertEquals(2,new Dec(counter).assertUnblocks(whenEven));
    assertEquals(2,whenEven);
  }

  @Test
  public void test_03() {
    Counter counter = new CreateCounter().assertGetReturnValue();
    new Set(counter,3).assertReturns();
    assertEquals(2,new Dec(counter).assertUnblocks());
    assertThrown(RuntimeException.class,new AssertIsEqual(counter,3).assertUnblocks());
  }

  @Test
  public void test_04() {
    Integer rndInt = new Rand().assertGetReturnValue();
    assertEquals((rndInt % 2) == 0, new IsEven(rndInt).assertUnblocks());
  }

  @Test
  public void test_05() {
    assertFail(() -> {
        Counter counter = new CreateCounter().assertGetReturnValue();
        new Set(counter,3).assertReturns();
        new Await(counter,4).assertBlocks();
        Assertions.assertEquals(3,new Dec(counter).assertGetReturnValue());
      }, true);
  }

  @Test
  public void test_06() {
    assertFail(() ->  {
      Counter counter = new CreateCounter().assertGetReturnValue();
      new Set(counter,3).assertReturns();
      new Await(counter,4).assertBlocks();
      Assertions.assertEquals(2,new Dec(counter).assertGetReturnValue());
      new Fail().assertReturns();
    }, true);
  }

  @Test
  public void test_par_1() {
    Counter counter = new CreateCounter().assertGetReturnValue();
    new Set(counter,3).assertReturns();
    Call<Integer> inc = new Inc(counter);
    Call<Integer> dec = new Dec(counter);
    Execute.exec(inc,dec); assertUnblocks(Arrays.asList(inc,dec));
    assertEquals(4,new Inc(counter).assertUnblocks());
  }

  @Test
  public void test_par_2() {
    assertFail(() -> {
        Counter counter = new CreateCounter().assertGetReturnValue();
        new Set(counter,3).assertReturns();
        Call<Integer> inc1 = new Inc(counter);
        Call<Integer> inc2 = new Inc(counter);
        Execute.exec(inc1,inc2);
        checkAlternatives(inc1::assertUnblocks,inc2::assertUnblocks);
      }, true);
  }

  @Test
  public void test_repeat() {
    for (int i=0; i<2; i++) {
      Counter counter = new CreateCounter().assertGetReturnValue();
      new Set(counter,3).assertReturns();
      assertEquals(2,new Dec(counter).assertUnblocks());
    }
  }

  @Test
  public void test1a() {
    CreateCounter cc = new CreateCounter();
    Execute.exec(cc); // Execute the call and wait
    Counter counter = cc.assertGetReturnValue(); // Inspect the result
    
    Set s = new Set(counter,3);
    Execute.exec(s);   // Execute the call and wait
    s.assertReturns(); // Assert that the call returns

    Await a = new Await(counter,2);
    Execute.exec(a); // Execute the call and wait
    a.assertBlocks(); // Assert that the call blocks
        
    Dec d = new Dec(counter);
    Execute.exec(d); // Execute the call and wait
    // Assert that the call to dec() unblocks also the earlier call to await()
    // and moreover that the call to dec() returns 2
    SeqAssertions.assertEquals(2,d.assertUnblocks(a)); 
  }

  @Test
  public void test1b() {
    Counter counter = new CreateCounter().assertGetReturnValue();
    new Set(counter,3).assertReturns();
    Await await = new Await(counter,2); await.assertBlocks();
    SeqAssertions.assertEquals(2,new Dec(counter).assertUnblocks(await));
  }

  @BeforeEach
  public void start(TestInfo testInfo) {
    test = new UnitTest(testInfo.getDisplayName());
  }

  @AfterEach
  public void finish() { test.finish(); }

  @BeforeAll
  public static void before() {
    UnitTest.setLocale("en");
  }

  @AfterAll
  public static void after() {
    UnitTest.reportTestResults();
  }
}

