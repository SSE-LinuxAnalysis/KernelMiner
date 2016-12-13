package de.uni_hildesheim.sse.kernel_miner.util.solver.cnf;

public class ConstraintException extends Exception {

    private static final long serialVersionUID = -2153432140783061540L;

    public ConstraintException(String message) {
        super(message);
    }
    
    public ConstraintException(Throwable cause) {
        super(cause);
    }
    
    public ConstraintException(String message, Throwable cause) {
        super(message, cause);
    }
    
}
