package de.uni_hildesheim.sse.kernel_miner.util.parser;

public final class Bracket extends Token {
    
    private boolean closing;
    
    public Bracket(boolean closing) {
        this.closing = closing;
    }
    
    public boolean isClosing() {
        return closing;
    }
    
}
