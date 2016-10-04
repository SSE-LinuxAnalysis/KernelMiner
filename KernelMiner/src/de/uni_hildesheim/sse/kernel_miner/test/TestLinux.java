package de.uni_hildesheim.sse.kernel_miner.test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import de.uni_hildesheim.sse.kernel_miner.code.TypeChef;
import de.uni_hildesheim.sse.kernel_miner.util.Logger;

public class TestLinux extends LinuxExtractor {

    public static void main(String[] args) {
//      Logger.init();
        try {
            Logger.init(new FileOutputStream(new File("global.log")));
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
        
        LinuxExtractor extractor = new TestLinux();
        extractor.start();
    }

    @Override
    protected TypeChef createTypeChef() {
        File baseDir = new File("/home/adam/typechef_tests");
        
        TypeChef chef = new TypeChef(new File(baseDir, "linux-releases/linux-4.4"),
                new File(baseDir, "kconfig_models/linux-4.4/x86"));
        chef.addDefaultLinuxIncludeDirs("x86");
        
        return chef;
    }

    @Override
    protected int getNumTypeChefThreads() {
        return 6;
    }

    @Override
    protected int getNumParserThreads() {
        return 1;
    }

    @Override
    protected File getPcFile() {
        return new File("/home/adam/typechef_tests/kbuild_pcs/linux-4.4/x86.pcs.txt");
    }
    
}
