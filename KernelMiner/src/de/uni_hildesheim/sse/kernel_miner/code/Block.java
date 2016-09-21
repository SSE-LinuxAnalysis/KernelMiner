package de.uni_hildesheim.sse.kernel_miner.code;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.uni_hildesheim.sse.kernel_miner.util.logic.Formula;

/**
 * Represents a single #ifdef block inside a {@link SourceFile}.
 * 
 * @author Adam Krafczyk
 */
public class Block {
    
    private Formula presenceCondition;
    
    private Block next;
    
    private String location;
    
    private int piLineNumber;
    
    private List<String> lines;
    
    /**
     * Creates a new {@link Block}.
     * 
     * @param presenceCondition The presence condition of this block.
     * @param location The file this block originates from (e.g. which included header it comes from).
     * @param piLineNumber The line number in the .pi file in the TypeChef output this block was read from.
     */
    public Block(Formula presenceCondition, String location, int piLineNumber) {
        this.presenceCondition = presenceCondition;
        this.location = location;
        this.piLineNumber = piLineNumber;
        lines = new ArrayList<>();
    }
    
    /**
     * @param line A line of code that is enclosed in this block.
     */
    public void addLine(String line) {
        lines.add(line);
    }
    
    /**
     * @return The lines of code that are enclosed in this block.
     */
    public List<String> getLines() {
        return Collections.unmodifiableList(lines);
    }
    
    /**
     * @return The presence condition of this block.
     */
    public Formula getPresenceCondition() {
        return presenceCondition;
    }
    
    /**
     * @return The following block in the same {@link SourceFile}.
     *      <code>null</code> if this is the last block.
     */
    public Block getNext() {
        return next;
    }
    
    /**
     * @param next The following block in the same {@link SourceFile}.
     */
    public void setNext(Block next) {
        this.next = next;
    }
    
    /**
     * @return The file this block originates from (e.g. which included header it comes from).
     */
    public String getLocation() {
        return location;
    }
    
    /**
     * @return The line number in the .pi file in the TypeChef output this block was read from.
     */
    public int getPiLineNumber() {
        return piLineNumber;
    }
    
    /**
     * Heuristically checks whether there is actual code inside this block.
     * This is done by checking whether there are any non-empty lines inside this block.
     * <br />
     * <br />
     * TODO: This heuristic is not very good, since blocks may only contain comments.
     * 
     * @return <code>true</code> if this block contains non-empty lines.
     */
    public boolean containsCode() {
        boolean containsCode = false;
        
        for (String line : lines) {
            if (line.length() > 0) {
                for (char c : line.toCharArray()) {
                    if (!Character.isWhitespace(c)) {
                        containsCode = true;
                        break;
                    }
                }
            }
            
            if (containsCode) {
                break;
            }
        }
        
        return containsCode;
    }

}
