package de.uni_hildesheim.sse.kernel_miner.util.solver;

public class SolverException extends Exception {

    private static final long serialVersionUID = -2241170619497062804L;

    public SolverException() {
    }
    
    public SolverException(String message) {
        super(message);
    }
    
    public SolverException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public SolverException(Throwable cause) {
        super(cause);
    }
    
}
