package de.uni_hildesheim.sse.kernel_miner.code.ast;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class File implements Serializable {
    
    private static final long serialVersionUID = 8466438812607108023L;
    
    private String name;
    
    private List<Function> functions;
    
    public File(String name) {
        this.name = name;
        functions = new ArrayList<>();
    }
    
    public String getName() {
        return name;
    }
    
    public List<Function> functions() {
        return functions;
    }
    
    @Override
    public String toString() {
        return toString("");
    }
    
    public String toString(String indentation) {
        StringBuffer result = new StringBuffer();
        result.append(indentation).append("File: ").append(name).append("\n");

        for (Function function : functions) {
            result.append(function.toString(indentation + "\t"));
        }
        
        return result.toString();
    }

}
