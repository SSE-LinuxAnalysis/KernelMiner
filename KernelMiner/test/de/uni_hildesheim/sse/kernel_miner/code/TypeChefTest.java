package de.uni_hildesheim.sse.kernel_miner.code;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;

import org.junit.After;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import de.uni_hildesheim.sse.kernel_miner.code.Block;
import de.uni_hildesheim.sse.kernel_miner.code.SourceFile;
import de.uni_hildesheim.sse.kernel_miner.code.typechef.TypeChef;
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
        CHEF = new TypeChef();
        
        CHEF.setSourceDir(new File(TESTDATA, "src"));
        CHEF.setOpenVariablesFile(new File(TESTDATA, "models/model.features"));
        CHEF.setSystemRoot(new File(TESTDATA, "res/systemRoot"));
//        CHEF.addDefaultPostIncludeDirs();
        CHEF.addPostIncludeDir(new File("usr/include"));
        CHEF.addSourceIncludeDir(new File("include"));
        CHEF.setWorkingDir(TESTDATA);
        CHEF.addStaticInclude(new File(TESTDATA, "models/model.completed.h"));
        CHEF.addStaticInclude(new File(TESTDATA, "models/model.nonbool.h"));
        CHEF.addStaticInclude(new File("res/typechef/partial_conf.h"));
        CHEF.setPlatformHeader(new File("res/typechef/platform.h"));
        
        File output = File.createTempFile("typechef_output", ".zip", TESTDATA);
        output.delete();
        output.deleteOnExit();
        CHEF.setOutput(output);
    }
    
    @After
    public void removePiFiles() {
        try {
            CHEF.getOutput().deleteFile(new File("simpleFile.c.pi"));
            CHEF.getOutput().deleteFile(new File("includingFile.c.pi"));
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testSimpleFile() throws IOException {
        SourceFile file = new SourceFile(new File("simpleFile.c"));
        CHEF.runOnFile(file);
        CHEF.parseTokens(file);
        
        Iterator<Block> it = file.getBlocks().iterator();
        
        Assert.assertTrue(it.hasNext());
        Block block = it.next();
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
        CHEF.parseTokens(file);
        
        Iterator<Block> it = file.getBlocks().iterator();
        
        Assert.assertTrue(it.hasNext());
        Block block = it.next();
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
    
//    @Test // TODO: investigate
    public void testSimpleFilePresenceCondition() throws IOException {
        SourceFile file = new SourceFile(new File("simpleFile.c"));
        file.setPresenceCondition(new Variable("CONFIG_MAX"));
        
        CHEF.runOnFile(file);
        CHEF.parseTokens(file);
        
        Iterator<Block> it = file.getBlocks().iterator();
        
        Assert.assertTrue(it.hasNext());
        Block block = it.next();
        Assert.assertEquals("simpleFile.c", block.getLocation());
        Assert.assertTrue(block.getPresenceCondition() instanceof True);

        Assert.assertFalse(it.hasNext());
    }
    
    @Test
    public void testComments() throws IOException {
        SourceFile file = new SourceFile(new File("commentFile.c"));
        
        CHEF.runOnFile(file);
        CHEF.parseTokens(file);
        
        Iterator<Block> it = file.getBlocks().iterator();
        
        Assert.assertTrue(it.hasNext());
        Block block = it.next();
        Assert.assertEquals("commentFile.c", block.getLocation());
        Assert.assertTrue(block.getPresenceCondition() instanceof True);
        
        Assert.assertTrue(it.hasNext());
        block = it.next();
        Assert.assertEquals("commentFile.c", block.getLocation());
        assertVariable(block.getPresenceCondition(), "CONFIG_MAX");
        
        Assert.assertTrue(it.hasNext());
        block = it.next();
        Assert.assertEquals("commentFile.c", block.getLocation());
        Assert.assertTrue(block.getPresenceCondition() instanceof True);
        
        Assert.assertFalse(it.hasNext());
    }
    
    private TypeChef tmpTestCreateTypeChef() throws IOException {
        TypeChef chef = new TypeChef();
        
//        File resDir = new File("C:/localUserFiles/krafczyk/tmp/typechef_windows");
        
        chef.setSourceDir(new File(TESTDATA, "src"));
        
//        chef.addSourceIncludeDir(new File("include"));
//        chef.setOpenVariablesFile(new File(resDir, "kconfig_models/x86.features"));
        chef.setSystemRoot(new File(TESTDATA, "res/systemRoot"));
//        chef.addPostIncludeDir(new File("usr/include"));
        chef.setWorkingDir(TESTDATA);
        
//        chef.addStaticInclude(new File(resDir, "kconfig_models/x86.completed.h"));
//        chef.addStaticInclude(new File(resDir, "kconfig_models/x86.nonbool.h"));
//        chef.addStaticInclude(new File("res/typechef/partial_conf.h"));
//        chef.addStaticInclude(new File("res/typechef/header_override/kconfig.h"));
        
        chef.setPlatformHeader(new File("res/typechef/platform.h"));
        
//        chef.addPreprocessorDefine("__KERNEL__");
//        chef.addPreprocessorDefine("CONFIG_AS_CFI=1");
//        chef.addPreprocessorDefine("CONFIG_AS_CFI_SIGNAL_FRAME=1");
//        chef.addPreprocessorDefine("KBUILD_BASENAME=\"base\"");
//        chef.addPreprocessorDefine("KBUILD_MODNAME=\"base\"");
        
//        chef.addPreprocessorDefine("BUILDING_ACPICA");
        
        File output = File.createTempFile("typechef_output", ".zip", TESTDATA);
        output.delete();
        output.deleteOnExit();
        chef.setOutput(output);
        
        return chef;
    }
    
    // TODO
//    @Test
    public void tmpTest() throws IOException {
        SourceFile file = new SourceFile(new File("test2.c"));
//        SourceFile file = new SourceFile(new File("multilinePreprocessor.c"));
//        SourceFile file = new SourceFile(new File("includingFile.c"));
//        SourceFile file = new SourceFile(new File("commentFile.c"));
//        SourceFile file = new SourceFile(new File("simpleFile.c"));
        
        TypeChef chef = tmpTestCreateTypeChef();
        
        chef.runOnFile(file);
        chef.parseTokens(file);
        
        for (Block block : file.getBlocks()) {
            System.out.println(block);
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
    
}
