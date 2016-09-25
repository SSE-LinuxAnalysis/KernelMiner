package de.uni_hildesheim.sse.kernel_miner.tests.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import de.uni_hildesheim.sse.kernel_miner.util.Files;
import de.uni_hildesheim.sse.kernel_miner.util.ZipArchive;

public class ZipArchiveTest {

    private static final File testdata = new File("testdata/ZipArchiveTest");
    
    @Test
    public void testCreation() throws IOException {
        File zipFile = new File(testdata, "testCreation.zip");
        zipFile.deleteOnExit();
        if (zipFile.exists()) {
            zipFile.delete();
        }
        Assert.assertFalse(zipFile.exists());
        
        ZipArchive archive = new ZipArchive(zipFile);
        Assert.assertFalse(zipFile.exists());
        
        archive.writeFile(new File("test.txt"), "Hello World\n");
        Assert.assertTrue(zipFile.exists());
    }
    
    @Test
    public void testContainsFile() {
        File zipFile = new File(testdata, "archive.zip");
        ZipArchive archive = new ZipArchive(zipFile);
        
        Assert.assertTrue(archive.containsFile(new File("test.txt")));
        Assert.assertTrue(archive.containsFile(new File("dir/test.txt")));
        
        Assert.assertFalse(archive.containsFile(new File("doesntExist.txt")));
        Assert.assertFalse(archive.containsFile(new File("doesntExist/doesnExist.txt")));
        Assert.assertFalse(archive.containsFile(new File("dir/doesntExist.txt")));
    }
    
    @Test
    public void testReadFile() throws FileNotFoundException, IOException {
        File zipFile = new File(testdata, "archive.zip");
        ZipArchive archive = new ZipArchive(zipFile);

        String read = archive.readFile(new File("test.txt"));
        
        Assert.assertEquals("Hello World!\n", read);
    }
    
    @Test
    public void testReadNotExistingFile() throws IOException {
        File zipFile = new File(testdata, "archive.zip");
        ZipArchive archive = new ZipArchive(zipFile);
        
        try {
            archive.readFile(new File("doesntExist.txt"));
            Assert.fail("Expected exception");
        } catch (FileNotFoundException e) {
            
        }
    }
    
    @Test
    public void testWriteAndDeleteFile() throws IOException {
        File zipFile = new File(testdata, "archive.zip");
        ZipArchive archive = new ZipArchive(zipFile);
        
        File toWrite = new File("testWrite.txt");
        
        Assert.assertFalse(archive.containsFile(toWrite));
        
        String content = "This is a test text\n";
        archive.writeFile(toWrite, content);
        
        Assert.assertTrue(archive.containsFile(toWrite));
        Assert.assertEquals(archive.readFile(toWrite), content);
        
        archive.deleteFile(toWrite);

        Assert.assertFalse(archive.containsFile(toWrite));
    }
    
    @Test
    public void testDeleteNonExissting() throws IOException {
        File zipFile = new File(testdata, "archive.zip");
        ZipArchive archive = new ZipArchive(zipFile);
        
        Assert.assertFalse(archive.containsFile(new File("doesntExist.txt")));
        
        try {
            archive.deleteFile(new File("doesntExist.txt"));
            Assert.fail("Expected exception");
        } catch (FileNotFoundException e) {
            
        }
    }
    
    @Test
    public void testOverwriteFile() throws IOException {
        File zipFile = new File(testdata, "archive.zip");
        ZipArchive archive = new ZipArchive(zipFile);
        
        File toWrite = new File("testOverwrite.txt");
        
        Assert.assertFalse(archive.containsFile(toWrite));
        
        String content1 = "This is a test text\n";
        archive.writeFile(toWrite, content1);
        
        Assert.assertTrue(archive.containsFile(toWrite));
        Assert.assertEquals(archive.readFile(toWrite), content1);
        
        String content2 = "This is another test text\n";
        archive.writeFile(toWrite, content2);
        
        Assert.assertTrue(archive.containsFile(toWrite));
        Assert.assertEquals(archive.readFile(toWrite), content2);
        
        archive.deleteFile(toWrite);

        Assert.assertFalse(archive.containsFile(toWrite));
    }
    
    @Test
    public void testCopyFileToArchive() throws IOException {
        File zipFile = new File(testdata, "archive.zip");
        ZipArchive archive = new ZipArchive(zipFile);
        
        File insideFile = new File("testCopy.txt");
        File outsideFile = new File(testdata, "testfile.txt");
        
        Assert.assertTrue(outsideFile.exists());
        
        Assert.assertFalse(archive.containsFile(insideFile));
        
        archive.copyFileToArchive(insideFile, outsideFile);
        
        Assert.assertTrue(outsideFile.exists());
        Assert.assertTrue(archive.containsFile(insideFile));
        
        Assert.assertEquals(Files.readFile(outsideFile), archive.readFile(insideFile));
        
        archive.deleteFile(insideFile);
        Assert.assertFalse(archive.containsFile(insideFile));
    }
    
    @Test
    public void testExtract() throws IOException {
        File zipFile = new File(testdata, "archive.zip");
        ZipArchive archive = new ZipArchive(zipFile);
        
        File insideFile = new File("test.txt");
        File outsideFile = new File(testdata, "testExtract.txt");
        
        Assert.assertFalse(outsideFile.exists());
        Assert.assertTrue(archive.containsFile(insideFile));
        
        archive.extract(insideFile, outsideFile);

        Assert.assertTrue(outsideFile.exists());
        Assert.assertTrue(archive.containsFile(insideFile));
        
        Assert.assertEquals(archive.readFile(insideFile), Files.readFile(outsideFile));
        
        outsideFile.delete();
        Assert.assertFalse(outsideFile.exists());
    }
    
}
