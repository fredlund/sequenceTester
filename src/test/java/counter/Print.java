package counter;

import es.upm.babel.sequenceTester.*;


public class Print extends ReturningCall<Integer> {
  private final String msg;

  Print(String msg) {
    this.msg = msg;
  }

  public Integer execute() {
    System.out.println(msg);
    return 2;
  }

  public String toString() {
    return "print(\""+msg+"\")";
  }
}
