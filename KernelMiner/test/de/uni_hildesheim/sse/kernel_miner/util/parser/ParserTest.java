package de.uni_hildesheim.sse.kernel_miner.util.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import de.uni_hildesheim.sse.kernel_miner.util.Files;
import de.uni_hildesheim.sse.kernel_miner.util.logic.Conjunction;
import de.uni_hildesheim.sse.kernel_miner.util.logic.Disjunction;
import de.uni_hildesheim.sse.kernel_miner.util.logic.Formula;
import de.uni_hildesheim.sse.kernel_miner.util.logic.Negation;
import de.uni_hildesheim.sse.kernel_miner.util.logic.Variable;
import de.uni_hildesheim.sse.kernel_miner.util.parser.CStyleBooleanGrammar;
import de.uni_hildesheim.sse.kernel_miner.util.parser.ExpressionFormatException;
import de.uni_hildesheim.sse.kernel_miner.util.parser.Parser;
import de.uni_hildesheim.sse.kernel_miner.util.parser.VariableCache;

public class ParserTest {

    private static final File TESTDATA = new File("testdata/ParserTest");
    
    @Test
    public void testSimpleVariable() throws ExpressionFormatException {
        VariableCache cache = new VariableCache();
        Parser<Formula> parser = new Parser<>(new CStyleBooleanGrammar(cache));
        String str = "A";
        
        Formula f = parser.parse(str);
        
        assertVariable(f, "A");
        Assert.assertEquals(1, cache.getNumVariables());
    }
    
    @Test
    public void testSimpleNegation() throws ExpressionFormatException {
        VariableCache cache = new VariableCache();
        Parser<Formula> parser = new Parser<>(new CStyleBooleanGrammar(cache));
        String str = "!A";
        
        Formula f = parser.parse(str);
        
        assertVariable(assertNegation(f), "A");
        Assert.assertEquals(1, cache.getNumVariables());
    }
    
    @Test
    public void testSimpleConjunction() throws ExpressionFormatException {
        VariableCache cache = new VariableCache();
        Parser<Formula> parser = new Parser<>(new CStyleBooleanGrammar(cache));
        String str = "A && B";
        
        Formula f = parser.parse(str);
        
        Formula[] t = assertConjunction(f);
        assertVariable(t[0], "A");
        assertVariable(t[1], "B");
        Assert.assertEquals(2, cache.getNumVariables());
    }
    
    @Test
    public void testSimpleDisjunction() throws ExpressionFormatException {
        VariableCache cache = new VariableCache();
        Parser<Formula> parser = new Parser<>(new CStyleBooleanGrammar(cache));
        String str = "A||B";
        
        Formula f = parser.parse(str);
        
        Formula[] t = assertDisjunction(f);
        assertVariable(t[0], "A");
        assertVariable(t[1], "B");
        Assert.assertEquals(2, cache.getNumVariables());
    }
    
    @Test
    public void testPrecedence() throws ExpressionFormatException {
        VariableCache cache = new VariableCache();
        Parser<Formula> parser = new Parser<>(new CStyleBooleanGrammar(cache));
        String str = "!A && B";
        
        Formula f = parser.parse(str);
        
        Formula[] t = assertConjunction(f);
        assertVariable(assertNegation(t[0]), "A");
        assertVariable(t[1], "B");
        Assert.assertEquals(2, cache.getNumVariables());
    }
    
    @Test
    public void testSimpleParenthesis1() throws ExpressionFormatException {
        VariableCache cache = new VariableCache();
        Parser<Formula> parser = new Parser<>(new CStyleBooleanGrammar(cache));
        String str = "A || (!B && C)";
        
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
        Parser<Formula> parser = new Parser<>(new CStyleBooleanGrammar(cache));
        String str = "(!(A) && ((B) || (C)))";
        
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
        Parser<Formula> parser = new Parser<>(new CStyleBooleanGrammar(cache));
        String str = "(A && B && (!A || B))";
        
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
    
    @Test
    public void testHuge() throws ExpressionFormatException, FileNotFoundException, IOException {
        VariableCache cache = new VariableCache();
        Parser<Formula> parser = new Parser<>(new CStyleBooleanGrammar(cache));
        
        File f = new File(TESTDATA, "huge.txt");
        String str = Files.readFile(f);
        
        parser.parse(str);
        Assert.assertEquals(11, cache.getNumVariables());
    }
    
    @Test
    public void testMalformedBrackets() {
        VariableCache cache = new VariableCache();
        Parser<Formula> parser = new Parser<>(new CStyleBooleanGrammar(cache));
        
        try {
            parser.parse("((A)");
            Assert.fail("Expected exception");
        } catch (ExpressionFormatException e) {
        }
        
        try {
            parser.parse("(A))");
            Assert.fail("Expected exception");
        } catch (ExpressionFormatException e) {
        }
    }
    
    @Test
    public void testMalformedMissingIdentifier() {
        VariableCache cache = new VariableCache();
        Parser<Formula> parser = new Parser<>(new CStyleBooleanGrammar(cache));
        
        try {
            parser.parse("");
            Assert.fail("Expected exception");
        } catch (ExpressionFormatException e) {
        }
        
        try {
            parser.parse("()");
            Assert.fail("Expected exception");
        } catch (ExpressionFormatException e) {
        }
        
        try {
            parser.parse("||");
            Assert.fail("Expected exception");
        } catch (ExpressionFormatException e) {
        }
        
        try {
            parser.parse("(||)");
            Assert.fail("Expected exception");
        } catch (ExpressionFormatException e) {
        }
    }
    
    @Test
    public void testMalformedOperator() {
        VariableCache cache = new VariableCache();
        Parser<Formula> parser = new Parser<>(new CStyleBooleanGrammar(cache));
        
        try {
            parser.parse("A &&");
            Assert.fail("Expected exception");
        } catch (ExpressionFormatException e) {
        }
        
        try {
            parser.parse("A!");
            Assert.fail("Expected exception");
        } catch (ExpressionFormatException e) {
        }
        
        try {
            parser.parse("A B");
            Assert.fail("Expected exception");
        } catch (ExpressionFormatException e) {
        }
        
        try {
            parser.parse("A & B");
            Assert.fail("Expected exception");
        } catch (ExpressionFormatException e) {
        }
        
        try {
            parser.parse("A | B");
            Assert.fail("Expected exception");
        } catch (ExpressionFormatException e) {
        }
    }
    
    @Test
    public void testMalformedCharacter() {
        VariableCache cache = new VariableCache();
        Parser<Formula> parser = new Parser<>(new CStyleBooleanGrammar(cache));
        
        try {
            parser.parse("A && BÜ");
            Assert.fail("Expected exception");
        } catch (ExpressionFormatException e) {
        }
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
