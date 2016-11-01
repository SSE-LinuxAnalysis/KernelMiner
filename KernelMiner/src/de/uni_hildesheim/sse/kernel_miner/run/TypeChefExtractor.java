package de.uni_hildesheim.sse.kernel_miner.run;

import java.io.File;
import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
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

/**
 * This class runs TypeChef on a list of source files and parses the output.
 * This is done using several threads to gain maximum performance.
 * 
 * @author Adam Krafczyk
 */
public abstract class TypeChefExtractor {
    
    private ConcurrentLinkedQueue<SourceFile> typeChefTodo;
    
    private ConcurrentLinkedQueue<SourceFile> parserTodo;
    
    private int parserWorkerCount = 0;
    private int typeChefWorkerCount = 0;
    
    private Object numFinishedTypeChefThreadsLock = new Object();
    private int numFinishedTypeChefThreads = 0;
    
    private TypeChef typeChef;
    
    public TypeChefExtractor() {
    }
    
    /**
     * Starts running TypeChef on the files and parsing their output.
     * This spawns several threads that do the work. This method returns
     * directly after spawning the threads, it does not wait for them to finish their work.
     * <br /><br />
     * The {@link Logger} class should be initialized before calling this.
     */
    public void start()  {
        typeChefTodo = new ConcurrentLinkedQueue<>();
        parserTodo = new ConcurrentLinkedQueue<>();
        typeChef = createTypeChef();
        readFileNames(getPcFile());
        
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
    
    /**
     * Creates a {@link TypeChef} instance. This method will only be called once,
     * and the {@link TypeChef} instance will be reused for all files.
     * 
     * @return The TypeChef instance that will be used to run TypeChef on the source files.
     */
    protected abstract TypeChef createTypeChef();
    
    /**
     * @return How many threads should run {@link TypeChef} in parallel.
     */
    protected abstract int getNumTypeChefThreads();
    
    /**
     * @return How many threads should run parsers for the TypeChef output in parallel.
     */
    protected abstract int getNumParserThreads();
    
    /**
     * If the returned set is non-empty, then only these files are analyzed.
     * 
     * @return The set of filenames, relative to the Linux kernel source tree.
     */
    protected abstract Set<File> getAllowedFiles();
    
    /**
     * @return The file that contains the list of all source files to parse with their presence condition.
     * This will be read by {@link KbuildMiner#readOutput(File)}. This file is usually
     * created by KbuildMiner.
     */
    protected abstract File getPcFile();

    private void readFileNames(File pcFile) {
        Logger.INSTANCE.logInfo("Reading file names from " + pcFile.getAbsolutePath());
        List<SourceFile> read = null;
        try {
            read = KbuildMiner.readOutput(pcFile); 
        } catch (IOException e) {
            Logger.INSTANCE.logException("Can't read pc file", e);
            System.exit(-1);
        }
        
        Set<File> allowedFilenames = getAllowedFiles();
        if (allowedFilenames.isEmpty()) {
            typeChefTodo.addAll(read);
        } else {
            for (SourceFile sourceFile : read) {
                if (allowedFilenames.contains(sourceFile.getPath())) {
                    typeChefTodo.add(sourceFile);
                }
            }
        }
        
        Logger.INSTANCE.logInfo("Read " + typeChefTodo.size() + " file locations");
    }
    
    private class TypeChefWorker implements Runnable {
        
        
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
                    typeChefWorkerCount++;
                    Thread.currentThread().setName("TypeChefWorkerThread " + typeChefWorkerCount);
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
                parserWorkerCount++;
                Thread.currentThread().setName("ParserWorkerThread " + parserWorkerCount);
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
