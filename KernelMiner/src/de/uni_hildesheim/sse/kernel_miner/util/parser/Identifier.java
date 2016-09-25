package de.uni_hildesheim.sse.kernel_miner.util.parser;

public final class Identifier extends Token {

    private String name;
    
    public Identifier(String name) {
        this.name = name;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    @Override
    public String toString() {
        return "[Identifier: " + name + "]";
    }
    
}
