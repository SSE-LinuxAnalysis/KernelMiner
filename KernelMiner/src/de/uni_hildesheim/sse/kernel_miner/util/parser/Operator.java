package de.uni_hildesheim.sse.kernel_miner.util.parser;

public final class Operator extends Token {

    private String symbol;
    
    private boolean binary;
    
    private int precedence;
    
    public Operator(String symbol, boolean binary, int precedence) {
        this.symbol = symbol;
        this.binary = binary;
        this.precedence = precedence;
    }
    
    public String getSymbol() {
        return symbol;
    }
    
    public boolean isBinary() {
        return binary;
    }
    
    public int getPrecedence() {
        return precedence;
    }
    
    @Override
    public String toString() {
        return "[Operator: " + symbol + "]";
    }
    
}
