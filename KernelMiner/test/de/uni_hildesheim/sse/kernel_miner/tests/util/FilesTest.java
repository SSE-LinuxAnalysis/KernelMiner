package de.uni_hildesheim.sse.kernel_miner.tests.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import de.uni_hildesheim.sse.kernel_miner.util.Files;

public class FilesTest {

    @Test
    public void testReadFile1() throws IOException {
        File toRead = new File("testdata/testReadFile1.txt");

        String content = Files.readFile(toRead);

        Assert.assertEquals("This is a test\nLine 2\n", content);
    }

    @Test
    public void testReadFile2() throws IOException {
        File toRead = new File("testdata/testReadFile2.txt");

        String content = Files.readFile(toRead);

        String expected = "This is a longer text. It contains more than 512 characters, to check the case that more than one buffer needs to be used.\n";
        expected = expected + expected + expected + expected;

        Assert.assertEquals(expected, content);
    }

    @Test
    public void testReadStream1() throws IOException {
        String expected = "This is a test text";
        
        String content = Files.readFile(new ByteArrayInputStream(expected.getBytes()));
        
        Assert.assertEquals(expected, content);
    }
    
    @Test
    public void testReadStream2() throws IOException {
        String expected = "This is a longer text. It contains more than 512 characters, to check the case that more than one buffer needs to be used.\n";
        expected = expected + expected + expected + expected;
        
        String content = Files.readFile(new ByteArrayInputStream(expected.getBytes()));
        
        Assert.assertEquals(expected, content);
    }
    
    @Test
    public void testWriteFile() throws IOException {
        File toWrite = new File("testdata/testWriteFile.txt");
        if (toWrite.exists()) {
            toWrite.delete();
        }
        Assert.assertFalse(toWrite.exists());
        
        String content = "This is a test text.\nLine 2\n";
        Files.writeFile(toWrite, content);
        
        Assert.assertTrue(toWrite.exists());
        Assert.assertEquals(content, Files.readFile(toWrite));
        
        toWrite.delete();
    }

}
