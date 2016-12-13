package de.uni_hildesheim.sse.kernel_miner.kbuild;

import org.junit.Assert;
import org.junit.Test;

import de.uni_hildesheim.sse.kernel_miner.kbuild.KbuildMinerPcGrammar;
import de.uni_hildesheim.sse.kernel_miner.util.logic.Conjunction;
import de.uni_hildesheim.sse.kernel_miner.util.logic.Disjunction;
import de.uni_hildesheim.sse.kernel_miner.util.logic.Formula;
import de.uni_hildesheim.sse.kernel_miner.util.logic.Negation;
import de.uni_hildesheim.sse.kernel_miner.util.logic.True;
import de.uni_hildesheim.sse.kernel_miner.util.logic.Variable;
import de.uni_hildesheim.sse.kernel_miner.util.parser.ExpressionFormatException;
import de.uni_hildesheim.sse.kernel_miner.util.parser.Parser;
import de.uni_hildesheim.sse.kernel_miner.util.parser.VariableCache;

public class KbuildMinerPcGrammarTest {

    @Test
    public void testSimpleVariable() throws ExpressionFormatException {
        VariableCache cache = new VariableCache();
        Parser<Formula> parser = new Parser<>(new KbuildMinerPcGrammar(cache));
        
        Formula f1 = parser.parse("A == \"y\"");
        assertVariable(f1, "CONFIG_A");
        
        Formula f2 = parser.parse("A == \"m\"");
        assertVariable(f2, "CONFIG_A_MODULE");
        
        Formula f3 = parser.parse("A == \"yes\"");
        assertVariable(f3, "CONFIG_A");
        
        Formula f4 = parser.parse("A != \"y\"");
        assertVariable(assertNegation(f4), "CONFIG_A");
        
        Formula f5 = parser.parse("A != \"m\"");
        assertVariable(assertNegation(f5), "CONFIG_A_MODULE");
        
        Formula f6 = parser.parse("A");
        assertVariable(f6, "CONFIG_A");
    }
    
    @Test
    public void testMalformedVariable() {
        VariableCache cache = new VariableCache();
        Parser<Formula> parser = new Parser<>(new KbuildMinerPcGrammar(cache));
        
        try {
            parser.parse("");
            Assert.fail("Expected exception");
        } catch (ExpressionFormatException e) {
            
        }
        
        try {
            parser.parse("A = \"y\"");
            Assert.fail("Expected exception");
        } catch (ExpressionFormatException e) {
            
        }
        
        try {
            parser.parse("A=\"y\"");
            Assert.fail("Expected exception");
        } catch (ExpressionFormatException e) {
            
        }
        
        try {
            parser.parse("A ! \"y\"");
            Assert.fail("Expected exception");
        } catch (ExpressionFormatException e) {
            
        }
        
        try {
            parser.parse("A == \"ja\"");
            Assert.fail("Expected exception");
        } catch (ExpressionFormatException e) {
            
        }
        
        try {
            parser.parse("A == yes");
            Assert.fail("Expected exception");
        } catch (ExpressionFormatException e) {
            
        }
    }
    
    @Test
    public void testSimpleConstant() throws ExpressionFormatException {
        VariableCache cache = new VariableCache();
        Parser<Formula> parser = new Parser<>(new KbuildMinerPcGrammar(cache));
        
        Formula f1 = parser.parse("[TRUE]");
        Assert.assertTrue(f1 instanceof True);
    }
    
    @Test
    public void testMalformedConstant() {
        VariableCache cache = new VariableCache();
        Parser<Formula> parser = new Parser<>(new KbuildMinerPcGrammar(cache));
        
        try {
            parser.parse("[TREU]");
            Assert.fail("Expected exception");
        } catch (ExpressionFormatException e) {
            
        }
        
        try {
            parser.parse("[FALSE]");
            Assert.fail("Expected exception");
        } catch (ExpressionFormatException e) {
            
        }
        
        try {
            parser.parse("[TRUE");
            Assert.fail("Expected exception");
        } catch (ExpressionFormatException e) {
            
        }
        
        try {
            parser.parse("TRUE]");
            Assert.fail("Expected exception");
        } catch (ExpressionFormatException e) {
            
        }
    }
    
    @Test
    public void testSimpleNegation() throws ExpressionFormatException {
        VariableCache cache = new VariableCache();
        Parser<Formula> parser = new Parser<>(new KbuildMinerPcGrammar(cache));
        String str = "!(A == \"y\")";
        
        Formula f = parser.parse(str);
        
        assertVariable(assertNegation(f), "CONFIG_A");
        Assert.assertEquals(1, cache.getNumVariables());
    }
    
    @Test
    public void testSimpleConjunction() throws ExpressionFormatException {
        VariableCache cache = new VariableCache();
        Parser<Formula> parser = new Parser<>(new KbuildMinerPcGrammar(cache));
        String str = "(A == \"y\") && B == \"m\"";
        
        Formula f = parser.parse(str);
        
        Formula[] t = assertConjunction(f);
        assertVariable(t[0], "CONFIG_A");
        assertVariable(t[1], "CONFIG_B_MODULE");
        Assert.assertEquals(2, cache.getNumVariables());
    }
    
    @Test
    public void testSimpleDisjunction() throws ExpressionFormatException {
        VariableCache cache = new VariableCache();
        Parser<Formula> parser = new Parser<>(new KbuildMinerPcGrammar(cache));
        String str = "(A == \"m\")||B";
        
        Formula f = parser.parse(str);
        
        Formula[] t = assertDisjunction(f);
        assertVariable(t[0], "CONFIG_A_MODULE");
        assertVariable(t[1], "CONFIG_B");
        Assert.assertEquals(2, cache.getNumVariables());
    }
    
    @Test
    public void testPrecedence() throws ExpressionFormatException {
        VariableCache cache = new VariableCache();
        Parser<Formula> parser = new Parser<>(new KbuildMinerPcGrammar(cache));
        String str = "!A == \"y\" && B == \"y\"";
        
        Formula f = parser.parse(str);
        
        Formula[] t = assertConjunction(f);
        assertVariable(assertNegation(t[0]), "CONFIG_A");
        assertVariable(t[1], "CONFIG_B");
        Assert.assertEquals(2, cache.getNumVariables());
    }
    
    @Test
    public void testSimpleParenthesis1() throws ExpressionFormatException {
        VariableCache cache = new VariableCache();
        Parser<Formula> parser = new Parser<>(new KbuildMinerPcGrammar(cache));
        String str = "(A == \"y\") || (!(B == \"y\") && (C == \"m\"))";
        
        Formula f = parser.parse(str);
        
        Formula[] t = assertDisjunction(f);
        assertVariable(t[0], "CONFIG_A");
        Formula[] t2 = assertConjunction(t[1]);
        assertVariable(assertNegation(t2[0]), "CONFIG_B");
        assertVariable(t2[1], "CONFIG_C_MODULE");
        Assert.assertEquals(3, cache.getNumVariables());
    }
    
    @Test
    public void testSimpleParenthesis2() throws ExpressionFormatException {
        VariableCache cache = new VariableCache();
        Parser<Formula> parser = new Parser<>(new KbuildMinerPcGrammar(cache));
        String str = "(!(A == \"y\") && ((B == \"m\") || (C == \"yes\")))";
        
        Formula f = parser.parse(str);
        
        Formula[] t = assertConjunction(f);
        assertVariable(assertNegation(t[0]), "CONFIG_A");
        Formula[] t2 = assertDisjunction(t[1]);
        assertVariable(t2[0], "CONFIG_B_MODULE");
        assertVariable(t2[1], "CONFIG_C");
        Assert.assertEquals(3, cache.getNumVariables());
    }
    
    @Test
    public void testComplex1() throws ExpressionFormatException {
        VariableCache cache = new VariableCache();
        Parser<Formula> parser = new Parser<>(new KbuildMinerPcGrammar(cache));
        String str = "(A == \"y\" && B == \"y\" && (!A == \"y\" || B))";
        
        Formula f = parser.parse(str);
        
        Formula[] t1 = assertConjunction(f);
        assertVariable(t1[0], "CONFIG_A");
        Formula[] t2 = assertConjunction(t1[1]);
        assertVariable(t2[0], "CONFIG_B");
        Formula[] t3 = assertDisjunction(t2[1]);
        assertVariable(assertNegation(t3[0]), "CONFIG_A");
        assertVariable(t3[1], "CONFIG_B");
        Assert.assertEquals(2, cache.getNumVariables());
    }
    
    private static void assertVariable(Formula f, String expectedName) {
        Assert.assertTrue(f instanceof Variable);
        Assert.assertEquals(expectedName, ((Variable) f).getName());
    }
    
    private static Formula assertNegation(Formula f) {
        Assert.assertTrue(f instanceof Negation);
        return ((Negation) f).getFormula();
    }
    
    private static Formula[] assertConjunction(Formula f) {
        Assert.assertTrue(f instanceof Conjunction);
        Conjunction c = (Conjunction) f;
        return new Formula[] {c.getLeft(), c.getRight()};
    }
    
    private static Formula[] assertDisjunction(Formula f) {
        Assert.assertTrue(f instanceof Disjunction);
        Disjunction c = (Disjunction) f;
        return new Formula[] {c.getLeft(), c.getRight()};
    }

}
