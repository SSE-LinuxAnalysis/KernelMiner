package de.uni_hildesheim.sse.kernel_miner.util.logic;

import org.junit.Assert;
import org.junit.Test;

import de.uni_hildesheim.sse.kernel_miner.util.logic.Conjunction;
import de.uni_hildesheim.sse.kernel_miner.util.logic.Disjunction;
import de.uni_hildesheim.sse.kernel_miner.util.logic.False;
import de.uni_hildesheim.sse.kernel_miner.util.logic.Formula;
import de.uni_hildesheim.sse.kernel_miner.util.logic.Negation;
import de.uni_hildesheim.sse.kernel_miner.util.logic.True;
import de.uni_hildesheim.sse.kernel_miner.util.logic.Variable;

public class FormulaTest {

    @Test
    public void testToStringComplex() {
        Formula f = new Conjunction(
                new Disjunction(new Variable("A"), new Negation(new True())),
                new Conjunction(new Negation(new Variable("B")), new False())
        );
        
        
        Assert.assertEquals("((A || !1) && (!B && 0))", f.toString());
    }
    
    @Test
    public void testEvaluateSimpleVariable() {
        Variable a = new Variable("A");
        
        a.setValue(true);
        Assert.assertTrue(a.evaluate());
        
        a.setValue(false);
        Assert.assertFalse(a.evaluate());
    }
    
    @Test
    public void testEvaluateSimpleConstants() {
        Assert.assertTrue(new True().evaluate());
        Assert.assertFalse(new False().evaluate());
    }
    
    @Test
    public void testEvaluateSimpleNegation() {
        Variable a = new Variable("A");
        Formula f = new Negation(a);
        
        a.setValue(true);
        Assert.assertFalse(f.evaluate());
        
        a.setValue(false);
        Assert.assertTrue(f.evaluate());
    }
    
    @Test
    public void testEvaluateSimpleConjunction() {
        Variable a = new Variable("A");
        Variable b = new Variable("B");
        Formula f = new Conjunction(a, b);
        
        a.setValue(true);
        b.setValue(true);
        Assert.assertTrue(f.evaluate());
        
        a.setValue(true);
        b.setValue(false);
        Assert.assertFalse(f.evaluate());
        
        a.setValue(false);
        b.setValue(true);
        Assert.assertFalse(f.evaluate());
        
        a.setValue(false);
        b.setValue(false);
        Assert.assertFalse(f.evaluate());
    }
    
    @Test
    public void testEvaluateSimpleDisjunction() {
        Variable a = new Variable("A");
        Variable b = new Variable("B");
        Formula f = new Disjunction(a, b);
        
        a.setValue(true);
        b.setValue(true);
        Assert.assertTrue(f.evaluate());
        
        a.setValue(true);
        b.setValue(false);
        Assert.assertTrue(f.evaluate());
        
        a.setValue(false);
        b.setValue(true);
        Assert.assertTrue(f.evaluate());
        
        a.setValue(false);
        b.setValue(false);
        Assert.assertFalse(f.evaluate());
    }
    
    @Test
    public void testEvaluateComplex() {
        Variable a = new Variable("A");
        Variable b = new Variable("B");
        Formula f = new Conjunction(
                new Disjunction(a, new Negation(new True())),
                new Negation(b)
        );
        
        a.setValue(true);
        b.setValue(true);
        Assert.assertFalse(f.evaluate());
        
        a.setValue(true);
        b.setValue(false);
        Assert.assertTrue(f.evaluate());
        
        a.setValue(false);
        b.setValue(true);
        Assert.assertFalse(f.evaluate());
        
        a.setValue(false);
        b.setValue(false);
        Assert.assertFalse(f.evaluate());
    }
    
    @Test
    public void testEqual() {
        Formula f1 = new Conjunction(
                new Disjunction(new Variable("A"), new Negation(new True())),
                new Conjunction(new Negation(new Variable("B")), new False())
        );
        Assert.assertFalse(f1.equals(new Object()));
        
        Formula f2 = new Conjunction(
                new Disjunction(new Variable("A"), new Negation(new True())),
                new Conjunction(new Negation(new Variable("B")), new False())
        );
        Assert.assertTrue(f1.equals(f2));
        
        Formula f3 = new Conjunction(
                new Disjunction(new Variable("A"), new Negation(new True())),
                new Conjunction(new Negation(new Variable("C")), new False())
        );
        Assert.assertFalse(f1.equals(f3));
        
        Formula f4 = new Conjunction(
                new Disjunction(new Variable("A"), new False()),
                new Conjunction(new Negation(new Variable("B")), new False())
                );
        Assert.assertFalse(f1.equals(f4));
        
        Formula f5 = new Conjunction(
                new Conjunction(new Variable("A"), new False()),
                new Conjunction(new Negation(new Variable("B")), new False())
                );
        Assert.assertFalse(f1.equals(f5));
    }

}
