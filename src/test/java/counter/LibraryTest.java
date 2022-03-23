package counter;

import es.upm.babel.sequenceTester.Call;
import es.upm.babel.sequenceTester.UnitTest;
import es.upm.babel.sequenceTester.Version;
import es.upm.babel.sequenceTester.Execute;
import org.junit.jupiter.api.*;

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
        Assertions.assertEquals(1,e.getBlockedCalls().size(),() -> { return e.getBlockedCalls().toString(); });
    }

    @BeforeEach
    public void start(TestInfo testInfo) {
        test = new UnitTest(testInfo.getDisplayName());
    }

    @AfterEach
    public void finish() {
        test.finish();
    }
}
