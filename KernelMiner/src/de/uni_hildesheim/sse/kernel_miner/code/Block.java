package de.uni_hildesheim.sse.kernel_miner.code;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Block {
    
    private String presenceCondition;
    
    private Block next;
    
    private String location;
    
    private int piLineNumber;
    
    private List<String> lines;
    
    public Block(String presenceCondition, String location, int piLineNumber) {
        this.presenceCondition = presenceCondition;
        this.location = location;
        this.piLineNumber = piLineNumber;
        lines = new ArrayList<>();
    }
    
    public void addLine(String line) {
        lines.add(line);
    }
    
    public List<String> getLines() {
        return Collections.unmodifiableList(lines);
    }
    
    public String getPresenceCondition() {
        return presenceCondition;
    }
    
    public Block getNext() {
        return next;
    }
    
    public void setNext(Block next) {
        this.next = next;
    }
    
    public String getLocation() {
        return location;
    }
    
    public int getPiLineNumber() {
        return piLineNumber;
    }
    
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
