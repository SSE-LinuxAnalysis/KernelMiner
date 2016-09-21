package de.uni_hildesheim.sse.kernel_miner.util.logic;

public class Conjunction extends Formula {

    private Formula left;
    
    private Formula right;
    
    public Conjunction(Formula left, Formula right) {
        this.left = left;
        this.right = right;
    }
    
    @Override
    public boolean evaluate() {
        return left.evaluate() && right.evaluate();
    }

    @Override
    public String toString() {
        return "(" + left.toString() + " && " + right.toString() + ")";
    }

}
