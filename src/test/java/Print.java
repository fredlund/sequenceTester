package counter;

import es.upm.babel.sequenceTester.*;


public class Print extends CounterCall<Integer> {
  private String msg;

  Print(String msg) {
    this.msg = msg;
  }

  public void toTry() {
    System.out.println(msg);
    setReturnValue(2); 
  }

  public String toString() {
    return "print(\""+msg+"\")";
  }
}
