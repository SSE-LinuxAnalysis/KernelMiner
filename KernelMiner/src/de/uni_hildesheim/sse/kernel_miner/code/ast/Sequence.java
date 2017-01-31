package de.uni_hildesheim.sse.kernel_miner.code.ast;

import java.util.ArrayList;
import java.util.List;

import de.uni_hildesheim.sse.kernel_miner.util.logic.Formula;

public class Sequence extends Statement {

    private static final long serialVersionUID = 5962988401484273597L;
    
    private List<Statement> statements;
    
    public Sequence(Formula pc) {
        super(pc);
        statements = new ArrayList<>();
    }
    
    public List<Statement> statements() {
        return statements;
    }

    @Override
    public String toString(String indentation) {
        StringBuffer result = new StringBuffer();
        
        result.append(indentation).append("[if ").append(getPc()).append("] ").append("Sequence:\n");
        for (Statement st : statements) {
            result.append(st.toString(indentation + "\t"));
        }
        
        return result.toString();
    }

}
