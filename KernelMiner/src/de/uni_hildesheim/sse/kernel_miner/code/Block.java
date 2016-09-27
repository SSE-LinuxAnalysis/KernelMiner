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
     * Comments are considered, too.
     * 
     * @return <code>true</code> if this block contains non-empty lines.
     */
    public boolean containsCode() {
        boolean inBlockComment = false;
        
        for (String line : lines) {
            if (line.length() > 0) {
                char[] chars = line.toCharArray();
                for (int i = 0; i < chars.length; i++) {
                    if (!inBlockComment && i < chars.length - 1 && chars[i] == '/' && chars[i + 1] == '*') {
                        inBlockComment = true;
                        i++;
                        continue;
                    }
                    
                    if (inBlockComment && i < chars.length - 1 && chars[i] == '*' && chars[i + 1] == '/') {
                        inBlockComment = false;
                        i++;
                        continue;
                    }
                    
                    if (!inBlockComment && i < chars.length - 1 && chars[i] == '/' && chars[i + 1] == '/') {
                        i++;
                        break;
                    }
                    
                    if (!inBlockComment && !Character.isWhitespace(chars[i])) {
                        return true;
                    }
                }
            }
        }
        
        return false;
    }
    
    @Override
    public String toString() {
        StringBuffer r = new StringBuffer();
        
        r.append("#file ").append(location).append('\n');
        if (presenceCondition != null) {
            r.append("#if ").append(presenceCondition.toString()).append('\n');
        }
        
        if (containsCode()) {
            for (String line : lines) {
                r.append(line).append('\n');
            }
        }
        
        if (presenceCondition != null) {
            r.append("#endif\n");
        }
        
        return r.toString();
    }

}
