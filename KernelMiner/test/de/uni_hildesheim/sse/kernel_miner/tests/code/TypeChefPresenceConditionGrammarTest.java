package de.uni_hildesheim.sse.kernel_miner.tests.code;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Assert;
import org.junit.Test;

import de.uni_hildesheim.sse.kernel_miner.code.typechef.TypeChefPresenceConditionGrammar;
import de.uni_hildesheim.sse.kernel_miner.util.logic.Conjunction;
import de.uni_hildesheim.sse.kernel_miner.util.logic.Disjunction;
import de.uni_hildesheim.sse.kernel_miner.util.logic.Formula;
import de.uni_hildesheim.sse.kernel_miner.util.logic.Negation;
import de.uni_hildesheim.sse.kernel_miner.util.logic.Variable;
import de.uni_hildesheim.sse.kernel_miner.util.parser.ExpressionFormatException;
import de.uni_hildesheim.sse.kernel_miner.util.parser.Parser;
import de.uni_hildesheim.sse.kernel_miner.util.parser.VariableCache;

public class TypeChefPresenceConditionGrammarTest {

    @Test
    public void testSimpleVariable() throws ExpressionFormatException {
        VariableCache cache = new VariableCache();
        Parser<Formula> parser = new Parser<>(new TypeChefPresenceConditionGrammar(cache));
        
        Formula f1 = parser.parse("definedEx(A)");
        assertVariable(f1, "A");
        
        Formula f2 = parser.parse("defined(A)");
        assertVariable(f2, "A");
    }
    
    @Test
    public void testMalformedVariable() {
        VariableCache cache = new VariableCache();
        Parser<Formula> parser = new Parser<>(new TypeChefPresenceConditionGrammar(cache));
        
        try {
            parser.parse("");
            Assert.fail("Expected exception");
        } catch (ExpressionFormatException e) {
            
        }
        
        try {
            parser.parse("dufiend(A)");
            Assert.fail("Expected exception");
        } catch (ExpressionFormatException e) {
            
        }
        
        try {
            parser.parse("definedEx()");
            Assert.fail("Expected exception");
        } catch (ExpressionFormatException e) {
            
        }
        
        try {
            parser.parse("A");
            Assert.fail("Expected exception");
        } catch (ExpressionFormatException e) {
            
        }
        
        try {
            parser.parse("definedEx(A )");
            Assert.fail("Expected exception");
        } catch (ExpressionFormatException e) {
            
        }
        
        try {
            parser.parse("definedEx( A)");
            Assert.fail("Expected exception");
        } catch (ExpressionFormatException e) {
            
        }
    }
    
    @Test
    public void testSimpleNegation() throws ExpressionFormatException {
        VariableCache cache = new VariableCache();
        Parser<Formula> parser = new Parser<>(new TypeChefPresenceConditionGrammar(cache));
        String str = "!definedEx(A)";
        
        Formula f = parser.parse(str);
        
        assertVariable(assertNegation(f), "A");
        Assert.assertEquals(1, cache.getNumVariables());
    }
    
    @Test
    public void testSimpleConjunction() throws ExpressionFormatException {
        VariableCache cache = new VariableCache();
        Parser<Formula> parser = new Parser<>(new TypeChefPresenceConditionGrammar(cache));
        String str = "defined(A) && defined(B)";
        
        Formula f = parser.parse(str);
        
        Formula[] t = assertConjunction(f);
        assertVariable(t[0], "A");
        assertVariable(t[1], "B");
        Assert.assertEquals(2, cache.getNumVariables());
    }
    
    @Test
    public void testSimpleDisjunction() throws ExpressionFormatException {
        VariableCache cache = new VariableCache();
        Parser<Formula> parser = new Parser<>(new TypeChefPresenceConditionGrammar(cache));
        String str = "defined(A)||defined(B)";
        
        Formula f = parser.parse(str);
        
        Formula[] t = assertDisjunction(f);
        assertVariable(t[0], "A");
        assertVariable(t[1], "B");
        Assert.assertEquals(2, cache.getNumVariables());
    }
    
    @Test
    public void testPrecedence() throws ExpressionFormatException {
        VariableCache cache = new VariableCache();
        Parser<Formula> parser = new Parser<>(new TypeChefPresenceConditionGrammar(cache));
        String str = "!defined(A) && defined(B)";
        
        Formula f = parser.parse(str);
        
        Formula[] t = assertConjunction(f);
        assertVariable(assertNegation(t[0]), "A");
        assertVariable(t[1], "B");
        Assert.assertEquals(2, cache.getNumVariables());
    }
    
    @Test
    public void testSimpleParenthesis1() throws ExpressionFormatException {
        VariableCache cache = new VariableCache();
        Parser<Formula> parser = new Parser<>(new TypeChefPresenceConditionGrammar(cache));
        String str = "defined(A) || (!definedEx(B) && defined(C))";
        
        Formula f = parser.parse(str);
        
        Formula[] t = assertDisjunction(f);
        assertVariable(t[0], "A");
        Formula[] t2 = assertConjunction(t[1]);
        assertVariable(assertNegation(t2[0]), "B");
        assertVariable(t2[1], "C");
        Assert.assertEquals(3, cache.getNumVariables());
    }
    
    @Test
    public void testSimpleParenthesis2() throws ExpressionFormatException {
        VariableCache cache = new VariableCache();
        Parser<Formula> parser = new Parser<>(new TypeChefPresenceConditionGrammar(cache));
        String str = "(!(defined(A)) && ((defined(B)) || (defined(C))))";
        
        Formula f = parser.parse(str);
        
        Formula[] t = assertConjunction(f);
        assertVariable(assertNegation(t[0]), "A");
        Formula[] t2 = assertDisjunction(t[1]);
        assertVariable(t2[0], "B");
        assertVariable(t2[1], "C");
        Assert.assertEquals(3, cache.getNumVariables());
    }
    
    @Test
    public void testComplex1() throws ExpressionFormatException {
        VariableCache cache = new VariableCache();
        Parser<Formula> parser = new Parser<>(new TypeChefPresenceConditionGrammar(cache));
        String str = "(defined(A) && defined(B) && (!defined(A) || defined(B)))";
        
        Formula f = parser.parse(str);
        
        Formula[] t1 = assertConjunction(f);
        assertVariable(t1[0], "A");
        Formula[] t2 = assertConjunction(t1[1]);
        assertVariable(t2[0], "B");
        Formula[] t3 = assertDisjunction(t2[1]);
        assertVariable(assertNegation(t3[0]), "A");
        assertVariable(t3[1], "B");
        Assert.assertEquals(2, cache.getNumVariables());
    }
    
    private static void assertVariable(Formula f, String expectedName) {
        assertTrue(f instanceof Variable);
        assertEquals(expectedName, ((Variable) f).getName());
    }
    
    private static Formula assertNegation(Formula f) {
        assertTrue(f instanceof Negation);
        return ((Negation) f).getFormula();
    }
    
    private static Formula[] assertConjunction(Formula f) {
        assertTrue(f instanceof Conjunction);
        Conjunction c = (Conjunction) f;
        return new Formula[] {c.getLeft(), c.getRight()};
    }
    
    private static Formula[] assertDisjunction(Formula f) {
        assertTrue(f instanceof Disjunction);
        Disjunction c = (Disjunction) f;
        return new Formula[] {c.getLeft(), c.getRight()};
    }

}
