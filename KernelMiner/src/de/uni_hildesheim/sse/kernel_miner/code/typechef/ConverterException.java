package de.uni_hildesheim.sse.kernel_miner.code.typechef;

public class ConverterException extends Exception {

    private static final long serialVersionUID = 5800556521114856317L;

    public ConverterException() {
    }
    
    public ConverterException(String message) {
        super(message);
    }
    
    public ConverterException(Throwable cause) {
        super(cause);
    }
    
    public ConverterException(String message, Throwable cause) {
        super(message, cause);
    }
    
}
