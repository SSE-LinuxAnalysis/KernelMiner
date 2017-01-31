package de.uni_hildesheim.sse.kernel_miner.code.ast;

import java.io.Serializable;

import de.uni_hildesheim.sse.kernel_miner.util.logic.Formula;

public abstract class Statement implements Serializable {

    private static final long serialVersionUID = -3355241115256941804L;

    private Formula pc;
    
    public Statement(Formula pc) {
        this.pc = pc;
    }
    
    public Formula getPc() {
        return pc;
    }
    
    @Override
    public String toString() {
        return toString("");
    }
    
    public abstract String toString(String indentation);
    
}
