package de.uni_hildesheim.sse.kernel_miner.code;

import org.junit.Assert;
import org.junit.Test;

import de.uni_hildesheim.sse.kernel_miner.util.logic.True;

public class BlockTest {

    @Test
    public void testContainsNoCode() {
        Block block = new Block(new True(), "some_header.h", -1);
        
        block.addLine("    ");
        block.addLine("\t");
        block.addLine("\t    ");
        block.addLine("");
        
        Assert.assertFalse(block.containsCode());
    }
    
    @Test
    public void testContainsCode() {
        Block block = new Block(new True(), "some_header.h", -1);
        
        block.addLine("   ");
        block.addLine("\t");
        block.addLine("\t  a  ");
        block.addLine("");
        
        Assert.assertTrue(block.containsCode());
        
    }

}
