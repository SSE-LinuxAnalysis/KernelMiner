package de.uni_hildesheim.sse.kernel_miner.kbuild;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import de.uni_hildesheim.sse.kernel_miner.code.SourceFile;
import de.uni_hildesheim.sse.kernel_miner.util.Logger;

public class KbuildParamFileTest {

    private static final File TESTDATA = new File("testdata/KbuildParamFileTest");

    @Before
    public void setUp() {
        Logger.init();
    }
    
    @Test
    public void testSimple() throws IOException {
        KbuildParamFile paramFile = new KbuildParamFile(new File(TESTDATA, "simple.sh"));
        
        SourceFile sourceFile1 = new SourceFile(new File("src/dir/file1.c"));
        List<String> params1 = paramFile.getExtraParameters(sourceFile1);
        Assert.assertEquals(1, params1.size());
        Assert.assertEquals("-DDEBUG", params1.get(0));
        
        SourceFile sourceFile2 = new SourceFile(new File("src/dir/file2.c"));
        List<String> params2 = paramFile.getExtraParameters(sourceFile2);
        Assert.assertEquals(1, params2.size());
        Assert.assertEquals("-D__KERNEL__", params2.get(0));
        

        SourceFile sourceFile3 = new SourceFile(new File("src/dir/file3.c"));
        List<String> params3 = paramFile.getExtraParameters(sourceFile3);
        Assert.assertEquals(0, params3.size());
        
    }
    
    @Test
    public void testDirectory() throws IOException {
        KbuildParamFile paramFile = new KbuildParamFile(new File(TESTDATA, "directories.sh"));

        SourceFile sourceFile1 = new SourceFile(new File("src/file1.c"));
        List<String> params1 = paramFile.getExtraParameters(sourceFile1);
        Assert.assertEquals(1, params1.size());
        Assert.assertEquals("-D__KERNEL__", params1.get(0));
        
        SourceFile sourceFile2 = new SourceFile(new File("src/dir/file2.c"));
        List<String> params2 = paramFile.getExtraParameters(sourceFile2);
        Assert.assertEquals(2, params2.size());
        Assert.assertTrue(params2.contains("-D__KERNEL__"));
        Assert.assertTrue(params2.contains("-DDIR_MODULE"));
        
        SourceFile sourceFile3 = new SourceFile(new File("src/dir/file3.c"));
        List<String> params3 = paramFile.getExtraParameters(sourceFile3);
        Assert.assertEquals(3, params3.size());
        Assert.assertTrue(params3.contains("-D__KERNEL__"));
        Assert.assertTrue(params3.contains("-DDIR_MODULE"));
        Assert.assertTrue(params3.contains("-DSPECIAL_FILE"));
    }
    
    @Test
    public void testMultipleWithOrder() throws IOException {
        KbuildParamFile paramFile = new KbuildParamFile(new File(TESTDATA, "multiple.sh"));
        
        SourceFile sourceFile1 = new SourceFile(new File("src/dir/file1.c"));
        List<String> params1 = paramFile.getExtraParameters(sourceFile1);
        Assert.assertEquals(4, params1.size());
        Assert.assertEquals("one", params1.get(0));
        Assert.assertEquals("two", params1.get(1));
        Assert.assertEquals("three", params1.get(2));
        Assert.assertEquals("four", params1.get(3));
    }
    
    @Test
    public void testWhitespace() throws IOException {
        KbuildParamFile paramFile = new KbuildParamFile(new File(TESTDATA, "whitespace.sh"));
        
        SourceFile sourceFile1 = new SourceFile(new File("src/dir/file1.c"));
        List<String> params1 = paramFile.getExtraParameters(sourceFile1);
        Assert.assertEquals(2, params1.size());
        Assert.assertEquals("one", params1.get(0));
        Assert.assertEquals("two", params1.get(1));
    }
    
    
    @Test
    public void testSameFileMultipleTimes() throws IOException {
        KbuildParamFile paramFile = new KbuildParamFile(new File(TESTDATA, "sameFileMultipleTimes.sh"));
        
        SourceFile sourceFile1 = new SourceFile(new File("src/dir/file1.c"));
        List<String> params1 = paramFile.getExtraParameters(sourceFile1);
        Assert.assertEquals(2, params1.size());
        Assert.assertTrue(params1.contains("-D1"));
        Assert.assertTrue(params1.contains("-D2"));
    }
    
    @Test
    public void testStrings() throws IOException {
        KbuildParamFile paramFile = new KbuildParamFile(new File(TESTDATA, "strings.sh"));
        
        SourceFile sourceFile1 = new SourceFile(new File("src/dir/file1.c"));
        List<String> params1 = paramFile.getExtraParameters(sourceFile1);
        Assert.assertEquals(1, params1.size());
        Assert.assertEquals("-DSTR=\"Some String with Spaces\"", params1.get(0));
        
        SourceFile sourceFile2 = new SourceFile(new File("src/dir/file2.c"));
        List<String> params2 = paramFile.getExtraParameters(sourceFile2);
        Assert.assertEquals(1, params2.size());
        Assert.assertEquals("-DSTR=Some String with Spaces", params2.get(0));
    }
    
    @Test
    public void testEscape() throws IOException {
        KbuildParamFile paramFile = new KbuildParamFile(new File(TESTDATA, "escape.sh"));
        
        SourceFile sourceFile1 = new SourceFile(new File("src/dir/file1.c"));
        List<String> params1 = paramFile.getExtraParameters(sourceFile1);
        Assert.assertEquals(1, params1.size());
        Assert.assertEquals("-DVERSION=\"1.2.3\"", params1.get(0));
        
        SourceFile sourceFile2 = new SourceFile(new File("src/dir/file2.c"));
        List<String> params2 = paramFile.getExtraParameters(sourceFile2);
        Assert.assertEquals(1, params2.size());
        Assert.assertEquals("-DSTR=Some String with Spaces", params2.get(0));
    }
    
}
