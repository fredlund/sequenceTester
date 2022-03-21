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
  were calls may block. The library has been used to test
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
terminated, others may be blocked waiting for some events. In tests we can
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
        private int waitingFor;
        private final Counter counter;

        Await(Counter counter, int waitingFor) {
            this.counter = counter;
            setUser("await");
        }
        public void execute() { counter.await(waitingFor); }
        public String toString() { return "await("+waitingFor+")"; }
    }

Such command classes must provide a definition of the execute method, which is responsible
for invoking the tested API. Moreover they should provide a toString() which is used
to pretty-print commands.


### Unit Tests
 
A unit test is an instance of the 
`TestStmt` interface. A test statement is either:
 
  - `Nil` -- A test statement which always succeeds.

  - `Prefix`(TestCall call, TestStatement stmt) -- 
  A test statement
  which first executes the TestCall call, and if that succeeds, then
  continues with succeeding statement.

  - `Branches`(Call[] calls, Alternative... alternatives) --
  A branch statement can handle nondeterminism in the tested
  program. First the calls are executed in parallel then
  the alternatives are checked to see if any of them can explain the
  result of executing the parallel calls. The alternatives are checked
  sequentially, from left (earlier in the array) to the right.
  An alternative describes which calls unblocked, with optional oracles attached.
  For an alternative to match
  the execution result,
  the unblocked calls must be accepted by the alternative unblock specification.
  An alternative has a
  continuation statement which is used to continue the test.
  If no alternative matches, the execution of the test case fails.

### Calls
 
  An instance of the `Call` class is a
  **recipe for making a call**, and an **oracle which decides if the invocation of
    the call returned the correct result** (or raised the correct exception).
  A call may be given
  a symbolic name, which is useful to specify that it is later
  (during the execution of another call) unblocked.
  Note that a Call instance extends the Tryer class of the cclib library.
 

### Oracles
  Oracles checks the return values, or exceptions, of terminated calls.
  Oracles are instances of the `es.upm.babel.sequenceTester.Oracle`
  interface.
  
### TestCalls
 
  An instance of the `TestCall` class
  executes a set of calls in parallel, and **checks that
  the correct calls are _blocked_ and _unblocked_**.
  The TestCall has a constructor:

    TestCall(List<Call> calls, Unblocks unblocks)

The calls are executed in parallel, and unblocks specifies which calls _must_ or _may_ unblock.
   

### Unblocks
 
  An unblock instance specifies which calls _must_ or _may_ unblock,
  and has a constructor
  public Unblocks(Map<String,Oracle> mustUnblock, Map<String,Oracle> mayUnblock)
  The maps associated the symbolic name of a call with an optional oracle (the oracle may be null).
  Note that oracles can be associated directly with calls and/or with an
  unblock specification.
   

## An Example
 
  Suppose we have an API with three methods and a constructor:
  ` Counter()` which creates a new counter,
  ` set(int)`, ` dec()`, and ` whenZero()` such that ` set(int)` sets the value of a counter
  to the argument and always unblocks immediately,
  ` dec()` decrements the value of the
  counter by 1 and always unblocks immediately, whereas
  a call to ` whenZero()` unblocks when the value of the counter becomes 0.
 
 An example unit test for this API is:

    Prefix
    (TestCall.unblocks(new CreateCounter()),
     Prefix
     (TestCall.unblocks(new Set(3)),
      Prefix
      (TestCall.unblocks(new Dec()),
       Prefix
       (TestCall.blocks(new WhenZero().name("whenZero")),
        Prefix
        (TestCall.unblocks(new Dec()),
         Prefix
         (TestCall.unblocks(new Dec(),"whenZero"),
          Nil))))))
 
  Given constructors CreateCounter(), Set(int), Dec() and WhenZero() which creates
  instances of a Call (sub)class,
  the test expresses that if we first create a counter, and sets the value
  of the new counter to 3
  (a call which immediately returns, i.e., "unblocks"),
  and then decrement the counter
  (a call which immediately returns),
  and if we next call whenZero() that call blocks (does not return).
  Note that we give the call to whenZero() the symbolic name "whenZero",
  by calling the method name (or n), with the symbolic name as an argument.
  If next we decrement the counter again that call immediately
  returns. Finally if we again call dec() again
  then both that call returned immediately, ***and***
  also the earlier call to whenZero() returned ("unblocked").
 
 Using the library API we can write this test more compactly as a sequence:
 
    TestCall.unblocks(new CreateCounter()),
    TestCall.unblocks(new Set(3)),
    TestCall.unblocks(new Dec()),
    TestCall.blocks(new WhenZero().name("whenZero")),
    TestCall.unblocks(new Dec()),
    TestCall.unblocks(new Dec(), "whenZero")
 
  Suppose next we modify the dec() method to return the value
  of the counter after the decrement operation,
  and that we wish to check that value in the unit test,
  i.e., defining oracles. This can be done using the code:
   
    TestCall.unblocks(new CreateCounter()),
    TestCall.unblocks(new Set(3)),
    TestCall.unblocks(new Dec().oracle(Check.returns(2))),
    TestCall.blocks(new WhenZero().name("whenZero")),
    TestCall.unblocks(new Dec().oracle(Check.returns(1))),
    TestCall.unblocks(new Dec().oracle(Check.returns(0)), "whenZero")
 
The specification of the decrement operation, e.g.,
new Dec().oracle(Check.returns(2)),
associates an oracle (using the method oracle or o)
to the method call. The oracle is Check.returns(2),
which specifies that the call returns normally with the value 2.
The `Check` class
contains a number of convient oracles; it is also possible
to define a custom oracle.
 
#### Subclassing Call
 Next we consider what is needed to write a subclass for the
  Call class; we examine the Dec call as an example:

    public class Dec extends CounterCall<Integer> {
        Dec() { setUser("dec"); }

    // Executes the call
    public void toTry() { setReturnValue(counter().dec());  }

    // Prints the call
    public String toString() { return "dec()"; }
    }

  All such subclasses should define toTry which executes the call
  and toString which is used to pretty print counterexamples.
  If a call returns a value, such as Dec, then the ` setReturnValue` method
  should be called with the value as the argument.
 
 
  The ` setUser("dec")` method call in the constructor specifies which "user" (or actor)
  executes the constructed call. The library enforces the restriction that whenever a call from
  a user blocks, then no more calls from that user will be issued until the original call
  was unblocked. If a test case specifies that a call should be made from a blocked user,
  then that test case is not well-formed, and the testing run is aborted with an error message;
  this does not constitute a test failure in the traditional sense.
   
 
In fact the Dec class does not subclass Call directly, but CounterCall, which
    provides access to the counter via the method call counter(). The counter() method is implemented
using the setTestState and getTestState methods which
    provide a context for the execution of a test: 

    public abstract class CounterCall<E> extends Call<E> {
        Counter controller;

        public CounterCall() { }

        public Counter counter() {
            Object state = getTestState(); // access remembered counter

            if (state instanceof Counter)
                return (Counter) state;
            else
                throw new RuntimeException();
        }
    }

Moreover, the CreateCounter class creates a counter: 

    public class CreateCounter extends CounterCall<Counter> {
         CreateCounter() {
            setUser("createCounter");
         }

         public void toTry() {
            Counter counter = new Counter();
            setTestState(counter); // remember counter
            setReturnValue(counter); // return a value
        }

        public String toString() {
            return "createCounter()";
        }
    }

### Frequently asked questions

  - How do we check if a call is blocking? A call is deemed blocking if it has not returned after
  some specified small time interval.
  The default can be changed by calling the `Call#waitTime(int)` method.
 
  - Want to write an oracle that is parametric on the return value of its call?
  If so use the `Check#lambda(java.util.function.Predicate)`
    method.
    An example: suppose that we have a call ` IsEven(int n)` that returns true
    if n is even. Then we can write make a call and check its return value in the following way:
  ` new IsEven(n).oracle(Check.lambda(x -> ((n % 2) == 0) == x)`. The function inside
  the call to lambda receives the return value of IsEven as a parameter, and returns true of false depending
  on whether the return value agrees with whether n is even or not.
 
  - How to refer to previous return values in subsequent calls?
    We can retrieve the return value of a call given its symbolic name
    using the method `Call#returnValue(String)`.
    This method unfortunately returns an ` Object`,
    thus often requiring a type cast in oracle code, which
    can cause an exception to be thrown at test time.
    A type-safe alternative uses a `Return` class instance for storing the
    value returned by the call returned by ` Rand()`, e.g.,
    
    Return<Integer> randR = new Return<>();
    ...
    TestCall.unblocks(new Rand().name("rand").returnsTo(randR)),
  
In later code we can then extract the returned value using a call
to `Return#getReturnValue`.
   
   
Apart from referring to a previous return value,
    we need a mechanism to postpone the creation of a
    new ` Call` subclass instance, until the arguments have become available.
    This can be achieved by creating an instance of the `Lambda` class.
    Consider for example an operation ` Rand()` that returns a random
    integer, and an operation ` IsEven(n)` that checks whether its
    parameter is even. We can check such an API using the below test case which
    invokes ` IsEven(v)` on the value ` v` returned by the call to ` Rand()`: 

    TestCall.unblocks(new Rand().name("rand")),
    TestCall.unblocks(new Lambda(() ->
                  {
                    Integer rndInt = (Integer) Call.returnValue("rand");
                    return new IsEven(rndInt).oracle(Check.returns((rndInt % 2) == 0));
                  })}
 
Note that we postpone the creation of the IsEven call using
  the `Lambda` subclass. The alternative when
  using a ` Return` instance is shown below as well. 

    Return<Integer> randR = new Return<>();
    ...
    TestCall.unblocks(new Rand().name("rand").returnsTo(randR)),
    TestCall.unblocks(new Lambda(() ->
                  {
                    int rndInt = randR.getReturnValue();
                    return new IsEven(rndInt).oracle(Check.returns((rndInt % 2) == 0));
                  })
    }

