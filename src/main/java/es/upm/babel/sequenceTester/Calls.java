package es.upm.babel.sequenceTester;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;


public class Calls {
  public List<Call<?>> calls;
  public Unblocks unblocks;

  private Calls(List<Call<?>> calls, Unblocks unblocks) {
    this.calls = calls;
    this.unblocks = unblocks();
  }

  public List<Call<?>> calls() {
    return calls;
  }
  
  public Unblocks unblocks() {
    return unblocks;
  }

  public static Calls unblocks(List<Call<?>> calls, String... unblocks) {
    Map<String,Oracle<?>> unblocksMap = Unblocks.unblocksMap(unblocks);
    for (Call call : calls)
      unblocksMap.put(call.getSymbolicName(),null);
    return new Calls(calls, new Unblocks(unblocksMap,null));
  }
  
  public static Calls unblocks(Call<?> call, String... unblocks) {
    List<Call<?>> list = new ArrayList<>();
    list.add(call);
    return unblocks(list, unblocks);
  }
  
  public static Calls unblocks(List<Call<?>> calls, List<Pair<String,Oracle<?>>> mustUnblocks) {
    Map<String,Oracle<?>> unblocksMap = Unblocks.unblocksMap(mustUnblocks);
    for (Call<?> call : calls)
      unblocksMap.put(call.getSymbolicName(),null);
    return new Calls(calls, new Unblocks(unblocksMap,null));
  }
  
  public static Calls unblocks(Call<?> call, List<Pair<String,Oracle<?>>> mustUnblocks) {
    List<Call<?>> list = new ArrayList<>();
    list.add(call);
    return unblocks(list, mustUnblocks);
  }
  
  public static Calls blocks(List<Call<?>> calls, String... unblocks) {
    return new Calls(calls, Unblocks.must(unblocks));
  }

  public static Calls blocks(Call<?> call, String... unblocks) {
    List<Call<?>> list = new ArrayList<>();
    list.add(call);
    return blocks(list, unblocks);
  }
  
  public static Calls unblocks(Call<?> call) {
    return unblocks(call, new String[] {});
  }
  
  public static Calls blocks(Call<?> call) {
    return blocks(call, new String[] {});
  }

}

  
