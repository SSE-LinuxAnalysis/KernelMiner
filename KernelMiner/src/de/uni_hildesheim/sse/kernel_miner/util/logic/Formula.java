package de.uni_hildesheim.sse.kernel_miner.util.logic;

/**
 * A boolean formula.
 * 
 * @author Adam Krafczyk
 */
public abstract class Formula {
    
    /**
     * Evaluates this formula, based on the values set for the {@link Variable}s in this formula.
     * 
     * @return Whether this formula evaluates to <code>true</code> or <code>false</code>.
     */
    public abstract boolean evaluate();
    
    /**
     * @return A string representation of this formula, in a C-style like format.
     */
    @Override
    public abstract String toString();

}
