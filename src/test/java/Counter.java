/**
 * A simple counter which implements methods for
 * - setting the value of the counter (set)
 * - decrementing the counter (dec)
 * - returning the value of the counter when it is positive (assert)
 * AND blocking until the value becomes positive.
 * - await returns when the value of the counter becomes equal
 * to the argument.
 */
package counter;

public class Counter {
  private Integer counter;

  public synchronized void set(int value) {
    counter = value;
  }

  public synchronized int dec() {
    return --counter;
  }

  public int whenEven() {
    int returnValue = -1;
    
    while (returnValue % 2 != 0) {
      System.out.println("in while");

      synchronized (counter) {
        System.out.println("counter="+counter);
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
      synchronized (counter) {
        equal = counter == value;
        if (!equal) {
          try {
            Thread.sleep(100);
          } catch (InterruptedException exc) {
            throw new RuntimeException();
          }
        }
      }
    } while (!equal);
  }
}

