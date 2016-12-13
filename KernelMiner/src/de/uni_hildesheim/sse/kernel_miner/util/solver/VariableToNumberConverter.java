package de.uni_hildesheim.sse.kernel_miner.util.solver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.uni_hildesheim.sse.kernel_miner.util.logic.Disjunction;
import de.uni_hildesheim.sse.kernel_miner.util.logic.Formula;
import de.uni_hildesheim.sse.kernel_miner.util.logic.Negation;
import de.uni_hildesheim.sse.kernel_miner.util.logic.Variable;

/**
 * A converter to convert variables names into numbers of a given DIMACS model,
 * and vice-versa.
 * 
 * @author Adam Krafczyk
 */
public class VariableToNumberConverter {
    
    private HashMap<String, Integer> mapping;
    
    private int maxNumber = 0;
    
    /**
     * Creates a {@link VariableToNumberConverter} for the given DIMACS model.
     * This expects the mapping to be at the top of the DIMACS file in the format:
     * <br /><code>c &lt;NUMBER&gt; &lt;VARIABLE_NAME&gt;</code>
     * <br />(undertaker format)
     * 
     * @param dimacsModel The DIMACS model file.
     * @param prefix A prefix that is added before all variables found in the DIMACS model file.
     * 
     * @throws IOException If reading the file fails.
     */
    public VariableToNumberConverter(File dimacsModel, String prefix) throws IOException {
        this();
        read(dimacsModel, prefix);
    }
    
    /**
     * Creates an empty {@link VariableToNumberConverter}.
     */
    public VariableToNumberConverter() {
        mapping = new HashMap<>();
    }
    
    /**
     * Parses the given DIMACS file and fills the internal mapping.
     * This expects the mapping to be at the top of the DIMACS file in the format:
     * <br /><code>c &lt;NUMBER&gt; &lt;VARIABLE_NAME&gt;</code>
     * <br />(undertaker format)
     * 
     * @param dimacsModel The file to parse.
     * @throws IOException If reading the file fails.
     */
    private void read(File dimacsModel, String prefix) throws IOException {
        BufferedReader file = new BufferedReader(new FileReader(dimacsModel));
        
        Pattern pattern = Pattern.compile("^c (\\d+) (.+)$");
        
        String line = null;
        while ((line = file.readLine()) != null) {
            if (!line.startsWith("c")) {
                break;
            }
            Matcher matcher = pattern.matcher(line);
            if (matcher.matches()) {
                int number = Integer.parseInt(matcher.group(1));
                if (number > maxNumber) {
                    maxNumber = number;
                }
                mapping.put(prefix + matcher.group(2), number);
            } 
        }
        
        file.close();
    }
    
    /**
     * @param name The name of the variable to search.
     * @return The number of the variable in the DIMACS model.
     * @throws VarNotFoundException If the variable was not found in the DIMACS model.
     */
    public int getNumber(String name) throws VarNotFoundException {
        try {
            return mapping.get(name);
        } catch (NullPointerException e) {
            throw new VarNotFoundException(name);
        }
    }
    
    /**
     * @param number The number of the variable in the DIMACS model.
     * @return The name of the variable, or <code>null</code> if not found.
     */
    public String getName(int number) {
        for (String name : mapping.keySet()) {
            // TODO: ENABLE_ for busybox
            try {
                if (getNumber(name) == number) {
                    return name;
                }
            } catch (VarNotFoundException e) {
                // can't happen
            }
        }
        return null;
    }
    
    
    /**
     * Adds a new variable <-> number mapping. Assures, that a variable with the
     * same name always gets the same number (i.e. it first checks, whether the
     * variable is already present).
     * 
     * @param name The name of the new variable.
     * @return The number for the new variable.
     */
    public int addVarible(String name) {
        return getOrCreate(name);
    }
    
    private int getOrCreate(String name) {
        if (mapping.get(name) != null) {
            return mapping.get(name);
        } else {
            mapping.put(name, ++maxNumber);
            return maxNumber;
        }
    }
    
    /**
     * @param a The left part.
     * @param b The right part.
     * @return The two parts merged together.
     */
    private static int[] merge(int[] a, int[] b) {
        int[] combined = new int[a.length + b.length];
        System.arraycopy(a, 0, combined, 0, a.length);
        System.arraycopy(b, 0, combined, a.length, b.length);
        return combined;
    }

    public int[] convertToDimacs(Formula disjunctionTerm) throws SolverException, VarNotFoundException {
        if (disjunctionTerm instanceof Negation) {
            Formula var = ((Negation) disjunctionTerm).getFormula();
            if (!(var instanceof Variable)) {
                throw new SolverException("Invalid disjunctionTerm, expected variable after negation: " + disjunctionTerm);
            }
            int number = getNumber(((Variable) var).getName());
            return new int[] { -1 * number };
            
        } else if (disjunctionTerm instanceof Disjunction) {
            int[] left = convertToDimacs(((Disjunction) disjunctionTerm).getLeft());
            int[] right = convertToDimacs(((Disjunction) disjunctionTerm).getRight());
            return merge(left, right);
            
        } else if (disjunctionTerm instanceof Variable) {
            int number = getNumber(((Variable) disjunctionTerm).getName());
            return new int[] { number };
            
        } else {
            // TODO: true, false
            throw new SolverException("Invalid element in disjunctionTerm: " + disjunctionTerm);
        }
    }
    
}
