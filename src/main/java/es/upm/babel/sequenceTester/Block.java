package es.upm.babel.sequenceTester;

public class Block implements Blocking {
    boolean shouldBlock = false;
    boolean hasExactUnblockSpec = false;
    boolean hasUnblockCountSpec = false;
    int[] unblocks = null;
    int unblockCount = 0;

    public Block() { }

    static public Block unblocks(boolean shouldNotBlock, int... unblocks) {
	Block b = new Block();
	b.hasExactUnblockSpec = true;
	b.hasUnblockCountSpec = false;
	b.shouldBlock = !shouldNotBlock;
	b.unblocks = unblocks;
	return b;
    }

    static public Block numUnblock(boolean shouldNotBlock, int numUnblocks) {
	Block b = new Block();
	b.hasExactUnblockSpec = false;
	b.hasUnblockCountSpec = true;
	b.shouldBlock = !shouldNotBlock;
	b.unblockCount = numUnblocks;
	return b;
    }

    static public Block blocks(boolean shouldNotBlock) {
	Block b = new Block();
	b.shouldBlock = !shouldNotBlock;
	b.hasExactUnblockSpec = false;
	b.hasUnblockCountSpec = false;
	return b;
    }
    
    public boolean shouldBlock() {
	return shouldBlock;
    }

    public boolean hasExactUnblockSpec() {
	return hasExactUnblockSpec;
    }

    public boolean hasUnblockCountSpec() {
	return hasUnblockCountSpec;
    }

    public int[] unblocks() {
	return unblocks;
    }

    public int unblocksCount() {
	return unblockCount;
    }
}
