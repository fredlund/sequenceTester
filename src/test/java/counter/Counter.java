/**
 * A simple counter which implements methods for
 * - set(int value) sets the value of the counter
 * - dec() decrements the counter by 1
 * - whenEven() returning the value of the counter when it becomes positive
 * - await(int value) returns when the value of the counter becomes equal
 * to the argument.
 * - assertIsEqual(int value) returns if the counter has the value
 * value and otherwise raises an RuntimeException.
 * 
 */
package counter;

public class Counter {
  private Integer counter;

  public synchronized void set(int value) {
    counter = value;
  }

  public synchronized int inc() {
    return ++counter;
  }

  public synchronized int dec() {
    return --counter;
  }

  public synchronized void assertIsEqual(int value) {
    if (counter != value) throw new RuntimeException(counter + " != " + value);
  }

  public int whenEven() {
    int returnValue = -1;
    
    while (returnValue % 2 != 0) {
      synchronized (this) {
        if (counter % 2 == 0)
          returnValue = counter;
      }

      if (returnValue % 2 != 0) {
        try {
          Thread.sleep(100);
        } catch (InterruptedException exc) {
          throw new RuntimeException();
        }
      }
    }
    return returnValue;
  }

  public void await(int value) {
    boolean equal = false;
    
    do {

      synchronized (this) {
        equal = counter == value;
      }

      if (!equal) {
        try {
          Thread.sleep(100);
        } catch (InterruptedException exc) {
          throw new RuntimeException();
        }
      }
    } while (!equal);
  }
}

