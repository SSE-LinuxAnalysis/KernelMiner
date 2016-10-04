package de.uni_hildesheim.sse.kernel_miner.test;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import de.uni_hildesheim.sse.kernel_miner.code.Block;
import de.uni_hildesheim.sse.kernel_miner.code.SourceFile;
import de.uni_hildesheim.sse.kernel_miner.code.TypeChef;
import de.uni_hildesheim.sse.kernel_miner.kbuild.KbuildMiner;
import de.uni_hildesheim.sse.kernel_miner.util.Logger;
import de.uni_hildesheim.sse.kernel_miner.util.ZipArchive;
import de.uni_hildesheim.sse.kernel_miner.util.logic.True;

public class TestWindows {

    private static final int NUM_THREADS = 1;
    
    private static final ConcurrentLinkedQueue<SourceFile> files = new ConcurrentLinkedQueue<>();
    
    private static final File BASE_DIR = new File("C:/localUserFiles/krafczyk/tmp/typechef_windows");
    
    private static void readFileNames() {
        File pcFile = new File(BASE_DIR, "kbuild_pcs/x86.pcs.txt");
        
        Set<File> allowedFiles = new HashSet<>();
        allowedFiles.add(new File("kernel/kallsyms.c"));
        
        Logger.INSTANCE.logInfo("Reading file names from " + pcFile.getAbsolutePath());
        try {
            List<SourceFile> read = KbuildMiner.readOutput(pcFile);
            
            for (SourceFile file : read) {
                if (allowedFiles.contains(file.getPath())) {
                    files.add(file);
                }
            }
        } catch (IOException e) {
            Logger.INSTANCE.logException("Can't read pc file", e);
            System.exit(-1);
        }
        
        Logger.INSTANCE.logInfo("Read " + files.size() + " file locations");
    }
    
    public static void main(String[] args) {
        Logger.init();
//        try {
//            Logger.init(new FileOutputStream(new File("global.log")));
//        } catch (IOException e) {
//            e.printStackTrace();
//            System.exit(-1);
//        }
        
        readFileNames();
        
        Logger.INSTANCE.logInfo("Starting " + NUM_THREADS + " worker threads");
        for (int i = 0; i < NUM_THREADS; i++) {
            Thread th = new Thread(new Worker());
            th.start();
        }
    }
    
    private static void writeCsv(SourceFile file, ZipArchive output) throws IOException {
        StringBuffer content = new StringBuffer();
        
        for (Block block : file.getBlocks()) {
            if (block.containsCode() && !(block.getPresenceCondition() instanceof True)) {
                content.append(file.getPath().getPath()).append(";")
                        .append(block.getPiLineNumber()).append(";")
                        .append(block.getLocation()).append(";")
                        .append(block.getPresenceCondition().toString()) // TODO: format
                        .append("\n");
            }
        }
        
        File filename = new File(file.getPath().getPath() + ".csv");
        output.writeFile(filename, content.toString());
    }
    
    
    private static void runOnFile(SourceFile file) {
        Logger.INSTANCE.logInfo("Starting on file " + file.getPath());
        try {
            TypeChef chef = new TypeChef(new File(BASE_DIR, "linux-4.4"),
                    new File(BASE_DIR, "kconfig_models/x86"));
            
            chef.setExe(new File(BASE_DIR, "TypeChef-0.4.1.jar"));
            chef.setSystemRoot(new File(BASE_DIR, "headers"));
            chef.setOutput(new File(BASE_DIR, "typechef_output.zip"));
            chef.addDefaultLinuxIncludeDirs("x86");
            chef.setWorkingDir(BASE_DIR);
    
            chef.runOnFile(file);
            chef.parseOutput(file);
            
            
            if (!file.getBlocks().isEmpty()) {
                writeCsv(file, chef.getOutput());
                Logger.INSTANCE.logInfo("Finished file " + file.getPath(),
                        files.size() + " files left");
                
            } else {
                Logger.INSTANCE.logWarning(file.getPath() + " does not contain any blocks");
            }
            
            
        } catch (IllegalArgumentException | IOException e) {
            Logger.INSTANCE.logException("Caught exception for file " + file.getPath(), e);
        }
        
    }
    
    private static int numFinishedThreads = 0;
    
    private static synchronized void threadFinished() {
        numFinishedThreads++;
        Logger.INSTANCE.logInfo("Nothing left to do");
        if (numFinishedThreads == NUM_THREADS) {
            Logger.INSTANCE.logInfo("All threads done; exiting");
        }
    }
    
    private static class Worker implements Runnable {
        
        private static int workerCount = 0;
        
        @Override
        public void run() {
            synchronized (Worker.class) {
                workerCount++;
                Thread.currentThread().setName("WorkerThread " + workerCount);
            }
            
            SourceFile file = null;
            while (true) {
                try {
                    file = files.poll();
                    if (file == null) {
                        break;
                    }
                    runOnFile(file);
                } catch (Exception e) {
                    Logger.INSTANCE.logException("Caught exception for file " + file.getPath(), e);
                }
            }
            threadFinished();
        }
        
    }

}
