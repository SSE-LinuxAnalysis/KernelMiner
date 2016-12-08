package de.uni_hildesheim.sse.kernel_miner.util.solver;

import java.util.List;

import de.uni_hildesheim.sse.kernel_miner.util.logic.Formula;

public interface ICnfConverter {

    public List<Formula> convertToCnf(Formula tree) throws ConstraintException;
    
}
