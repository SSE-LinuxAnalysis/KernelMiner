package de.uni_hildesheim.sse.kernel_miner.util.solver;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.sat4j.core.VecInt;
import org.sat4j.maxsat.SolverFactory;
import org.sat4j.reader.DimacsReader;
import org.sat4j.reader.ParseFormatException;
import org.sat4j.reader.Reader;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.TimeoutException;

import de.uni_hildesheim.sse.kernel_miner.util.logic.Formula;
import de.uni_hildesheim.sse.kernel_miner.util.logic.Negation;
import de.uni_hildesheim.sse.kernel_miner.util.logic.Variable;

public class SatSolver {

    private File dimacsModel;
    
    private VariableToNumberConverter varConverter;
    
    public SatSolver() {
        varConverter = new VariableToNumberConverter();
    }
    
    public SatSolver(File dimacsModel) throws SolverException {
        this.dimacsModel = dimacsModel;
        try {
            varConverter = new VariableToNumberConverter(dimacsModel, "CONFIG_");
        } catch (IOException e) {
            throw new SolverException("Can't read DIMACS file", e);
        }
    }
    
    public boolean isSatisfiable() throws SolverException {
        ISolver solver = getSolver();
        return isSatisfiable(solver);
    }
    
    /**
     * @param defaultValue How unknown variables in constraint are treated:<ul>
     *      <li>If <code>null</code>: Throw an exception</li>
     *      <li>If <code>true</code>: Leave them open and continue to solve</li>
     *      <li>If <code>false</code>: Add them as permanently <code>false</code> and continue to solve</li>
     * </ul>
     */
    public boolean isSatisfiable(Formula constraint, Boolean defaultValue) throws SolverException {
        ISolver solver = getSolver();
        
        
        ICnfConverter cnfConverter = new RecursiveReplacingCnfConverter(varConverter);
        
        List<Formula> cnfTerms;
        try {
            cnfTerms = cnfConverter.convertToCnf(constraint);
        } catch (ConstraintException e) {
            throw new SolverException("Can't convert constraint to DNF", e);
        }
        
        
        for (int i = 0; i < cnfTerms.size(); i++) {
            int[] numbers = convertToNumbers(cnfTerms.get(i), defaultValue, cnfTerms);
            try {
                solver.addClause(new VecInt(numbers));
            } catch (ContradictionException e) {
                return false;
            }
        }
        
        return isSatisfiable(solver);
    }
    
    private int[] convertToNumbers(Formula cnfTerm, Boolean defaultValue, List<Formula> cnfTerms)
            throws SolverException {
        
        int[] numbers = null;
        
        do {
            
            try {
                numbers = varConverter.convertToDimacs(cnfTerm);
            } catch (VarNotFoundException e) {
                if (defaultValue == null) {
                    throw new SolverException("Variable not found in DIMACS model and no default value specified", e);
                } else {
                    varConverter.addVarible(e.getName());
                    if (!defaultValue) {
                        // add the variable as always false
                        cnfTerms.add(new Negation(new Variable(e.getName())));
                    }
                }
            }
        } while (numbers == null);
       
        return numbers;
    }
    
    private boolean isSatisfiable(ISolver solver) throws SolverException {
        try {
            return solver.isSatisfiable();
        } catch (TimeoutException e) {
            throw new SolverException(e);
        }
    }
    
    private ISolver getSolver() throws SolverException {
        ISolver solver = SolverFactory.newDefault();
        solver.setDBSimplificationAllowed(false);
        Reader reader = new DimacsReader(solver);
        
        try {
            reader.parseInstance(dimacsModel.getAbsolutePath());
        } catch (ParseFormatException | IOException | ContradictionException e) {
            throw new SolverException("Can't create solver", e);
        }
        
        return solver;
    }
    
}
