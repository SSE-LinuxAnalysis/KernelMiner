package de.uni_hildesheim.sse.kernel_miner.util.logic;

public final class Negation extends Formula {
    
    private Formula formula;
    
    public Negation(Formula formula) {
        this.formula = formula;
    }
    
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

}
