package de.uni_hildesheim.sse.kernel_miner.util.solver.cnf;

import java.util.List;

import de.uni_hildesheim.sse.kernel_miner.util.logic.Conjunction;
import de.uni_hildesheim.sse.kernel_miner.util.logic.Disjunction;
import de.uni_hildesheim.sse.kernel_miner.util.logic.Formula;
import de.uni_hildesheim.sse.kernel_miner.util.logic.Negation;
import de.uni_hildesheim.sse.kernel_miner.util.logic.Variable;
import de.uni_hildesheim.sse.kernel_miner.util.solver.VariableToNumberConverter;

/**
 * Introduces new variables to simplify overall constraint.
 * Resulting constraints are equisatisfiable.
 * 
 * Reduces the number of resulting constraints from (worst case) exponential growth to
 * quadratic growth.
 * 
 * @author Adam Krafczyk
 */
public class RecursiveReplacingCnfConverter extends RecursiveCnfConverter {
    
    private int UNIQUE = 1;
    
    private VariableToNumberConverter varConverter;
    
    /**
     * For test cases.
     */
    public RecursiveReplacingCnfConverter() {
    }
    
    /**
     * Default constructor.
     * @param varConverter A {@link VariableToNumberConverter} that we tell about new variables.
     */
    public RecursiveReplacingCnfConverter(VariableToNumberConverter varConverter) {
        this.varConverter = varConverter;
    }
    
    @Override
    protected void handleOr(Disjunction call, List<Formula> result) throws ConstraintException {
        /*
         * We have call = P v Q
         * 
         * If P and Q both are "complex", then we do the following to simplify the constraint:
         * 
         *     We introduce a new variable, Z, such that (~Z v P) ^ (Z v Q).
         *     This is satisfiable if, and only if, call is satisfiable.
         *     
         * A formula is complex, if it contains more than one variable.
         */
        
        if (isComplex(call.getLeft()) && isComplex(call.getRight())) {
            Variable z = new Variable("temp_" + (UNIQUE++));
            
            if (varConverter != null) {
                // "reserve" new number for newly introduced number
                varConverter.addVarible(z.getName());
            }
            
            Formula notZ = new Negation(z);
            
            Formula left = new Disjunction(notZ, call.getLeft());
            Formula right = new Disjunction(z, call.getRight());
            
            result.addAll(convertPrivate(new Conjunction(left, right)));
        } else {
            super.handleOr(call, result);
        }
        
    }
    
    /**
     * A formula is complex, if it contains more than one variable.
     * @param tree
     * @return <tt>true</tt> if tree contains more than one variable
     * @throws ConstraintException
     */
    private boolean isComplex(Formula tree) throws ConstraintException {
        if (tree instanceof Variable) {
            return false;
            
        } else if (tree instanceof Negation) {
            return isComplex(((Negation) tree).getFormula());
            
        } else if (tree instanceof Conjunction || tree instanceof Disjunction) {
                return true;
            
        } else {
            throw new ConstraintException("Invalid element in tree: " + tree);
        }
    }

}
