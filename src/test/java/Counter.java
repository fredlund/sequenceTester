/**
 * A simple counter which implements methods for
 * - setting the value of the counter (set)
 * - decrementing the counter (dec)
 * - returning the value of the counter when it is positive (whenEven)
 * AND blocking until the value becomes positive.
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
      synchronized (counter) {
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
}

