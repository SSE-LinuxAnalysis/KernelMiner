package de.uni_hildesheim.sse.kernel_miner.code.ast;

import java.io.Serializable;

import de.uni_hildesheim.sse.kernel_miner.util.logic.Formula;
import de.uni_hildesheim.sse.kernel_miner.util.logic.True;

public class Function implements Serializable {
    
    private static final long serialVersionUID = 503070325399275705L;

    private String name;
    
    private Formula pc;
    
    private Sequence body;
    
    public Function(String name, Formula pc) {
        this.name = name;
        this.pc = pc;
        body = new Sequence(new True());
    }
    
    public String getName() {
        return name;
    }
    
    public Formula getPc() {
        return pc;
    }
    
    public Sequence getBody() {
        return body;
    }

    @Override
    public String toString() {
        return toString("");
    }
    
    public String toString(String indentation) {
        StringBuffer result = new StringBuffer();
        
        result.append(indentation).append("Function: ").append(name);
        result.append(" [if ").append(pc.toString()).append("]\n");
        result.append(body.toString(indentation + "\t"));
        
        return result.toString();
    }

}
