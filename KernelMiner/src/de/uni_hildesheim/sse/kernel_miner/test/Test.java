package de.uni_hildesheim.sse.kernel_miner.test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import de.uni_hildesheim.sse.kernel_miner.code.Block;
import de.uni_hildesheim.sse.kernel_miner.code.SourceFile;
import de.uni_hildesheim.sse.kernel_miner.code.TypeChef;
import de.uni_hildesheim.sse.kernel_miner.kbuild.KbuildMiner;
import de.uni_hildesheim.sse.kernel_miner.util.Files;
import de.uni_hildesheim.sse.kernel_miner.util.Logger;
import de.uni_hildesheim.sse.kernel_miner.util.ZipArchive;
import de.uni_hildesheim.sse.kernel_miner.util.logic.True;

public class Test {

    private static final int NUM_THREADS = 4;
    
    private static final ConcurrentLinkedQueue<SourceFile> files = new ConcurrentLinkedQueue<>();
    
    private static void readFileNames() {
        File pcFile = new File("/home/adam/typechef_tests/kbuild_pcs/linux-4.4/x86.pcs.txt");
        
        Logger.INSTANCE.logInfo("Reading file names from " + pcFile.getAbsolutePath());
        try {
            List<SourceFile> read = KbuildMiner.readOutput(pcFile); 
            files.addAll(read);
        } catch (IOException e) {
            Logger.INSTANCE.logException("Can't read pc file", e);
            System.exit(-1);
        }
        
        // for debugging purposes, only selected files can be parsed by the following lines:
//        files.clear();
//        files.add(new SourceFile(new File("arch/x86/crypto/aes_glue.c")));
//        files.add(new SourceFile(new File("kernel/kallsyms.c")));
//        files.add(new SourceFile(new File("arch/x86/kernel/crash.c")));
//        files.add(new SourceFile(new File("arch/x86/kernel/fpu/regset.c")));
        
        Logger.INSTANCE.logInfo("Read " + files.size() + " file locations");
    }
    
    public static void main(String[] args) {
//        Logger.init();
        try {
            Logger.init(new FileOutputStream(new File("global.log")));
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
        
        readFileNames();
        
        Logger.INSTANCE.logInfo("Starting " + NUM_THREADS + " worker threads");
        for (int i = 0; i < NUM_THREADS; i++) {
            Thread th = new Thread(new Worker());
            th.start();
        }
    }
    
    private static void deleteDir(File dir) {
        for (File f : dir.listFiles()) {
            if (f.isDirectory()) {
                deleteDir(f);
            } else {
                f.delete();
            }
        }
        dir.delete();
    }
    
    private static void compressDir(File dir) throws IOException {
        List<String> command = new LinkedList<>();
        command.add("tar");
        command.add("-czf");
        command.add(dir.getAbsolutePath() + ".tar.gz");
        for (File f : dir.listFiles()) {
            command.add(Files.relativize(f, new File(dir.getAbsolutePath()).getParentFile()));
        }
        
        ProcessBuilder bldr = new ProcessBuilder(command);
        Process p = bldr.start();
        int status = -1;
        try {
            status = p.waitFor();
        } catch (InterruptedException e) {
        }
        if (status == 0) {
            deleteDir(dir);
        } else {
            Logger.INSTANCE.logError("Can't compress dir " + dir);
        }
    }
    
    private static void writeCsv(SourceFile file, ZipArchive output) throws IOException {
        StringBuffer content = new StringBuffer();
        
        for (Block currentBlock = file.getFirstBlock(); currentBlock != null; currentBlock = currentBlock.getNext()) {
            if (currentBlock.containsCode() && !(currentBlock.getPresenceCondition() instanceof True)) {
                content.append(file.getPath().getPath()).append(";")
                        .append(currentBlock.getPiLineNumber()).append(";")
                        .append(currentBlock.getLocation()).append(";")
                        .append(currentBlock.getPresenceCondition().toString()) // TODO: format
                        .append("\n");
            }
        }
        
        File filename = new File(file.getPath().getPath() + ".csv");
        output.writeFile(filename, content.toString());
    }
    
    private static final File BASE_DIR = new File("/home/adam/typechef_tests");
    
    private static void runOnFile(SourceFile file) {
        Logger.INSTANCE.logInfo("Starting on file " + file.getPath());
        File dir = new File(file.getPath().getPath().replace('/', '.'));
        
        try {
            TypeChef chef = new TypeChef(new File(BASE_DIR, "linux-releases/linux-4.4"),
                    new File(BASE_DIR, "kconfig_models/linux-4.4"));
    
            chef.runOnFile(file);
            
            
            if (file.getFirstBlock() != null) {
                writeCsv(file, chef.getOutput());
                Logger.INSTANCE.logInfo("Finished file " + file.getPath(),
                        files.size() + " files left");
                
            } else {
                Logger.INSTANCE.logWarning(file.getPath() + " does not contain any blocks");
            }
            
            
        } catch (IllegalArgumentException | IOException e) {
            Logger.INSTANCE.logException("Caught exception for file " + file.getPath(), e);
        }
        
        if (dir.isDirectory()) {
            try {
                compressDir(dir);
            } catch (IOException e) {
                Logger.INSTANCE.logException("Caught exception while compressing dir for file " + file.getPath(), e);
            }
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
