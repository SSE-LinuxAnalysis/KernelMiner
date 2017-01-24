package de.uni_hildesheim.sse.kernel_miner.util.parser;

/**
 * An operator token of an expression to be parsed.
 * 
 * @author Adam Krafczyk
 */
public final class Operator extends Token {

    private String symbol;
    
    private boolean binary;
    
    private int precedence;
    
    /**
     * Creates a new operator token.
     * 
     * @param symbol The string how this operator is represented inside an expression string.
     * @param binary Whether this is a binary or unary operator.
     * @param precedence The precedence level of this operator.
     */
    public Operator(String symbol, boolean binary, int precedence) {
        this.symbol = symbol;
        this.binary = binary;
        this.precedence = precedence;
    }
    
    /**
     * @return The string how this operator is represented inside an expression string.
     */
    public String getSymbol() {
        return symbol;
    }
    
    /**
     * @return Whether this is a binary or unary operator.
     */
    public boolean isBinary() {
        return binary;
    }
    
    /**
     * Retrieves the precedence level of this operator. Operators with higher
     * precedence levels are evaluated first, compared to operators with lower
     * precedence levels. Brackets can, of course, override this precedence order.
     * 
     * @return The precedence level of this operator.
     */
    public int getPrecedence() {
        return precedence;
    }
    
    @Override
    public String toString() {
        return "[Operator: " + symbol + "]";
    }
    
}
