package de.uni_hildesheim.sse.kernel_miner.util.logic.solver.cnf;

import java.util.LinkedList;
import java.util.List;

import de.uni_hildesheim.sse.kernel_miner.util.logic.Conjunction;
import de.uni_hildesheim.sse.kernel_miner.util.logic.Disjunction;
import de.uni_hildesheim.sse.kernel_miner.util.logic.False;
import de.uni_hildesheim.sse.kernel_miner.util.logic.Formula;
import de.uni_hildesheim.sse.kernel_miner.util.logic.Negation;
import de.uni_hildesheim.sse.kernel_miner.util.logic.True;
import de.uni_hildesheim.sse.kernel_miner.util.logic.Variable;

/**
 * A CNF converter based on https://www.cs.jhu.edu/~jason/tutorials/convert-to-CNF.html
 * 
 * Constants in the boolean {@link Formula} are converted to {@link #PSEUDO_TRUE} and {@link #PSEUDO_TRUE}.
 * 
 * @author Adam Krafczyk
 */
public class RecursiveCnfConverter implements ICnfConverter {
    
    public static final Variable PSEUDO_TRUE = new Variable("PSEUDO_TRUE");
    public static final Variable PSEUDO_FALSE = new Variable("PSEUDO_FALSE");
    
    @Override
    public List<Formula> convertToCnf(Formula tree) throws ConstraintException {
        List<Formula> result = new LinkedList<>();
        
        if (containsConstants(tree)) {
            result.add(PSEUDO_TRUE);
            result.add(new Negation(PSEUDO_FALSE));
            
            tree = replaceConstants(tree);
        }
        
        result.addAll(convertPrivate(tree));
        
        return result;
    }
    
    private static boolean containsConstants(Formula tree) {
        if (tree instanceof True || tree instanceof False) {
            return true;
            
        } else if (tree instanceof Disjunction) {
            return containsConstants(((Disjunction) tree).getLeft())
                    || containsConstants(((Disjunction) tree).getRight());
            
        } else if (tree instanceof Conjunction) {
            return containsConstants(((Conjunction) tree).getLeft())
                    || containsConstants(((Conjunction) tree).getRight());
            
        } else if (tree instanceof Negation) {
            return containsConstants(((Negation) tree).getFormula());
        }
        
        return false;
    }
    
    private static Formula replaceConstants(Formula tree) throws ConstraintException {
        if (tree instanceof True) {
            return PSEUDO_TRUE;
            
        } else if (tree instanceof False) {
            return PSEUDO_FALSE;
            
        } else if (tree instanceof Disjunction) {
            return new Disjunction(replaceConstants(((Disjunction) tree).getLeft()), ((Disjunction) tree).getRight());
            
        } else if (tree instanceof Conjunction) {
            return new Conjunction(replaceConstants(((Conjunction) tree).getLeft()), ((Conjunction) tree).getRight());
            
        } else if (tree instanceof Negation) {
            return new Negation(replaceConstants(((Negation) tree).getFormula()));
        }
        
        return tree;
    }
    
    protected final List<Formula> convertPrivate(Formula tree) throws ConstraintException {
        /*
         * See https://www.cs.jhu.edu/~jason/tutorials/convert-to-CNF.html
         */
        List<Formula> result = new LinkedList<>();
        
        if (tree instanceof Variable) {
            handleVariable((Variable) tree, result);
            
        } else if (tree instanceof Disjunction) {
            handleOr((Disjunction) tree, result);
            
        } else if (tree instanceof Conjunction) {
            handleAnd((Conjunction) tree, result);
            
        } else if (tree instanceof Negation) {
            handleNot((Negation) tree, result);
        } else {
            throw new ConstraintException("Invalid element in tree: " + tree);
        }
        
        return result;
    }
    
    protected void handleVariable(Variable var, List<Formula> result) throws ConstraintException {
        result.add(var);
    }
    
    protected void handleOr(Disjunction call, List<Formula> result) throws ConstraintException {
        /*
         * We have call = P v Q
         * 
         * CONVERT(P) must have the form P1 ^ P2 ^ ... ^ Pm, and
         * CONVERT(Q) must have the form Q1 ^ Q2 ^ ... ^ Qn,
         * where all the Pi and Qi are dijunctions of literals.
         * So we need a CNF formula equivalent to
         *    (P1 ^ P2 ^ ... ^ Pm) v (Q1 ^ Q2 ^ ... ^ Qn).
         * So return (P1 v Q1) ^ (P1 v Q2) ^ ... ^ (P1 v Qn)
         *         ^ (P2 v Q1) ^ (P2 v Q2) ^ ... ^ (P2 v Qn)
         *           ...
         *         ^ (Pm v Q1) ^ (Pm v Q2) ^ ... ^ (Pm v Qn)
         */
        List<Formula> leftSide = convertPrivate(call.getLeft());
        List<Formula> rightSide = convertPrivate(call.getRight());
        
        for (Formula p : leftSide) {
            for (Formula q : rightSide) {
                Formula newOr = new Disjunction(p, q);
                result.add(newOr);
            }
        }
//        result.addAll(ConditionUtils.getMaxTerms(call));
    }
    
    protected void handleAnd(Conjunction call, List<Formula> result) throws ConstraintException {
        /*
         * We have call = P ^ Q
         * 
         * CONVERT(P) must have the form P1 ^ P2 ^ ... ^ Pm, and
         * CONVERT(Q) must have the form Q1 ^ Q2 ^ ... ^ Qn,
         * where all the Pi and Qi are disjunctions of literals.
         * So return P1 ^ P2 ^ ... ^ Pm ^ Q1 ^ Q2 ^ ... ^ Qn.
         */
        
        List<Formula> leftSide = convertPrivate(call.getLeft());
        List<Formula> rightSide = convertPrivate(call.getRight());
        
        result.addAll(leftSide);
        result.addAll(rightSide);
    }
    
    protected void handleNot(Negation call, List<Formula> result) throws ConstraintException {
        if (call.getFormula() instanceof Variable) {
            // If call has the form ~A for some variable A, then return call.
            result.add(call);
            
        } else if (call.getFormula() instanceof Negation) {
            // If call has the form ~(~P), then return CONVERT(P). (double negation)
            
            Negation child = (Negation) call.getFormula();
            result.addAll(convertPrivate(child.getFormula()));
            
        } else if (call.getFormula() instanceof Disjunction) {
            // If call has the form ~(P v Q), then return CONVERT(~P ^ ~Q). (de Morgan's Law)
            
            Disjunction innerCall = (Disjunction) call.getFormula();
            
            Formula p = innerCall.getLeft();
            Formula q = innerCall.getRight();
              
            Formula notP = new Negation(p);
            Formula notQ = new Negation(q);
            Formula newAnd = new Conjunction(notP, notQ);
              
            result.addAll(convertPrivate(newAnd));
            
        } else if (call.getFormula() instanceof Conjunction) {
            // If call has the form ~(P ^ Q), then return CONVERT(~P v ~Q). (de Morgan's Law)
            Conjunction innerCall = (Conjunction) call.getFormula();
            
            Formula p = innerCall.getLeft();
            Formula q = innerCall.getRight();
              
            Formula notP = new Negation(p);
            Formula notQ = new Negation(q);
            Formula newOr = new Disjunction(notP, notQ);
              
            result.addAll(convertPrivate(newOr));
        
        } else {
            throw new ConstraintException("Invalid element in not call: " + call);
        }
    }

}
