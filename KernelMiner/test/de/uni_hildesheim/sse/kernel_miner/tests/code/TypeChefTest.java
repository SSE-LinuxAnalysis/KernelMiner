package de.uni_hildesheim.sse.kernel_miner.tests.code;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import de.uni_hildesheim.sse.kernel_miner.code.Block;
import de.uni_hildesheim.sse.kernel_miner.code.SourceFile;
import de.uni_hildesheim.sse.kernel_miner.code.TypeChef;
import de.uni_hildesheim.sse.kernel_miner.util.Logger;
import de.uni_hildesheim.sse.kernel_miner.util.logic.Formula;
import de.uni_hildesheim.sse.kernel_miner.util.logic.Negation;
import de.uni_hildesheim.sse.kernel_miner.util.logic.True;
import de.uni_hildesheim.sse.kernel_miner.util.logic.Variable;

public class TypeChefTest {
    
    private static final File TESTDATA = new File("testdata/TypeChefTest");
    
    private static TypeChef CHEF;
    
    @BeforeClass
    public static void initLogger() {
        Logger.init();
    }
    
    @BeforeClass
    public static void createTypeChef() throws IOException {
        CHEF = new TypeChef(new File(TESTDATA, "src"), new File(TESTDATA, "models/model"));
        
        CHEF.setSystemRoot(new File(TESTDATA, "res/systemRoot"));
        CHEF.setExe(new File(TESTDATA, "res/TypeChef-0.4.1.jar"));
        CHEF.addSourceIncludeDir(new File("include"));
        
        File output = File.createTempFile("typechef_output", ".zip", TESTDATA);
        output.delete();
        output.deleteOnExit();
        CHEF.setOutput(output);
    }

    @Test
    public void testSimpleFile() throws IOException {
        SourceFile file = new SourceFile(new File("simpleFile.c"));
        CHEF.runOnFile(file);
        
        Iterator<Block> it = file.getBlocks().iterator();
        
        Assert.assertTrue(it.hasNext());
        Block block = it.next();
        Assert.assertTrue(block.getLocation().endsWith("res/typechef/platform.h"));
        Assert.assertTrue(block.getPresenceCondition() instanceof True);
        
        Assert.assertTrue(it.hasNext());
        block = it.next();
        Assert.assertTrue(block.getLocation().endsWith("res/typechef/partial_conf.h"));
        Assert.assertTrue(block.getPresenceCondition() instanceof True);

        Assert.assertTrue(it.hasNext());
        block = it.next();
        Assert.assertEquals("simpleFile.c", block.getLocation());
        Assert.assertTrue(block.getPresenceCondition() instanceof True);

        Assert.assertTrue(it.hasNext());
        block = it.next();
        Assert.assertEquals("simpleFile.c", block.getLocation());
        assertVariable(block.getPresenceCondition(), "CONFIG_MAX");

        Assert.assertTrue(it.hasNext());
        block = it.next();
        Assert.assertEquals("simpleFile.c", block.getLocation());
        assertVariable(assertNegation(block.getPresenceCondition()), "CONFIG_MAX");

        Assert.assertTrue(it.hasNext());
        block = it.next();
        Assert.assertEquals("simpleFile.c", block.getLocation());
        Assert.assertTrue(block.getPresenceCondition() instanceof True);
        
        Assert.assertFalse(it.hasNext());
    }
    
    @Test
    public void testIncludes() throws IOException {
        SourceFile file = new SourceFile(new File("includingFile.c"));
        CHEF.runOnFile(file);
        
        Iterator<Block> it = file.getBlocks().iterator();
        
        Assert.assertTrue(it.hasNext());
        Block block = it.next();
        Assert.assertTrue(block.getLocation().endsWith("res/typechef/platform.h"));
        Assert.assertTrue(block.getPresenceCondition() instanceof True);
        
        Assert.assertTrue(it.hasNext());
        block = it.next();
        Assert.assertTrue(block.getLocation().endsWith("res/typechef/partial_conf.h"));
        Assert.assertTrue(block.getPresenceCondition() instanceof True);
        
        
        Assert.assertTrue(it.hasNext());
        block = it.next();
        Assert.assertTrue(block.getLocation().endsWith("usr/include/someSystemHeader.h"));
        Assert.assertTrue(block.getPresenceCondition() instanceof True);
        
        Assert.assertTrue(it.hasNext());
        block = it.next();
        Assert.assertEquals("include/someProjectHeader.h", block.getLocation());
        Assert.assertTrue(block.getPresenceCondition() instanceof True);
        
        Assert.assertTrue(it.hasNext());
        block = it.next();
        Assert.assertEquals("includingFile.c", block.getLocation());
        Assert.assertTrue(block.getPresenceCondition() instanceof True);
        
        Assert.assertFalse(it.hasNext());
    }
    
    private static void assertVariable(Formula f, String expectedName) {
        assertTrue(f instanceof Variable);
        assertEquals(expectedName, ((Variable) f).getName());
    }
    
    private static Formula assertNegation(Formula f) {
        assertTrue(f instanceof Negation);
        return ((Negation) f).getFormula();
    }
    
}
