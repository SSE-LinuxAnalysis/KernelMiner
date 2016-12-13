package de.uni_hildesheim.sse.kernel_miner.kbuild;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import de.uni_hildesheim.sse.kernel_miner.code.SourceFile;
import de.uni_hildesheim.sse.kernel_miner.kbuild.KbuildMiner;
import de.uni_hildesheim.sse.kernel_miner.util.Logger;


public class KbuildMinerTest {
    
    private static final File TESTDATA = new File("testdata/KbuildMinerTest");
    
    @Test
    public void testReadOutput() throws IOException {
        ByteArrayOutputStream log = new ByteArrayOutputStream();
        Logger.init(log);
        
        File pcs = new File(TESTDATA, "pcs.txt");
        
        List<SourceFile> files = KbuildMiner.readOutput(pcs);
        Assert.assertEquals(15952, files.size());
        
        String output = log.toString();
        Assert.assertEquals("", output);
        
        for (SourceFile file : files) {
            Assert.assertNotNull(file.getPresenceCondition());
        }
        
        // ((CRYPTO_AES_586 == "y") || (CRYPTO_AES_586 == "m"))
        Assert.assertEquals("(CONFIG_CRYPTO_AES_586 || CONFIG_CRYPTO_AES_586_MODULE)",
                files.get(0).getPresenceCondition().toString());
        
        // ((64BIT == "y") && ((CRYPTO_AES_NI_INTEL == "y") || (CRYPTO_AES_NI_INTEL == "m")))
        Assert.assertEquals("(CONFIG_64BIT && (CONFIG_CRYPTO_AES_NI_INTEL || CONFIG_CRYPTO_AES_NI_INTEL_MODULE))",
                files.get(2).getPresenceCondition().toString());
        
        // ((X86_CMPXCHG64 != "y") && (X86_32 == "y"))
        Assert.assertEquals("(!CONFIG_X86_CMPXCHG64 && CONFIG_X86_32)",
                files.get(290).getPresenceCondition().toString());
        
        // !(X86_32 == "y")
        Assert.assertEquals("!CONFIG_X86_32",
                files.get(294).getPresenceCondition().toString());
        
        // [TRUE]
        Assert.assertEquals("1",
                files.get(72).getPresenceCondition().toString());
    }
    
    @Test
    public void testReadInvalidPcs() throws IOException {
        ByteArrayOutputStream log = new ByteArrayOutputStream();
        Logger.init(log);
        
        File pcs = new File(TESTDATA, "invalid_pcs.txt");
        
        List<SourceFile> files = KbuildMiner.readOutput(pcs);
        Assert.assertEquals(8, files.size());
        
        String[] lines = log.toString().split("\n");
        Assert.assertEquals(8, lines.length);
        
        for (int i = 0; i < lines.length; i++) {
            Assert.assertTrue(lines[i].startsWith("[warning]"));
            Assert.assertTrue(lines[i].contains("Presence condition for file "));
            Assert.assertTrue(lines[i].endsWith(" is invalid"));
        }
        
        for (SourceFile file : files) {
            Assert.assertNull(file.getPresenceCondition());
        }
    }

}
