package de.uni_hildesheim.sse.kernel_miner.code.typechef;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.EOFException;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.ProcessBuilder.Redirect;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import de.uni_hildesheim.sse.kernel_miner.code.Block;
import de.uni_hildesheim.sse.kernel_miner.code.CToken;
import de.uni_hildesheim.sse.kernel_miner.code.SourceFile;
import de.uni_hildesheim.sse.kernel_miner.util.Files;
import de.uni_hildesheim.sse.kernel_miner.util.Logger;
import de.uni_hildesheim.sse.kernel_miner.util.ZipArchive;
import de.uni_hildesheim.sse.kernel_miner.util.logic.Formula;
import de.uni_hildesheim.sse.kernel_miner.util.logic.True;
import de.uni_hildesheim.sse.kernel_miner.util.solver.SatSolver;
import de.uni_hildesheim.sse.kernel_miner.util.solver.SolverException;

/**
 * This class utilizes TypeChef to populate {@link SourceFile}s with {@link Block}s,
 * containing their presence conditions.
 * 
 * @author Adam Krafczyk
 */
public class TypeChef {
    
    private File sourceDir;
    
    private File platformHeader;
    
    private File openVariablesFile;
    
    private File systemRoot;
    
    private List<File> staticIncludes;
    
    private List<File> postIncludeDirs;
    
    private List<File> sourceIncludeDirs;
    
    private List<String> preprocessorDefines;
    
    private File kbuildParamFile;
    
    private File dimacsModel;
    
    private ZipArchive output;
    
    private File workingDir;
    
    /**
     * Creates a {@link TypeChef} instance with no parameters set. The appropriate
     * set* and add* methods must be called before this can be used.
     */
    public TypeChef() {
        staticIncludes = new ArrayList<>();
        postIncludeDirs = new ArrayList<>();
        sourceIncludeDirs = new ArrayList<>();
        preprocessorDefines = new ArrayList<>();
    }
    
    /**
     * Sets the path to the source code tree.
     * 
     * @param sourceDir The directory of the source code tree.
     */
    public void setSourceDir(File sourceDir) {
        this.sourceDir = sourceDir;
    }

    /**
     * Sets the path to the platform header file, containing the symbols defined
     * by the compiler, that will be included at the top of the source file.
     * 
     * @param platformHeader The platform header file.
     */
    public void setPlatformHeader(File platformHeader) {
        this.platformHeader = platformHeader;
    }
    
    
    /**
     * Sets the path to a file listing all (open, i.e. not permanently true or false)
     * Kconfig variables. This file is usually generated by KbuildMiner.
     * This is optional, but using this file helps reduce the complexity of the
     * presence conditions, because variables that are always true or always
     * false can be filtered out. If this file is not provided, then all CONFIG_
     * variables are treated as variability.
     * 
     * @param openVariablesFile The file containing a list of variable names
     *      (with CONFIG_ prefix, one name per line).
     */
    public void setOpenVariablesFile(File openVariablesFile) {
        this.openVariablesFile = openVariablesFile;
    }
    
    /**
     * Sets the system root root directory. System include directories are
     * interpreted to be relative to this. There should at least be a
     * <code>usr/include/</code> folder in this system root. Most Linux machines
     * probably want to use simply <code>/</code> here.
     * 
     * @param systemRoot The system root directory, where all system include directories are located.
     */
    public void setSystemRoot(File systemRoot) {
        this.systemRoot = systemRoot;
    }
    
    /**
     * Adds a header file that is always included at the top of source files.
     * This can be used for overriding headers (by defining their include guard),
     * or adding other definitions, that should always be present while parsing.
     * 
     * @param staticInclude The header file that is statically included, before
     *      anything else in the source file.
     */
    public void addStaticInclude(File staticInclude) {
        staticIncludes.add(staticInclude);
    }
    
    /**
     * Clears the list of system include directories.
     */
    public void clearStaticInclude() {
        staticIncludes.clear();
    }
    
    /**
     * Adds a system include directory, where TypeChef will search for header files.
     * 
     * @param postIncludeDir The system include directory, relative to the system root.
     */
    public void addPostIncludeDir(File postIncludeDir) {
        postIncludeDirs.add(postIncludeDir);
    }
    
    /**
     * Adds the default list of system include directories. This works for the
     * headers of an Ubuntu machine.
     */
    public void addDefaultPostIncludeDirs() {
        addPostIncludeDir(new File("usr/lib/gcc/x86_64-linux-gnu/5/include"));
        addPostIncludeDir(new File("usr/include/x86_64-linux-gnu"));
    }
    
    /**
     * Clears the list of system include directories.
     */
    public void clearPostIncludeDirs() {
        postIncludeDirs.clear();
    }
    
    /**
     * Adds an include directory inside the source code tree, where TypeChef
     * will search for header files.
     * 
     * @param sourceIncludeDir The include directory, relative to the source code tree root.
     */
    public void addSourceIncludeDir(File sourceIncludeDir) {
        sourceIncludeDirs.add(sourceIncludeDir);
    }
    
    /**
     * Adds the default include directories for a Linux source tree. This should
     * work on recent Linux versions.
     * 
     * @param arch The architecture to set the include directories for.
     */
    public void addDefaultLinuxIncludeDirs(String arch) {
        addSourceIncludeDir(new File("include"));
        addSourceIncludeDir(new File("arch/" + arch + "/include"));
        addSourceIncludeDir(new File("arch/" + arch + "/include/generated"));
        addSourceIncludeDir(new File("arch/" + arch + "/include/uapi"));
        addSourceIncludeDir(new File("arch/" + arch + "/include/generated/uapi"));
        addSourceIncludeDir(new File("arch/" + arch + "/include/asm/mach-default"));
        addSourceIncludeDir(new File("arch/" + arch + "/include/asm/mach-generic"));
        addSourceIncludeDir(new File("arch/" + arch + "/include/asm/mach-voyager"));
        addSourceIncludeDir(new File("include/uapi"));
    }
    
    /**
     * Clears the list of include directories in the source code tree.
     */
    public void clearSourceIncludeDirs() {
        sourceIncludeDirs.clear();
    }
    
    public void addPreprocessorDefine(String symbol) {
        preprocessorDefines.add(symbol);
    }
    
    public void clearPreprocessorDefines() {
        preprocessorDefines.clear();
    }
    
    public void setKbuildParamFile(File kbuilbParamFile) {
        this.kbuildParamFile = kbuilbParamFile;
    }
    
    public void setDimacsModel(File dimacsModel) {
        this.dimacsModel = dimacsModel;
    }
    
    /**
     * Sets the output destination. A zip file with the given name will be created.
     * If the archive already exists, then the .pi files within will be reused and
     * new ones will be added to the archive.
     * 
     * @param output The name of the zip file to store output in. Should end with ".zip".
     */
    public void setOutput(File output) {
        this.output = new ZipArchive(output);
    }
    
    /**
     * @return The {@link ZipArchive} where the output is stored.
     */
    public ZipArchive getOutput() {
        return output;
    }
    
    /**
     * @param workingDir The working directory where temporary files are stored
     *      while running TypeChef.
     */
    public void setWorkingDir(File workingDir) {
        this.workingDir = workingDir;
    }
    
    /**
     * Checks whether all parameters are set to sane values.
     * 
     * @throws IllegalArgumentException If any of the parameters is not set correctly.
     */
    private void checkParameters() throws IllegalArgumentException {
        if (sourceDir == null || !sourceDir.isDirectory()) {
            throw new IllegalArgumentException("Source directory \"" + sourceDir + "\" is not a directory.");
        }
        if (!sourceDir.canRead()) {
            throw new IllegalArgumentException("Source directory \"" + sourceDir + "\" is not readable.");
        }
        
        if (platformHeader == null || !platformHeader.isFile()) {
            throw new IllegalArgumentException("Platform header \"" + platformHeader + "\" does not exist");
        }
        if (!platformHeader.canRead()) {
            throw new IllegalArgumentException("Platform header \"" + platformHeader + "\" is not readable");
        }
        
        if (openVariablesFile != null) {
            if (!openVariablesFile.isFile()) {
                throw new IllegalArgumentException("openFeatures file not specified");
            }
            if (!openVariablesFile.canRead()) {
                throw new IllegalArgumentException("openFeatures file can not be read");
            }
        } else {
            Logger.INSTANCE.logWarning("No openVariablesFile specified");
        }
        
        if (systemRoot == null || !systemRoot.isDirectory()) {
            throw new IllegalArgumentException("System root \"" + systemRoot + "\" is not a directory.");
        }
        
        if (staticIncludes.isEmpty()) {
            Logger.INSTANCE.logWarning("No static includes defined");
        }
        for (File staticInclude : staticIncludes) {
            if (staticInclude == null || !staticInclude.isFile()) {
                throw new IllegalArgumentException("Static include header \"" + staticIncludes + "\" does not exist");
            }
            if (!staticInclude.canRead()) {
                throw new IllegalArgumentException("Static include header \"" + staticIncludes + "\" is not readable");
            }
        }
        
        if (postIncludeDirs.isEmpty()) {
            Logger.INSTANCE.logWarning("No post include directories specified");
        }
        for (File postIncludeDir : postIncludeDirs) {
            postIncludeDir = new File(systemRoot, postIncludeDir.getPath());
            if (!postIncludeDir.isDirectory()) {
                Logger.INSTANCE.logWarning("Post include directory \"" + postIncludeDir + "\" is not a directory");
            }
        }
        
        if (sourceIncludeDirs.isEmpty()) {
            Logger.INSTANCE.logWarning("No source include directories specified");
        }
        for (File sourceIncludeDir : sourceIncludeDirs) {
            sourceIncludeDir = new File(sourceDir, sourceIncludeDir.getPath());
            if (!sourceIncludeDir.isDirectory()) {
//                Logger.INSTANCE.logWarning("Source include directory \"" + sourceIncludeDir + "\" is not a directory");
            }
        }
        
        // TODO: preprocessorDefines
        
        if (kbuildParamFile != null) {
            if (!kbuildParamFile.isFile()) {
                throw new IllegalArgumentException("kbuildParamFile \"" + kbuildParamFile + "\" does not exist");
            }
            if (!kbuildParamFile.canRead()) {
                throw new IllegalArgumentException("kbuildParamFile \"" + kbuildParamFile + "\" is not readable");
            }
        } else {
            Logger.INSTANCE.logWarning("No kbuildParamFile specified");
        }
        
        if (dimacsModel != null) {
            if (!dimacsModel.isFile()) {
                throw new IllegalArgumentException("dimacsModel \"" + dimacsModel + "\" does not exist");
            }
            if (!dimacsModel.canRead()) {
                throw new IllegalArgumentException("dimacsModel \"" + dimacsModel + "\" is not readable");
            }
        } else {
            Logger.INSTANCE.logWarning("No DIMACS model specified");
        }
        
        if (output == null) {
            throw new IllegalArgumentException("Output archive not specified.");
        }
        
        if (workingDir == null || !workingDir.isDirectory()) {
            throw new IllegalArgumentException("Working directory \"" + workingDir + "\" is not a directory.");
        }
        if (!workingDir.canWrite()) {
            throw new IllegalArgumentException("Source directory \"" + sourceDir + "\" is not writable.");
        }
    }
    
    public void parseTokens(SourceFile sourcefile) {
        List<CToken> tokens = sourcefile.getTokens();
        
        if (tokens == null) {
            Logger.INSTANCE.logWarning("No tokens for file " + sourcefile.getPath().getPath());
            return;
        }
        
        List<Block> blocks = new LinkedList<>();
        Formula previous = null;
        for (CToken token : tokens) {
            Formula pc = token.getPc();
            String filename = "unkown";
            if (token.getSourceName() != null) {
                filename = token.getSourceName().replace("file ", "");
            }
            filename = Files.relativize(new File(filename), sourceDir);

            boolean appendToPrevious = previous != null && blocks.size() > 0;
            if (appendToPrevious) {
                Block previousBlock = blocks.get(blocks.size() - 1);
                appendToPrevious = filename.equals(previousBlock.getLocation());
            }
            if (appendToPrevious) {
                appendToPrevious = pc.equals(previous);
            }
            
            
            if (appendToPrevious) {
                blocks.get(blocks.size() - 1).addLine(token.getText());
                
            } else {
                blocks.add(new Block(pc, filename, token.getLine()));
                previous = pc;
            }
        }
        
        sourcefile.setTokens(null); // TODO: maybe remove in future?
        
        sourcefile.setBlocks(blocks);
    }
    
    private List<String> buildParameters(SourceFile file, File piOutput, File pcFile) {
        List<String> params = new ArrayList<>();
        
        // environment stuff
        params.add("--platfromHeader=" + platformHeader.getAbsolutePath());
        params.add("--systemRoot=" + systemRoot.getAbsolutePath());
        
        // typechef behavior
        params.add("--lex");
        params.add("--lexNoStdout");
        params.add("--no-analysis");
        

        // Kconfig variables
        params.add("--prefixonly=CONFIG_");
        if (openVariablesFile != null) {
            params.add("--openFeat=" + openVariablesFile.getAbsolutePath());
        }
        
        // output
        params.add("--output=" + piOutput.getAbsolutePath().substring(0, piOutput.getAbsolutePath().length() - 3));
        params.add("--lexOutput=" + piOutput.getAbsolutePath());
        
        // presence condition of file TODO: is this even used?
        if (file.getPresenceCondition() != null) {
            try {
                BufferedWriter writer = new BufferedWriter(new FileWriter(pcFile));
                writer.write(file.getPresenceCondition().toString()); // TODO: format
                writer.newLine();
                writer.close();
                
                params.add("--filePC=" + pcFile.getAbsolutePath());
            } catch (IOException e) {
                Logger.INSTANCE.logException("Can't write presence condition file", e);
            }
            
        }
        
        // include stuff
        for (File staticInclude : staticIncludes) {
            params.add("--include=" + staticInclude.getAbsolutePath());
        }
        for (File sourceIncludeDir : sourceIncludeDirs) {
            params.add("--incdir=" + new File(sourceDir, sourceIncludeDir.getPath()).getAbsolutePath());
        }
        for (File postIncludeDir : postIncludeDirs) {
            params.add("--postIncludes=" + postIncludeDir.getPath());
        }
        
        // preprocessor definitions
        for (String symbol : preprocessorDefines) {
            params.add("-D" + symbol);
        }
        
        // kbuildParamFile
        if (kbuildParamFile != null) {
            BufferedReader in = null;
            try {
                in = new BufferedReader(new FileReader(kbuildParamFile));
                String line;
                while ((line = in.readLine()) != null) {
                    String[] parts = line.split(": ");
                    if (file.getPath().getPath().startsWith(parts[0])) {
                        for (String flag : parts[1].split(" ")) {
                            flag = flag.replace("$srcPath", sourceDir.getAbsolutePath());
                            params.add(flag);
                        }
                    }
                }
            } catch (IOException e) {
                Logger.INSTANCE.logException("Exception while reading kbuildparam file", e);
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        Logger.INSTANCE.logException("Exception while closing kbuildparam file", e);
                    }
                }
            }
        }
        
        // the source file to parse
        params.add(new File(sourceDir, file.getPath().getPath()).getAbsolutePath());
        
        return params;
    }
    
    /**
     * Runs a TypeChef process with the current configuration and the given output files.
     * 
     * @param file The source file in the source code tree to run TypeChef on.
     * @param piOutput The .pi file where TypeChef should write it's primary lexer output.
     * @param pcFile The .pc file that should contain the presence condition of the given {@link SourceFile}.
     * @param stdOut The file that should contain the standard output of TypeChef.
     * @param stdErr The file that should contain the error output of TypeChef.
     * @return The process' exit status.
     * 
     * @throws IOException If running the process fails.
     * @throws IllegalArgumentException If any of the parameters of the current configuration is not set correctly.
     */
    private int runTypeChef(final SourceFile file, File piOutput, File pcFile) throws IOException, IllegalArgumentException {
        final List<String> params = buildParameters(file, piOutput, pcFile);
        
        final ServerSocket serSock = new ServerSocket(0);
        final List<String> errors = new LinkedList<>();
        
        Thread comm = new Thread("Comm of " + Thread.currentThread().getName()) {
            
            @SuppressWarnings("unchecked")
            public void run() {
                try {
                    Socket socket = serSock.accept();
                    
                    ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                    ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
                    
                    out.writeObject(params);
                    
                    Object tokenList = in.readObject();
                    if (tokenList != null) {
                        file.setTokens((List<CToken>) tokenList);
                    }
                    
                    Object errorList = in.readObject();
                    if (errorList != null) {
                        errors.addAll((List<String>) errorList);
                    }
                    
                    serSock.close();
                } catch (EOFException e) {
                    Logger.INSTANCE.logWarning("TypeChefRunner exited without sending a result");
                } catch (ClassNotFoundException | IOException e) {
                    Logger.INSTANCE.logException("Exception", e);
                }
            };
            
        };
        comm.start();
        
        ProcessBuilder builder = new ProcessBuilder("java",
                "-Xmx20g",
                "-cp", System.getProperty("java.class.path"),
                TypeChefRunner.class.getName(),
                serSock.getLocalPort() + "");
        builder.redirectError(Redirect.INHERIT);
        builder.redirectOutput(Redirect.INHERIT);
        Process process = builder.start();
        
        try {
            process.waitFor();
            comm.join();
        } catch (InterruptedException e) {
            Logger.INSTANCE.logException("Exception while waiting", e);
        }
        
        if (!errors.isEmpty()) {
            String[] errorStr = new String[errors.size() + 1];
            errorStr[0] = "Lexer errors:";
            for (int i = 1; i < errorStr.length; i++) {
                errorStr[i] = errors.get(i - 1);
            }
            Logger.INSTANCE.logInfo(errorStr);
        }
        
        return file.getTokens() != null ? 0 : -1;
    }
    
    /**
     * Runs TypeChef on a single {@link SourceFile} in the source code tree.
     * The output will be stored in the output .zip archive to be later used
     * by {@link #parseOutput(SourceFile)}.
     * 
     * @param file The {@link SourceFile} in the source code tree that should be parsed.
     * 
     * @return <code>true</code> if file was actually parsed, false if result is already present.
     * 
     * @throws IOException If running the TypeChef process or reading its output fails.
     * @throws IllegalArgumentException If any of the parameters in this object are faulty.
     */
    public boolean runOnFile(SourceFile file) throws IOException, IllegalArgumentException {
        checkParameters();
        
        // check if we already got output
        File csvFile = new File(file.getPath().getPath() + ".csv");
        if (output.containsFile(csvFile) && output.getSize(csvFile) > 0) {
            Logger.INSTANCE.logInfo("Skipping " + file.getPath() + " because a .csv file is already present");
            return false;
        }
        
        // check if file PC is even satisfiable
        if (dimacsModel != null && file.getPresenceCondition() != null
                && !(file.getPresenceCondition() instanceof True)) {
            try {
                SatSolver solver = new SatSolver(dimacsModel);
                
                if (!solver.isSatisfiable(file.getPresenceCondition(), false)) {
                    Logger.INSTANCE.logInfo("Skipping " + file.getPath() + " because it's PC is not satisfiable:",
                            file.getPresenceCondition().toString());
                    return false;
                }
                
            } catch (SolverException e) {
                Logger.INSTANCE.logException("Failed to solve file PC; running anyways", e);
            }
        }
        
        File piFile = new File(file.getPath().getPath() + ".pi");
        
        String name = file.getPath().getPath().replace(File.separatorChar, '.') + ".";
        
        File tmpPiOutput = File.createTempFile(name, ".pi", workingDir);
        File tmpPCfile = File.createTempFile(name, ".pc", workingDir);
        
        int status = runTypeChef(file, tmpPiOutput, tmpPCfile);

        if (tmpPiOutput.isFile()) {
            output.copyFileToArchive(piFile, tmpPiOutput);
            tmpPiOutput.delete();
        }
        if (tmpPCfile.isFile()) {
            output.copyFileToArchive(new File(file.getPath().getPath() + ".pc"), tmpPCfile);
            tmpPCfile.delete();
        }
        
        if (status != 0) {
            Logger.INSTANCE.logError("The typechef instance for " + file.getPath() + " exited with status: " + status);
        }
        return true;
    }
    
}
