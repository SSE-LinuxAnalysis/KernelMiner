package de.uni_hildesheim.sse.kernel_miner.code.ast;

import java.io.Serializable;

public abstract class Expression implements Serializable {

    private static final long serialVersionUID = 4885341030255256753L;

    @Override
    public String toString() {
        return toString("");
    }
    
    public abstract String toString(String indentation);
    
}
