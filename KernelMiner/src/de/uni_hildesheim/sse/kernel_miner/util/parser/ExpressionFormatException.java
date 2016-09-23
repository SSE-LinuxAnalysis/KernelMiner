package de.uni_hildesheim.sse.kernel_miner.util.parser;

/**
 * An exception to indicate that parsing an expression string failed.
 * 
 * @author Adam Krafczyk
 */
public class ExpressionFormatException extends Exception {

    private static final long serialVersionUID = 6254538502877777240L;

    public ExpressionFormatException(String message) {
        super(message);
    }
    
}
