package de.uni_hildesheim.sse.kernel_miner.run;

import java.io.File;
import java.io.IOException;

import de.uni_hildesheim.sse.kernel_miner.code.SourceFile;
import de.uni_hildesheim.sse.kernel_miner.code.TypeChef;
import de.uni_hildesheim.sse.kernel_miner.util.parser.ExpressionFormatException;

public class Test {

    public static void main(String[] args) throws ExpressionFormatException, IOException {
//        Formula pc = new Parser<>(new CStyleBooleanGrammar(null)).parse(
//                "(CONFIG_MSPRO_BLOCK || CONFIG_MSPRO_BLOCK_MODULE) && (CONFIG_MEMSTICK || CONFIG_MEMSTICK_MODULE)");
//                "CONFIG_KALLSYMS || CONFIG_KALLSYMS_MODULE");
                
        SourceFile file = new SourceFile(new File(
//                "drivers/memstick/core/mspro_block.c"));
//                "kernel/kallsyms.c"));
                "kernel/smpboot.c"));
//        file.setPresenceCondition(pc);
        
        TypeChefRun run = new TypeChefRun("kernelminer_tmp.properties");
        TypeChef chef = run.createTypeChef();
        
        chef.runOnFile(file);
    }
    
}
