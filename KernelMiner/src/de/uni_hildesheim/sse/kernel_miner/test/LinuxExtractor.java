package de.uni_hildesheim.sse.kernel_miner.test;

import java.io.File;
import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import de.uni_hildesheim.sse.kernel_miner.code.Block;
import de.uni_hildesheim.sse.kernel_miner.code.SourceFile;
import de.uni_hildesheim.sse.kernel_miner.code.TypeChef;
import de.uni_hildesheim.sse.kernel_miner.kbuild.KbuildMiner;
import de.uni_hildesheim.sse.kernel_miner.util.Logger;
import de.uni_hildesheim.sse.kernel_miner.util.ZipArchive;
import de.uni_hildesheim.sse.kernel_miner.util.logic.True;

public abstract class LinuxExtractor {
    
    private ConcurrentLinkedQueue<SourceFile> typeChefTodo;
    
    private ConcurrentLinkedQueue<SourceFile> parserTodo;
    
    
    private Object numFinishedTypeChefThreadsLock = new Object();
    private int numFinishedTypeChefThreads = 0;
    
    private TypeChef typeChef;
    
    public LinuxExtractor() {
        typeChefTodo = new ConcurrentLinkedQueue<>();
        parserTodo = new ConcurrentLinkedQueue<>();
        
        typeChef = createTypeChef();
        
        readFileNames(getPcFile());
    }
    
    public void start()  {
        int numChefThreads = getNumTypeChefThreads();
        Logger.INSTANCE.logInfo("Starting " + numChefThreads + " TypeChefWorkerThreads");
        for (int i = 0; i < numChefThreads; i++) {
            Thread th = new Thread(new TypeChefWorker());
            th.setUncaughtExceptionHandler(ExceptionHandler.INSTANCE);
            th.start();
        }
        
        int numParserThreads = getNumParserThreads();
        Logger.INSTANCE.logInfo("Starting " + numParserThreads + " ParserWorkerThreads");
        for (int i = 0; i < numParserThreads; i++) {
            Thread th = new Thread(new ParserWorker());
            th.setUncaughtExceptionHandler(ExceptionHandler.INSTANCE);
            th.start();
        }
    }
    
    protected abstract TypeChef createTypeChef();
    
    protected abstract int getNumTypeChefThreads();
    
    protected abstract int getNumParserThreads();
    
    protected abstract File getPcFile();

    private void readFileNames(File pcFile) {
        Logger.INSTANCE.logInfo("Reading file names from " + pcFile.getAbsolutePath());
        try {
            List<SourceFile> read = KbuildMiner.readOutput(pcFile); 
            typeChefTodo.addAll(read);
        } catch (IOException e) {
            Logger.INSTANCE.logException("Can't read pc file", e);
            System.exit(-1);
        }
        
        // for debugging purposes, only selected files can be parsed by the following lines:
//        typeChefTodo.clear();
//        typeChefTodo.add(new SourceFile(new File("arch/x86/crypto/aes_glue.c")));
//        typeChefTodo.add(new SourceFile(new File("kernel/kallsyms.c")));
//        typeChefTodo.add(new SourceFile(new File("arch/x86/kernel/crash.c")));
//        typeChefTodo.add(new SourceFile(new File("arch/x86/kernel/fpu/regset.c")));
        
        Logger.INSTANCE.logInfo("Read " + typeChefTodo.size() + " file locations");
    }
    
    private class TypeChefWorker implements Runnable {
        
        private int workerCount = 0;
        
        private void runTypeChef(SourceFile file) {
            Logger.INSTANCE.logInfo("Running TypeChef on file " + file.getPath());
            try {
                typeChef.runOnFile(file);
                
                Logger.INSTANCE.logInfo("Finished TypeChef on file " + file.getPath(),
                        typeChefTodo.size() + " files left");
                
            } catch (IllegalArgumentException | IOException e) {
                Logger.INSTANCE.logException("Caught exception while running TypeChef for file " + file.getPath(), e);
            }
        }
        
        @Override
        public void run() {
            try {
                synchronized (TypeChefWorker.class) {
                    workerCount++;
                    Thread.currentThread().setName("TypeChefWorkerThread " + workerCount);
                }
                
                SourceFile file = null;
                while (true) {
                    try {
                        file = typeChefTodo.poll();
                        if (file == null) {
                            break;
                        }
                        runTypeChef(file);
                        parserTodo.add(file);
                        
                    } catch (Exception e) {
                        Logger.INSTANCE.logException("Caught exception while running TypeChef for file "
                                + file.getPath(), e);
                    }
                }
    
                Logger.INSTANCE.logInfo("Nothing left to do");
            } finally {
                synchronized (numFinishedTypeChefThreadsLock) {
                    numFinishedTypeChefThreads++;
                }
            }
        }
        
    }
    
    private class ParserWorker implements Runnable {
        
        private int workerCount = 0;
        
        private void parseFile(SourceFile file) {
            Logger.INSTANCE.logInfo("Parsing file " + file.getPath());
            try {
                typeChef.parseOutput(file);
                
                if (!file.getBlocks().isEmpty()) {
                    writeCsv(file, typeChef.getOutput());
                    Logger.INSTANCE.logInfo("Finished parsing file " + file.getPath(),
                            parserTodo.size() + " files in parsing queue");
                    
                } else {
                    Logger.INSTANCE.logWarning(file.getPath() + " does not contain any blocks");
                }
                
            } catch (IOException e) {
                Logger.INSTANCE.logException("Caught exception while parsing file " + file.getPath(), e);
            }
        }
        
        private void writeCsv(SourceFile file, ZipArchive output) throws IOException {
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
        
        @Override
        public void run() {
            synchronized (ParserWorker.class) {
                workerCount++;
                Thread.currentThread().setName("ParserWorkerThread " + workerCount);
            }
            
            SourceFile file = null;
            
            while (true) {
                try {
                    file = parserTodo.poll();
                    if (file == null) {
                        synchronized (numFinishedTypeChefThreadsLock) {
                            if (numFinishedTypeChefThreads >= getNumTypeChefThreads()) {
                                break;
                            }
                        }
                        
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                        }
                        
                        continue;
                    }
                    
                    parseFile(file);
                } catch (Exception e) {
                    Logger.INSTANCE.logException("Caught exception while parsing file " + file.getPath(), e);
                }
            }
            
            Logger.INSTANCE.logInfo("Nothing left to do");
        }
        
    }
    
    private static class ExceptionHandler implements UncaughtExceptionHandler {

        public static final ExceptionHandler INSTANCE = new ExceptionHandler();
        
        @Override
        public void uncaughtException(Thread t, Throwable e) {
            Logger.INSTANCE.logException("Uncaught exception in thread " + t.getName(), e);
        }
        
    }
    
}
