package de.uni_hildesheim.sse.kernel_miner.run;

import java.io.File;
import java.io.IOException;

import de.uni_hildesheim.sse.kernel_miner.code.SourceFile;
import de.uni_hildesheim.sse.kernel_miner.code.typechef.TypeChef;
import de.uni_hildesheim.sse.kernel_miner.util.Logger;
import de.uni_hildesheim.sse.kernel_miner.util.parser.ExpressionFormatException;

public class Test {

    public static void main(String[] args) throws ExpressionFormatException, IOException {
//        Formula pc = new Parser<>(new CStyleBooleanGrammar(null)).parse(
//                "(CONFIG_MSPRO_BLOCK || CONFIG_MSPRO_BLOCK_MODULE) && (CONFIG_MEMSTICK || CONFIG_MEMSTICK_MODULE)");
//                "CONFIG_KALLSYMS || CONFIG_KALLSYMS_MODULE");
                
        SourceFile file = new SourceFile(new File(
//                "include/linux/jiffies.h"));
//                "include/linux/rcupdate.h"));
//                "drivers/memstick/core/mspro_block.c"));
//                "kernel/kallsyms.c"));
//                "kernel/smpboot.c"));
//                "arch/x86/crypto/crc32-pclmul_glue.c"));
                "drivers/media/dvb-frontends/a8293.c"));
//        file.setPresenceCondition(pc);
        
        TypeChefRun run = new TypeChefRun(
                "res/windows.kernelminer.properties");
//                "kernelminer_tmp.properties");
        TypeChef chef = run.createTypeChef();
        
        chef.runOnFile(file);
        chef.parseTokens(file);
        
        Logger.INSTANCE.logInfo("Done; got " + file.getBlocks().size() + " blocks");
    }
    
}
