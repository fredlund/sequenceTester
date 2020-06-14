package es.upm.babel.sequenceTester;

/**
 * Represents an alternative in a branching statement of an unit test.
 */

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;

public class Alternative {
  private TestStmt continuation;
  private Unblocks unblocks;

  public Alternative(Unblocks unblocks, TestStmt continuation) {
    this.unblocks = unblocks;
    this.continuation = continuation;
  }

  public Unblocks unblocks() {
    return unblocks;
  }

  public TestStmt continuation() {
    return continuation;
  }

  public static Alternative alternative(String[] parms, TestStmt continuation) {
    return new Alternative(new Unblocks(Unblocks.unblocksMap(parms),null), continuation);
  }

  public static Alternative alternative(TestStmt continuation, String... parms) {
    return alternative(parms, continuation);
  }

  public static Alternative alternative(TestStmt continuation, List<Pair<String,Oracle>> mustUnblocks) {
    return new Alternative(new Unblocks(Unblocks.unblocksMap(mustUnblocks),null),continuation);
  }

  public String toString() {
    return "<"+unblocks+","+continuation+">";
  }
}
