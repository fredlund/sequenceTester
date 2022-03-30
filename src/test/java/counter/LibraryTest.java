package counter;

import es.upm.babel.sequenceTester.*;
import org.junit.jupiter.api.*;

import java.util.Arrays;
import java.util.List;

import static es.upm.babel.sequenceTester.SeqAssertions.assertEquals;

// Testing internal functionality -- do not read this code... :-)
public class LibraryTest {
    public UnitTest test;

    @Test
    public void test1() {
        System.out.println("Library version: "+Version.major()+"."+Version.minor()+"."+Version.patchlevel());
        UnitTest.setLocale("es");
        UnitTest.setLocale("en","US");

        String description = "This is a test description";
        Assertions.assertEquals(description,test.setConfigurationDescription(description).getConfigurationDescription());

        Counter counter = new CreateCounter().getReturnValue();
        Call<?> c1 = new Set(counter,3).user("user1").w(200).assertIsUnblocked().assertReturns();
        new Await(counter,4).assertIsBlocked().assertBlocks();
        new Fail().assertRaisedException().assertIsUnblocked();
        Assertions.assertTrue(test.getAllCalls().contains(c1));
        assertEquals(2,new Dec(counter).assertReturns().assertReturnsValue());
        Execute e = Execute.exec(new Inc(counter));
        SeqAssertions.assertUnblocks(e, e.getCalls());
        Assertions.assertEquals(1,e.getBlockedCalls().size(),() -> e.getBlockedCalls().toString());
        Call<?> fail = new Fail();
        Execute.exec(fail);
        SeqAssertions.assertThrown(fail);
        SeqAssertions.assertMustMayUnblocked(fail, List.of(fail), List.of());
        SeqAssertions.assertMustMayUnblocked(List.of(), List.of(fail));
        SeqAssertions.assertMustMayUnblocked(List.of(fail), List.of());
        SeqAssertions.assertUnblocks(fail, List.of(fail));
    }

    @BeforeEach
    public void start(TestInfo testInfo) {
        test = new UnitTest(testInfo.getDisplayName());
    }

    @AfterEach
    public void finish() { test.finish(); }
}
