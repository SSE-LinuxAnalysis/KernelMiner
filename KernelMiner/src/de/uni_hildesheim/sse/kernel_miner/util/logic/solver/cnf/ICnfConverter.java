package de.uni_hildesheim.sse.kernel_miner.util.logic.solver.cnf;

import java.util.List;

import de.uni_hildesheim.sse.kernel_miner.util.logic.Formula;

/**
 * A converter that converts any boolean {@link Formula} to CNF.
 * 
 * @author Adam Krafczyk
 */
public interface ICnfConverter {

    /**
     * Converts the given {@link Formula} to CNF.
     * 
     * @param tree The formula to create.
     * @return A list of CNF parts; the parts only contain variables, negations and disjunctions.
     *      AND'd together, the parts are a CNF representation of the given boolean {@link Formula}.
     * 
     * @throws ConstraintException If converting the {@link Formula} fails.
     */
    public List<Formula> convertToCnf(Formula tree) throws ConstraintException;
    
}
