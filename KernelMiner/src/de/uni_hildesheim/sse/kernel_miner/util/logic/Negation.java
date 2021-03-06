package de.uni_hildesheim.sse.kernel_miner.util.logic;

/**
 * The boolean negation operator (NOT).
 * 
 * @author Adam Krafczyk
 */
public final class Negation extends Formula {
    
    private static final long serialVersionUID = -7539218655156469390L;
    
    private Formula formula;
    
    /**
     * Creates a boolean negation (NOT).
     * 
     * @param formula The operand of this negation.
     */
    public Negation(Formula formula) {
        this.formula = formula;
    }
    
    /**
     * @return The operand of this negation.
     */
    public Formula getFormula() {
        return formula;
    }

    @Override
    public boolean evaluate() {
        return !formula.evaluate();
    }

    @Override
    public String toString() {
        return "!" + formula;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Negation) {
            Negation other = (Negation) obj;
            return formula.equals(other.formula);
        }
        return false;
    }

}
