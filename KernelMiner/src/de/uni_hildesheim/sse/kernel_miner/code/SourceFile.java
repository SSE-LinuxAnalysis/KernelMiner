package de.uni_hildesheim.sse.kernel_miner.code;

import java.io.File;

import de.uni_hildesheim.sse.kernel_miner.util.logic.Formula;

public class SourceFile {

    private File path;
    
    private Formula presenceCondition;
    
    private Block firstBlock;
    
    public SourceFile(File path) {
        this.path = path;
    }
    
    public File getPath() {
        return path;
    }
    
    public void setPresenceCondition(Formula presenceCondition) {
        this.presenceCondition = presenceCondition;
    }
    
    public Formula getPresenceCondition() {
        return presenceCondition;
    }
    
    public void setFirstBlock(Block firstBlock) {
        this.firstBlock = firstBlock;
    }
    
    public Block getFirstBlock() {
        return firstBlock;
    }
    
}
