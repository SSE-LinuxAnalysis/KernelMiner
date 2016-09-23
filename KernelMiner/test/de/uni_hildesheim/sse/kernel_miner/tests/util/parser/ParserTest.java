package de.uni_hildesheim.sse.kernel_miner.tests.util.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import de.uni_hildesheim.sse.kernel_miner.util.ExpressionFormatException;
import de.uni_hildesheim.sse.kernel_miner.util.logic.Conjunction;
import de.uni_hildesheim.sse.kernel_miner.util.logic.Disjunction;
import de.uni_hildesheim.sse.kernel_miner.util.logic.Formula;
import de.uni_hildesheim.sse.kernel_miner.util.logic.Negation;
import de.uni_hildesheim.sse.kernel_miner.util.logic.Variable;
import de.uni_hildesheim.sse.kernel_miner.util.parser.CStyleBooleanGrammar;
import de.uni_hildesheim.sse.kernel_miner.util.parser.Parser;

public class ParserTest {

    @Test
    public void testSimpleVariable() throws ExpressionFormatException {
        Parser<Formula> parser = new Parser<>(new CStyleBooleanGrammar());
        String str = "A";
        
        Formula f = parser.parse(str);
        
        assertVariable(f, "A");
    }
    
    @Test
    public void testSimpleNegation() throws ExpressionFormatException {
        Parser<Formula> parser = new Parser<>(new CStyleBooleanGrammar());
        String str = "!A";
        
        Formula f = parser.parse(str);
        
        assertVariable(assertNegation(f), "A");
    }
    
    @Test
    public void testSimpleConjunction() throws ExpressionFormatException {
        Parser<Formula> parser = new Parser<>(new CStyleBooleanGrammar());
        String str = "A && B";
        
        Formula f = parser.parse(str);
        
        Formula[] t = assertConjunction(f);
        assertVariable(t[0], "A");
        assertVariable(t[1], "B");
    }
    
    @Test
    public void testSimpleDisjunction() throws ExpressionFormatException {
        Parser<Formula> parser = new Parser<>(new CStyleBooleanGrammar());
        String str = "A||B";
        
        Formula f = parser.parse(str);
        
        Formula[] t = assertDisjunction(f);
        assertVariable(t[0], "A");
        assertVariable(t[1], "B");
    }
    
    @Test
    public void testSimpleParenthesis1() throws ExpressionFormatException {
        Parser<Formula> parser = new Parser<>(new CStyleBooleanGrammar());
        String str = "A || (!B && C)";
        
        Formula f = parser.parse(str);
        
        Formula[] t = assertDisjunction(f);
        assertVariable(t[0], "A");
        Formula[] t2 = assertConjunction(t[1]);
        assertVariable(assertNegation(t2[0]), "B");
        assertVariable(t2[1], "C");
    }
    
    @Test
    public void testSimpleParenthesis2() throws ExpressionFormatException {
        Parser<Formula> parser = new Parser<>(new CStyleBooleanGrammar());
        String str = "(!(A) && ((B) || (C)))";
        
        Formula f = parser.parse(str);
        
        Formula[] t = assertConjunction(f);
        assertVariable(assertNegation(t[0]), "A");
        Formula[] t2 = assertDisjunction(t[1]);
        assertVariable(t2[0], "B");
        assertVariable(t2[1], "C");
    }
    
    @Test
    public void testComplex1() throws ExpressionFormatException {
        Parser<Formula> parser = new Parser<>(new CStyleBooleanGrammar());
        String str = "(A && B && (!A || B))";
        
        Formula f = parser.parse(str);
        
        Formula[] t1 = assertConjunction(f);
        assertVariable(t1[0], "A");
        Formula[] t2 = assertConjunction(t1[1]);
        assertVariable(t2[0], "B");
        Formula[] t3 = assertDisjunction(t2[1]);
        assertVariable(assertNegation(t3[0]), "A");
        assertVariable(t3[1], "B");
    }
    
    private static void assertVariable(Formula f, String expectedName) {
        assertTrue(f instanceof Variable);
        assertEquals(expectedName, ((Variable) f).toString());
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
