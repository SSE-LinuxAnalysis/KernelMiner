package de.uni_hildesheim.sse.kernel_miner.tests.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import de.uni_hildesheim.sse.kernel_miner.util.Files;

public class FilesTest {

    private static final File testdata = new File("testdata/FilesTest");
    
    @Test
    public void testRelativizeWithExistingFiles() {
        File dir = testdata;
        File fileInDir = new File(dir.getPath() + "/existingFile.txt");
        
        Assert.assertEquals(dir.getPath().length() + 17, fileInDir.getPath().length());
        
        String relative = Files.relativize(fileInDir, dir);
        
        Assert.assertEquals("existingFile.txt", relative);
    }
    
    @Test
    public void testRelativizeWithNotExistingFiles() {
        File dir = new File("doesntExist");
        File fileInDir = new File(dir.getPath() + "/doesntExist.txt");
        
        Assert.assertEquals(dir.getPath().length() + 16, fileInDir.getPath().length());
        
        String relative = Files.relativize(fileInDir, dir);
        
        Assert.assertEquals("doesntExist.txt", relative);
    }
    
    @Test
    public void testRelativizeMultipleDirs() {
        File dir = new File("doesntExist/a/b");
        File fileInDir = new File(dir.getPath() + "/doesntExist.txt");
        
        Assert.assertEquals(dir.getPath().length() + 16, fileInDir.getPath().length());
        
        String relative = Files.relativize(fileInDir, dir.getParentFile());
        
        Assert.assertEquals("b/doesntExist.txt", relative);
    }
    
    @Test
    public void testRelativizeDotDot() {
        File dir = new File("doesntExist/a/b");
        File fileInDir = new File(dir.getPath() + "/../doesntExist.txt");
        
        Assert.assertEquals(dir.getPath().length() + 19, fileInDir.getPath().length());
        
        String relative1 = Files.relativize(fileInDir, dir.getParentFile());
        
        Assert.assertEquals("doesntExist.txt", relative1);
    }
    
    @Test
    public void testReadFileSmallText() throws IOException {
        File toRead = new File(testdata, "testReadFile1.txt");

        String content = Files.readFile(toRead);

        Assert.assertEquals("This is a test\nLine 2\n", content);
    }

    @Test
    public void testReadFileBigText() throws IOException {
        File toRead = new File(testdata, "testReadFile2.txt");

        String content = Files.readFile(toRead);

        String expected = "This is a longer text. It contains more than 512 characters, to check the case that more than one buffer needs to be used.\n";
        expected = expected + expected + expected + expected;

        Assert.assertEquals(expected, content);
    }
    
    @Test
    public void testReadFileNonExisting() throws IOException {
        File toRead = new File(testdata, "doesntExist.txt");
        
        try {
            Files.readFile(toRead);
            Assert.fail("Expected exception");
        } catch (FileNotFoundException e) {
            
        }
    }

    @Test
    public void testReadStreamShortText() throws IOException {
        String expected = "This is a test text";
        
        String content = Files.readStream(new ByteArrayInputStream(expected.getBytes()));
        
        Assert.assertEquals(expected, content);
    }
    
    @Test
    public void testReadStreamBigText() throws IOException {
        String expected = "This is a longer text. It contains more than 512 characters, to check the case that more than one buffer needs to be used.\n";
        expected = expected + expected + expected + expected;
        
        String content = Files.readStream(new ByteArrayInputStream(expected.getBytes()));
        
        Assert.assertEquals(expected, content);
    }
    
    @Test
    public void testWriteFile() throws IOException {
        File toWrite = new File(testdata, "testWriteFile.txt");
        toWrite.deleteOnExit();
        Assert.assertFalse(toWrite.exists());
        
        String content = "This is a test text.\nLine 2\n";
        Files.writeFile(toWrite, content);
        
        Assert.assertTrue(toWrite.exists());
        Assert.assertEquals(content, Files.readFile(toWrite));
        
        toWrite.delete();
    }
    
    @Test
    public void testOverwriteFile() throws IOException {
        File toWrite = new File(testdata, "testOverwriteFile.txt");
        toWrite.deleteOnExit();
        Assert.assertFalse(toWrite.exists());
        
        String content1 = "Text 1\n";
        Files.writeFile(toWrite, content1);
        
        Assert.assertTrue(toWrite.exists());
        Assert.assertEquals(content1, Files.readFile(toWrite));
        
        String content2 = "Text 2\n";
        Files.writeFile(toWrite, content2);
        
        Assert.assertTrue(toWrite.exists());
        Assert.assertEquals(content2, Files.readFile(toWrite));
        
        toWrite.delete();
    }

}
