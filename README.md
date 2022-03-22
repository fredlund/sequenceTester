# sequenceTester
A Java library for testing sequences of possibly blocking Java commands

## Building

    make -- builds a library sequenceTester.jar
    make javadoc -- builds Javadoc documentation (incomplete)
    make test -- run a simple test example (tests in Junit 5)

## Introduction

  The sequenceTester library is a Java library
  used for writing and testing unit tests
  for a possibly non-sequential APIs, i.e.,
  were calls can execute in parallel, and
  where calls may block. The library has been used to test
  exercises in the undergraduate
  course "Concurrencia" at the Escuela Tecnica Superior
  de Ingenieros Informaticos at the Universidad Politecnica de Madrid, Spain.

  The main idea is to write test cases as sequences of API calls,
  and to provide excellent diagnostics of why a test case failed, i.e.,
  showing a complete trace of the calls of a failed test case, and
  indicating clearly the reason for failure. For example, that a call "a"
  is blocked although it should have been unblocked by a call "b",
  or that it returned an incorrect value.
 
## Library Functionality 

The sequenceTester library provides additional functionality to tests written
using the Junit 5 testing library. Tests are written normally, except that
instead of issuing normal method calls, and testing the ouctome of such calls
with test assertions, here a user defines a set of call (classes) representing
"commands" to create objects and call methods, 
instantiates them, and instructs the library to run a set of such call instances
in parallel. The observable outcome is that some such calls may have
terminated, others may be blocked waiting for events. In tests we can
directly observe the unblocked  calls (terminated calls, either due to having
returned normally or abnormally through raising an exception), and the still
executing (blocked calls). Return values can be inspected using
normal Junit 5 assertions, as can exceptions.
If a test case assertion fails, the library produces a detailed trace showing
the execution of the test case. 

As an example suppose that we want to check a simple Counter implemented below:

    public class Counter {
        private Integer counter;

       public synchronized void set(int value) { counter = value; }
       public synchronized int dec() { return --counter; }

       public void await(int value) {
           boolean equal = false;
             do {
                 synchronized (this) { equal = counter == value; }
                 if (!equal) {
                     try { Thread.sleep(100); } catch (InterruptedException exc) {
                         throw new RuntimeException();
                     }
                 }
             } while (!equal);
         }
    }
The Counter class defines three methods: set(int) which sets the value of the counter,
dec() which decreases the value of the counter and await(int) which busy-waits until
the value of the counter is equal to its argument.

To use the library we must define four new classes representing the constructor and the
tree methods. Methods that have the type void should subclass the VoidCall class,
whereas other methods and the constructor should subclass the ReturningCall class. Below we
show the corresponding class definitions:

    public class CreateCounter extends ReturningCall<Counter> {
        CreateCounter() { }
        public Counter execute() { return new Counter(); }
        public String toString() { return "createCounter()"; }
    }

    public class Set extends VoidCall {
        private final int value;
        private final Counter counter;

        Set(Counter counter, int value) {
            this.counter = counter;
            this.value = value;
        }
        public void execute() { counter.set(value); }
        public String toString() { return "set("+value+")"; }
    }

    public class Dec extends ReturningCall<Integer> {
        private final Counter counter;

        Dec(Counter counter) { this.counter = counter; }
        public Integer execute() { return counter.dec(); }
        public String toString() { return "dec()"; }
    }

    public class Await extends VoidCall {
        private final int waitingFor;
        private final Counter counter;

        Await(Counter counter, int waitingFor) {
            this.counter = counter;
            this.waitingFor = waitingFor;
            setUser("await");
        }
        public void execute() { counter.await(waitingFor); }
        public String toString() { return "await("+waitingFor+")"; }
    }

Such command classes must provide a definition of the execute method, which is responsible
for invoking the tested API. Moreover they should provide a toString() which is used
to pretty-print commands.

### Unit Tests
 
A unit test is a sequence of test "phases". In a phase a number of calls are
executed concurrently. Next the library waits a definable period of time, and
after the wait observes the outcome of the execution of calls. That is, which calls
are still running, which calls have terminated throwing an exception or returning
a value. In the unit test we can assert that such observations hold.
A concrete example:

    @Test
    public void test1a() {
        CreateCounter cc = new CreateCounter();
        Execute.exec(cc); // Execute the call and wait
        Counter counter = cc.getReturnValue(); // Inspect the result

        Set s = new Set(counter,3);
        Execute.exec(s);   // Execute the call and wait
        s.assertReturns(); // Assert that the call returns

        Await a = new Await(counter,1);
        Execute.exec(a); // Execute the call and wait
        a.assertBlocks(); // Assert that the call blocks
        
        Dec d = new Dec(counter);
        Execute.exec(d); // Execute the call and wait
        // Assert that the call to dec() unblocks also the earlier call to await()
        // and moreover that the call to dec() returns 2
        SeqAssertions.assertEquals(2,d.assertUnblocks(a)); 
    }

Using a number of convenience methods this can be shortened to:

    @Test
    public void test1b() {
        Counter counter = new CreateCounter().getReturnValue();
        new Set(counter,3).assertReturns();
        Await await = new Await(counter,1); await.assertBlocks();
        SeqAssertions.assertEquals(2,new Dec(counter).assertUnblocks(await));
    }

Both tests fail since we have specified that the decrement call should unblock the
earlier call to await, but the value of the counter after executing decrement will
be 2 instead of 1 (which await expects). The failure report is documented as:

    Tests > test1b() FAILED
    org.opentest4j.AssertionFailedError: the call 3: await(1) is still blocked although it should have been unblocked

    Call trace (error detected in the last line):

    1: createCounter() --> unblocked 1: createCounter() returned counter.Counter@1cf9eb89
    2: set(3) --> unblocked 2: set(3)
    3: await(1)
    4: dec() --> unblocked 4: dec() returned 2
        at org.junit.jupiter.api.AssertionUtils.fail(AssertionUtils.java:39)
        at org.junit.jupiter.api.Assertions.fail(Assertions.java:109)
        at es.upm.babel.sequenceTester.UnitTest.failTest(UnitTest.java:126)
        at es.upm.babel.sequenceTester.UnitTest.failTest(UnitTest.java:118)
        at es.upm.babel.sequenceTester.Unblocks.checkCalls(Unblocks.java:91)
        at es.upm.babel.sequenceTester.SeqAssertions.assertMustMayUnblocked(SeqAssertions.java:82)
        at es.upm.babel.sequenceTester.SeqAssertions.assertUnblocks(SeqAssertions.java:107)
        at es.upm.babel.sequenceTester.Call.assertUnblocks(Call.java:213)
        at counter.Tests.test1b(Tests.java:138)

### Branching Tests

Testing non-deterministic systems is easily achieved. It is recommend to use
the convient methods checkAlternatives, checkAlternative and endAlternatives to
permit expressive error messages. An example:

    @Test
    public void test_par_2() {
        Counter counter = new CreateCounter().getReturnValue();
        new Set(counter,3).assertReturns();
        Call<Integer> inc1 = new Inc(counter);
        Call<Integer> inc2 = new Inc(counter);
        Execute.exec(inc1,inc2); # Executes increments in parallel
        checkAlternatives();
        if (checkAlternative(() -> { inc1.assertUnblocks(); }))
          ;
        else if (checkAlternative(() -> { inc2.assertUnblocks(); }))
          ;
        else
          endAlternatives();
    }

In the test first a counter is created, initialized to 3. Then two increment calls are created
and executed in parallel. Next we illustrate how to handle non-determinism. We assert
that there are two branches; in the first only the first increment call unblocks,
and in the second branch only the second increment call unblocks.
This is obviously wrong, so Junit 5 displays the following error:

    Tests > test_par_2() FAILED
        org.opentest4j.AssertionFailedError: All possible alternative executions failed:
    Alternative 1:
      the call 4: inc() should block
    but returned the value 5

    Alternative 2:
      the call 3: inc() should block
    but returned the value 4

    Call trace (error detected in the last line):

    1: createCounter() --> unblocked 1: createCounter() returned counter.Counter@271d4193
    2: set(3) --> unblocked 2: set(3)
    ===  calls executed in parallel: 
      3: inc()
      4: inc() --> unblocked 3: inc() returned 4, 4: inc() returned 5
        at org.junit.jupiter.api.AssertionUtils.fail(AssertionUtils.java:39)
        at org.junit.jupiter.api.Assertions.fail(Assertions.java:109)
        at es.upm.babel.sequenceTester.UnitTest.failTest(UnitTest.java:126)
        at es.upm.babel.sequenceTester.UnitTest.failTest(UnitTest.java:118)
        at es.upm.babel.sequenceTester.SeqAssertions.endAlternatives(SeqAssertions.java:159)
        at counter.Tests.test_par_2(Tests.java:99)

### Test Case Definition

To use the library properly it is required to define both a @BeforeEach and an @AfterEach method.

    @BeforeEach
    public void start(TestInfo testInfo) {
        test = new UnitTest(testInfo.getDisplayName());
    }

    @AfterEach
    public void finish(TestInfo testInfo) { 
        test.finish(); 
    }
In the @BeforeEach method we provide the name of the test to the library, and also initialize
library internal data structures, through the creation of
an instance of the UnitTest class (and saving the instance in an attribute).
In the post-test method calling test.finish() permits the library to perform some
post-test correctness checks.

### Checking Test Cases

Apart from the assertions a number of other checks are done on test cases.

#### Users

A "user" can be attached to a call; this is an arbitrary object. If a call user has been specified
(i.e., it is distinct from null) when the call is executed then the library checks that 
no other call with the same user remains blocked, and if such a call exists,
the test case fails. That is, think of the user mechanism
as providing a client caller that requires calls to be served strictly in order, i.e.,
waits for the completion of one call before attempting another.

#### Exceptions

The library enforces that all thrown exceptions must be explictely permitted. That is, 
if a call throws an exception, and we do not assert that such an exception may be throws
(e.g., using the assertions assertThrown) then the corresponding test case will fail.

#### Created Calls Must be Executed

Similarly the library enforces that upon the successfull completion of a test case,
all calls created during the execution of the test case have been executed; otherwise
the test case fails.
This can be understood as a syntax check on test case, preventing us to forget to execute
a call.
