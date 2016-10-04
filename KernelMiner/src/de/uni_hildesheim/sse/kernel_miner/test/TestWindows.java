package de.uni_hildesheim.sse.kernel_miner.test;

import java.io.File;

import de.uni_hildesheim.sse.kernel_miner.code.TypeChef;
import de.uni_hildesheim.sse.kernel_miner.util.Logger;

public class TestWindows extends LinuxExtractor {
    
    private static final File BASE_DIR = new File("C:/localUserFiles/krafczyk/tmp/typechef_windows");
    
    public static void main(String[] args) {
      Logger.init();
        
        LinuxExtractor extractor = new TestWindows();
        extractor.start();
    }

    @Override
    protected TypeChef createTypeChef() {
        TypeChef chef = new TypeChef(new File(BASE_DIR, "linux-4.4"),
                new File(BASE_DIR, "kconfig_models/x86"));
        
        chef.setExe(new File(BASE_DIR, "TypeChef-0.4.1.jar"));
        chef.setSystemRoot(new File(BASE_DIR, "headers"));
        chef.setOutput(new File(BASE_DIR, "typechef_output.zip"));
        chef.addDefaultLinuxIncludeDirs("x86");
        chef.setWorkingDir(BASE_DIR);
        
        return chef;
    }

    @Override
    protected int getNumTypeChefThreads() {
        return 1;
    }

    @Override
    protected int getNumParserThreads() {
        return 1;
    }

    @Override
    protected File getPcFile() {
        return new File(BASE_DIR, "kbuild_pcs/x86.pcs.txt");
    }

}
