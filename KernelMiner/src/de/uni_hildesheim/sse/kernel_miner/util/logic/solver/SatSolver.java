package de.uni_hildesheim.sse.kernel_miner.util.logic.solver;

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
import de.uni_hildesheim.sse.kernel_miner.util.logic.solver.cnf.ConstraintException;
import de.uni_hildesheim.sse.kernel_miner.util.logic.solver.cnf.ICnfConverter;
import de.uni_hildesheim.sse.kernel_miner.util.logic.solver.cnf.RecursiveReplacingCnfConverter;

/**
 * A solver for checking whether {@link Formula} and DIMACS models are satisfiable.
 * 
 * @author Adam Krafczyk
 */
public class SatSolver {

    private File dimacsModel;
    
    private VariableToNumberConverter varConverter;
    
    /**
     * Creates an empty sat solver. This is useful for solving {@link Formula}s
     * without a DIMACS model.
     */
    public SatSolver() {
        varConverter = new VariableToNumberConverter();
    }
    
    /**
     * Creates a solver for the given DIMACS model.
     * 
     * @param dimacsModel The DIMACS model used as a base for this solver.
     * @param prefix A prefix that is added in front of all variables found in the DIMACS model file.
     * @throws SolverException If reading the DIMACS file fails.
     */
    public SatSolver(File dimacsModel, String prefix) throws SolverException {
        this.dimacsModel = dimacsModel;
        try {
            varConverter = new VariableToNumberConverter(dimacsModel, prefix);
        } catch (IOException e) {
            throw new SolverException("Can't read DIMACS file", e);
        }
    }
    
    /**
     * @return Whether the DIMACS model in this solver is satisfiable or not.
     * @throws SolverException If the DIMACS model can't be solved.
     */
    public boolean isSatisfiable() throws SolverException {
        ISolver solver = getSolver();
        return isSatisfiable(solver);
    }
    
    /**
     * Checks whether the given {@link Formula} is satisfiable. If this solver
     * was created with a DIMACS model, then the given {@link Formula} is
     * combined with an AND with the DIMACS model and the resulting model
     * is checked for satisfiability.
     * 
     * @param formula The {@link Formula} that is checked for satisfiability.
     * @param defaultValue How unknown variables in constraint are treated:<ul>
     *      <li>If <code>null</code>: Throw an exception</li>
     *      <li>If <code>true</code>: Leave them open and continue to solve</li>
     *      <li>If <code>false</code>: Add them as permanently <code>false</code> and continue to solve</li>
     * </ul>
     * 
     * @return Whether the given {@link Formula} (+ the DIMACS model) is satisfiable.
     * @throws SolverException If the given {@link Formula} can't be converted to CNF, or solving the model fails.
     */
    public boolean isSatisfiable(Formula formula, Boolean defaultValue) throws SolverException {
        ISolver solver = getSolver();
        
        
        ICnfConverter cnfConverter = new RecursiveReplacingCnfConverter(varConverter);
        
        List<Formula> cnfTerms;
        try {
            cnfTerms = cnfConverter.convertToCnf(formula);
        } catch (ConstraintException e) {
            throw new SolverException("Can't convert constraint to CNF", e);
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
        
        // try to add the term until there are no more unknown variables
        do {
            
            try {
                numbers = varConverter.convertToDimacs(cnfTerm);
            } catch (VarNotFoundException e) {
                // use defaultValue to determine what happens to unknown variables
                if (defaultValue == null) {
                    throw new SolverException("Variable not found in DIMACS model and no default value specified", e);
                } else {
                    varConverter.addVarible(e.getVariableName());
                    if (!defaultValue) {
                        // add the variable as always false
                        cnfTerms.add(new Negation(new Variable(e.getVariableName())));
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
        
        if (dimacsModel != null) {
            Reader reader = new DimacsReader(solver);
            
            try {
                reader.parseInstance(dimacsModel.getAbsolutePath());
            } catch (ParseFormatException | IOException | ContradictionException e) {
                throw new SolverException("Can't create solver", e);
            }
        }
        
        return solver;
    }
    
}
