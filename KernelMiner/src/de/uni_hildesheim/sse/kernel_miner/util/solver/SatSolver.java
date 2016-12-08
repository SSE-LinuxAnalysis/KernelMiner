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
    
    public boolean isSatisfiable(Formula constraint) throws SolverException {
        ISolver solver = getSolver();
        
        
        ICnfConverter cnfConverter = new RecursiveReplacingCnfConverter(varConverter);
        
        List<Formula> dnfTerms;
        try {
            dnfTerms = cnfConverter.convertToCnf(constraint);
        } catch (ConstraintException e) {
            throw new SolverException("Can't convert constraint to DNF", e);
        }
        
        
        for (Formula term : dnfTerms) {
            int[] numbers = varConverter.convertToDimacs(term);
            try {
                solver.addClause(new VecInt(numbers));
            } catch (ContradictionException e) {
                return false;
            }
        }
        
        return isSatisfiable(solver);
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
