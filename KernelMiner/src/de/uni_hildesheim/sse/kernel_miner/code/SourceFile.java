package de.uni_hildesheim.sse.kernel_miner.code;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.uni_hildesheim.sse.kernel_miner.util.logic.Formula;

/**
 * Represents a single source file in the Linux source tree.
 * 
 * @author Adam Krafczyk
 */
public class SourceFile {

    private File path;
    
    private Formula presenceCondition;
    
    private List<Block> blocks;
    
    /**
     * Creates a new source file.
     * 
     * @param path The path relative to the Linux source tree root.
     */
    public SourceFile(File path) {
        this.path = path;
        this.blocks = new ArrayList<>();
    }
    
    /**
     * @return The path of this source file, relative to the Linux source tree root.
     */
    public File getPath() {
        return path;
    }
    
    /**
     * @param presenceCondition The presence condition of this file, as imposed by the build system.
     */
    public void setPresenceCondition(Formula presenceCondition) {
        this.presenceCondition = presenceCondition;
    }
    
    /**
     * @return The presence condition of this file, as imposed by the build system.
     */
    public Formula getPresenceCondition() {
        return presenceCondition;
    }
    
    /**
     * Sets the list of blocks found in this file. The previous list is overridden.
     * 
     * @param blocks The list of blocks found in this file.
     */
    public void setBlocks(List<Block> blocks) {
        this.blocks = new ArrayList<>(blocks);
    }
    
    /**
     * @return The (unmodifiable) list of blocks that are found in this file.
     */
    public List<Block> getBlocks() {
        return Collections.unmodifiableList(blocks);
    }
    
}
