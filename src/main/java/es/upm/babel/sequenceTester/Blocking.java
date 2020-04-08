package es.upm.babel.sequenceTester;

public interface Blocking {
    public boolean shouldBlock();

    public boolean hasExactUnblockSpec(); 
    public boolean hasUnblockCountSpec();

    public int[] unblocks();
    public int unblocksCount();
}
