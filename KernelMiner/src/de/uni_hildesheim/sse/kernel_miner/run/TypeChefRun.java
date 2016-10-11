package de.uni_hildesheim.sse.kernel_miner.run;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import de.uni_hildesheim.sse.kernel_miner.code.TypeChef;
import de.uni_hildesheim.sse.kernel_miner.util.Logger;

/**
 * This class initiates running TypeChef to extract variability information
 * from a complete source code tree. The main Method can be called as an
 * entry point for this application.
 * 
 * @author Adam Krafczyk
 */
public class TypeChefRun extends TypeChefExtractor {

    private Properties config;
    
    /**
     * Creates a {@link TypeChefRun} with the given configuration.
     * 
     * @param configFile The path to the configuration file. See doc/kernelminer.properties
     * @throws IOException If reading the configuration file or initializing the {@link Logger} fails.
     */
    public TypeChefRun(String configFile) throws IOException {
        config = new Properties();
        try {
            config.load(new FileInputStream(new File(configFile)));
        } catch (FileNotFoundException e) {
            System.out.println("Warning: Log file not found; using default values");
        }
        
        String logFile = config.getProperty("logFile", "stdout");
        if (logFile.equals("stdout")) {
            Logger.init();
        } else {
            Logger.init(new FileOutputStream(new File(logFile)));
        }
    }
    
    @Override
    protected TypeChef createTypeChef() {
        TypeChef chef = new TypeChef();
        
        chef.setExe(new File(config.getProperty("typechef.exe")));
        chef.setSourceDir(new File(config.getProperty("typechef.sourceDir")));
        chef.setKconfigModelsBase(new File(config.getProperty("typechef.kconfigModelBase")));
        chef.setSystemRoot(new File(config.getProperty("typechef.systemRoot", "/")));
        chef.setOutput(new File(config.getProperty("typechef.output", "typechef_output.zip")));
        chef.setWorkingDir(new File(config.getProperty("typechef.workingDir", ".")));
        chef.setPartialConfHeader(new File(config.getProperty("typechef.partialConfHeader", "res/typechef/partial_conf.h")));
        chef.setPlatformHeader(new File(config.getProperty("typechef.platformHeader", "res/typechef/platform.h")));
        
        int postIncludeIndex = 0;
        String postIncludeDir;
        while ((postIncludeDir = config.getProperty("typechef.postIncludeDir." + postIncludeIndex)) != null) {
            chef.addPostIncludeDir(new File(postIncludeDir));
            postIncludeIndex++;
        }
        if (postIncludeIndex == 0) {
            chef.addDefaultPostIncludeDirs();
        }
        
        String arch = config.getProperty("typechef.arch");
        if (arch != null) {
            chef.addDefaultLinuxIncludeDirs("x86");
        }
        
        int sourceIncludeIndex = 0;
        String sourceIncludeDir;
        while ((sourceIncludeDir = config.getProperty("typechef.sourceIncludeDir." + sourceIncludeIndex)) != null) {
            chef.addSourceIncludeDir(new File(sourceIncludeDir));
            sourceIncludeIndex++;
        }
        
        return chef;
    }

    @Override
    protected int getNumTypeChefThreads() {
        return Integer.parseInt(config.getProperty("typechef.numTypeChefThreads", "1"));
    }

    @Override
    protected int getNumParserThreads() {
        return Integer.parseInt(config.getProperty("typechef.numParserThreads", "1"));
    }

    @Override
    protected File getPcFile() {
        return new File(config.getProperty("typechef.pcFile"));
    }
    
    /**
     * Starts the application that runs TypeChef.
     * 
     * @param args <code>args[0]</code> Specifies the path to the configuration file.
     *      If not specified, then "kernelminer.properties" is used.
     * @throws IOException If reading the configuration file or initializing the {@link Logger} fails.
     */
    public static void main(String[] args) throws IOException {
        String configFile = "kernelminer.properties";
        if (args.length > 0) {
            configFile = args[0];
        }
        new TypeChefRun(configFile).start();
    }

}
