package es.upm.babel.sequenceTester;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * Provides methods for specifying and checking which may or must become unblocked.
 */

public class Unblocks {
  private final Set<Call<?>> mustUnblock;
  private final Set<Call<?>> mayUnblock;

  /**
   * Creates an unblocks specification.
   * The mustUnblock parameter specifies which calls must unblock, and the mayUnblock parameter
   * specifies which calls may unblock.
   */
  public Unblocks(Set<Call<?>> mustUnblock, Set<Call<?>> mayUnblock) {
    this.mustUnblock =  mustUnblock == null ? new HashSet<>() : mustUnblock;
    this.mayUnblock = mayUnblock == null ? new HashSet<>() : mayUnblock;
  }

  /**
   * Specifies that a number of calls must be unblocked, and
   * that (some other) calls may be unblocked.
   */
  public Unblocks(List<Call<?>> mustCalls, List<Call<?>> mayCalls) {
    this(new HashSet<>(mustCalls), new HashSet<>(mayCalls));
  }

  //////////////////////////////////////////////////////////////////////

  void checkCalls(Execute e) {
    UnitTest t = UnitTest.getCurrentTest();
    List<Call<?>> calls = e.getCalls();
    Set<Call<?>> unblockedCalls = e.getUnblockedCalls();
    
    // System.out.println("checkCalls: unblocked="+unblockedCalls+" spec: "+this);

    // Check that each unblocked call is either
    // listed in the may or must unblocked enumeration.
    for (Call<?> unblockedCall : unblockedCalls) {
      if (!mustUnblock.contains(unblockedCall) &&
          !mayUnblock.contains(unblockedCall)) {
        printReasonForUnblockingIncorrectly(unblockedCall,calls);
      }
    }

    Set<Call<?>> wronglyUnblocked = new HashSet<>();
    // Check that each call that must have been unblocked,
    // is no longer blocked
    for (Call<?> shouldBeUnblockedCall : mustUnblock) {
      if (e.getBlockedCalls().contains(shouldBeUnblockedCall)) {
        wronglyUnblocked.add(shouldBeUnblockedCall);
      }
    }

    if (wronglyUnblocked.size() > 0) {
      UnitTest.failTest
        (Texts.getText("the_calls","S")+Call.printCalls(wronglyUnblocked)+
         Texts.getText("are_still","PS")+Texts.getText("blocked_plural","S")+
         Texts.getText("although_they_should_have_been","S")+Texts.getText("unblocked_plural")+"\n");
    }

    for (Call<?> call : calls) {
      call.checkedForUnblocks();
    }
  }
    
  private String returned(Object value) {
    if (value == null) return Texts.getText("terminated_normally");
    else return Texts.getText("returned_the_value","S")+value;
  }
  
  private String prefixConfigurationDescription(String configurationDescription) {
    if (configurationDescription == null || configurationDescription.equals("")) return "";
    else return Texts.getText("with_the_configuration","S") + configurationDescription+",\n";
  }
    
  private void printReasonForUnblockingIncorrectly(Call<?> call, List<Call<?>> calls) {
    if (call.raisedException()) {
      Throwable exc = call.intGetException();
      StringWriter errors = new StringWriter();
      exc.printStackTrace(new PrintWriter(errors));
      String StackTrace = errors.toString();
      
      UnitTest.failTest
        (Texts.getText("the_call","S") + call.printCall()+
         Texts.getText("should_block_singular","P")+ "\n" +
         Texts.getText("but","S") + Texts.getText("raised_the_exception","S") + exc +
         "\n\nStacktrace:\n"+StackTrace+"\n");
    } else {
      boolean justExecuted = false;
      for (Call<?> executingCall : calls) {
        if (executingCall == call) {
          justExecuted = true;
          break;
        }
      }
      
      String blockStr;
      if (justExecuted)
        blockStr = Texts.getText("should_block_singular","P");
      else
        blockStr = Texts.getText("should_still_be_blocked_singular","SP") +
          Texts.getText("after","S") + Texts.getText("the_calls","S") + Call.printCalls(calls);
      
      String returnString = "";
      if (call.returnsValue()) returnString = Texts.getText("but","S") + returned(call.intGetReturnValue());

      UnitTest.failTest
        (Texts.getText("the_call","S") + call.printCall()+blockStr+"\n"+returnString+"\n");
    }
  }

  public String toString() {
    return "<must = "+mustUnblock+", may="+mayUnblock+">";
  }
}


