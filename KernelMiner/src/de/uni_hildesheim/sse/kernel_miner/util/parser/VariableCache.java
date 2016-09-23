package de.uni_hildesheim.sse.kernel_miner.util.parser;

import java.util.HashMap;
import java.util.Map;

import de.uni_hildesheim.sse.kernel_miner.util.logic.Variable;

public class VariableCache {

    private Map<String, Variable> variables;
    
    public VariableCache() {
        variables = new HashMap<>();
    }
    
    public Variable getVariable(String name) {
        Variable var = variables.get(name);
        if (var == null) {
            var = new Variable(name);
            variables.put(name, var);
        }
        return var;
    }
    
    public void clear() {
        variables.clear();
    }
    
}
