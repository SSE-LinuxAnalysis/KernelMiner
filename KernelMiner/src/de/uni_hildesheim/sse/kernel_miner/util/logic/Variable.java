package de.uni_hildesheim.sse.kernel_miner.util.logic;

public class Variable extends Formula {
    
    private String name;
    
    private boolean value;
    
    public Variable(String name) {
        this.name = name;
    }
    
    public void setValue(boolean value) {
        this.value = value;
    }

    @Override
    public boolean evaluate() {
        return value;
    }

    @Override
    public String toString() {
        return name;
    }

}
