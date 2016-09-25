package de.uni_hildesheim.sse.kernel_miner.util.logic;

/**
 * A boolean disjunction operator (OR).
 * 
 * @author Adam Krafczyk
 */
public final class Disjunction extends Formula {

    private Formula left;
    
    private Formula right;
    
    /**
    * Creates a boolean disjunction (OR).
    * 
    * @param left The left operand.
    * @param right The right operand.
    */
    public Disjunction(Formula left, Formula right) {
        this.left = left;
        this.right = right;
    }
    
    /**
     * @return The left operand.
     */
    public Formula getLeft() {
        return left;
    }
    
    /**
     * @return The right operand.
     */
    public Formula getRight() {
        return right;
    }
    
    @Override
    public boolean evaluate() {
        return left.evaluate() || right.evaluate();
    }

    @Override
    public String toString() {
        return "(" + left.toString() + " || " + right.toString() + ")";
    }

}
