package de.uni_hildesheim.sse.kernel_miner.util.logic;

/**
 * The boolean constant "true".
 * 
 * @author Adam Krafczyk
 */
public final class True extends Formula {

    @Override
    public boolean evaluate() {
        return true;
    }

    @Override
    public String toString() {
        return "1";
    }
    
}
