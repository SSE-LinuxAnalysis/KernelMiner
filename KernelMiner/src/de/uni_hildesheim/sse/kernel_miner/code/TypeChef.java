package de.uni_hildesheim.sse.kernel_miner.code;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.lang.ProcessBuilder.Redirect;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import de.uni_hildesheim.sse.kernel_miner.util.Files;
import de.uni_hildesheim.sse.kernel_miner.util.Logger;
import de.uni_hildesheim.sse.kernel_miner.util.ZipArchive;
import de.uni_hildesheim.sse.kernel_miner.util.logic.Formula;
import de.uni_hildesheim.sse.kernel_miner.util.logic.True;
import de.uni_hildesheim.sse.kernel_miner.util.logic.Variable;
import de.uni_hildesheim.sse.kernel_miner.util.parser.ExpressionFormatException;
import de.uni_hildesheim.sse.kernel_miner.util.parser.Parser;
import de.uni_hildesheim.sse.kernel_miner.util.parser.VariableCache;

/**
 * This class utilizes TypeChef to populate {@link SourceFile}s with {@link Block}s,
 * containing their presence conditions.
 * 
 * @author Adam Krafczyk
 */
public class TypeChef {

    private static final VariableCache PC_CACHE = new VariableCache();
    
    private static final Parser<Formula> PC_PARSER = new Parser<>(new TypeChefPresenceConditionGrammar(PC_CACHE));
    
    private File exe;
    
    private File sourceDir;
    
    private File partialConfHeader;
    
    private File platformHeader;
    
    private File kconfigModelsBase;
    
    private File systemRoot;
    
    private List<File> postIncludeDirs;
    
    private List<File> sourceIncludeDirs;
    
    private ZipArchive output;
    
    private File workingDir;
    
    /**
     * Creates a {@link TypeChef} instance with default parameters that
     * should work when running on an Ubuntu machine.
     * 
     * @param sourceDir The directory of the source code tree.
     * @param kconfigModelsBase The path to the Kconfig models generated by KonfigReader.
     *      Files with this as a basename plus the following suffixes should exist, as
     *      generated by KconfigReader:
     *      <ul>
     *          <li><code>.dimacs</code></li>
     *          <li><code>.features</code></li>
     *          <li><code>.completed.h</code></li>
     *          <li><code>.nonbool.h</code></li>
     *      </ul>
     */
    public TypeChef(File sourceDir, File kconfigModelsBase) {
        setExe(new File("../typechef/typechef.sh"));
        setSourceDir(sourceDir);
        setPartialConfHeader(new File("res/typechef/partial_conf.h"));
        setPlatformHeader(new File("res/typechef/platform.h"));
        setKconfigModelsBase(kconfigModelsBase);
        setSystemRoot(new File("/"));
        
        postIncludeDirs = new ArrayList<>();
        addDefaultPostIncludeDirs();
        
        sourceIncludeDirs = new ArrayList<>();
        
        output = new ZipArchive(new File("typechef_output_" + sourceDir.getName() + ".zip"));
        
        setWorkingDir(new File("."));
    }
    
    /**
     * Sets the path to the TypeChef executable.
     * 
     * @param exe Path to the TypeChef executable.
     */
    public void setExe(File exe) {
        this.exe = exe;
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
     * Sets the path to a header file, containing partial configurations, that
     * will be included at the top of the source file.
     * 
     * @param partialConfHeader The partial configuration header file.
     */
    public void setPartialConfHeader(File partialConfHeader) {
        this.partialConfHeader = partialConfHeader;
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
     * Sets the path to the Kconfig models generated by KonfigReader.
     * Files with this as a basename plus the following suffixes should exist, as
     * generated by KconfigReader:
     * <ul>
     *     <li><code>.dimacs</code></li>
     *     <li><code>.features</code></li>
     *     <li><code>.completed.h</code></li>
     *     <li><code>.nonbool.h</code></li>
     * </ul>
     * 
     * @param kconfigModelsBase The base name for the Kconfig models.
     */
    public void setKconfigModelsBase(File kconfigModelsBase) {
        this.kconfigModelsBase = kconfigModelsBase;
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
        if (!exe.isFile()) {
            throw new IllegalArgumentException("Executable \"" + exe + "\" does not exist");
        }
        if (!exe.canExecute() && !exe.getName().endsWith(".jar")) {
            throw new IllegalArgumentException("Executable \"" + exe + "\" can't be executed");
        }
        
        if (!sourceDir.isDirectory()) {
            throw new IllegalArgumentException("Source directory \"" + sourceDir + "\" is not a directory.");
        }
        if (!sourceDir.canRead()) {
            throw new IllegalArgumentException("Source directory \"" + sourceDir + "\" is not readable.");
        }
        
        if (!partialConfHeader.isFile()) {
            throw new IllegalArgumentException("Partial conf header \"" + partialConfHeader + "\" does not exist");
        }
        if (!partialConfHeader.canRead()) {
            throw new IllegalArgumentException("Partial conf header \"" + partialConfHeader + "\" is not readable");
        }
        
        if (!platformHeader.isFile()) {
            throw new IllegalArgumentException("Platform header \"" + platformHeader + "\" does not exist");
        }
        if (!platformHeader.canRead()) {
            throw new IllegalArgumentException("Platform header \"" + platformHeader + "\" is not readable");
        }
        
        for (String suffix : new String[] { ".dimacs", ".features", ".completed.h", ".nonbool.h" }) {
            File toCheck = new File(kconfigModelsBase.getPath() + suffix); 
            if (!toCheck.isFile()) {
                throw new IllegalArgumentException("Kconfig dir \"" + toCheck + "\" does not exist");
            }
            if (!toCheck.canRead()) {
                throw new IllegalArgumentException("Kconfig dir \"" + toCheck + "\" is not readable");
            }
        }
    }
    
    /**
     * Parses the output of TypeChef and populates the {@link SourceFile} with {@link Block}s
     * filled with presence conditions. {@link #runOnFile(SourceFile)} has to be called
     * beforehand, so the output of TypeChef is present in the output .zip archive.
     * 
     * @param sourceFile The {@link SourceFile} object representing the source file
     *      in the source code tree, where the {@link Block}s will be added to.
     *      
     * @throws IOException If reading the .pi file fails.
     */
    public void parseOutput(SourceFile sourceFile) throws IOException {
        File piFile = new File(sourceFile.getPath().getPath() + ".pi");
        
        BufferedReader in;
        try {
            in = new BufferedReader(new StringReader(output.readFile(piFile)));
        } catch (FileNotFoundException e) {
            Logger.INSTANCE.logWarning("TypeChef did not output a .pi file for " + sourceFile.getPath());
            return;
        }
        
        List<Block> blocks = new LinkedList<>();
        String currentLocation = sourceFile.getPath().getPath();
        
        String line;
        int lineNumber = 0;
        while ((line = in.readLine()) != null) {
            lineNumber++;
            
            if (line.startsWith("#line ")) {
                String[] parts = line.split(" ");
                currentLocation = parts[2].substring(1, parts[2].length() - 1);
                currentLocation = Files.relativize(new File(currentLocation), sourceDir);
                if (!blocks.isEmpty()) {
                    Block lastBlock = blocks.get(blocks.size() - 1);
                    if (!currentLocation.equals(lastBlock.getLocation())) {
                        blocks.add(new Block(new True(), currentLocation, lineNumber));
                    }
                }
                
            } else if (line.startsWith("#ifdef ")) {
                String varName = line.substring(7);
                Formula pc = new Variable(varName);
                blocks.add(new Block(pc, currentLocation, lineNumber));
                
            } else if (line.startsWith("#if ")) {
                String pcStr = line.substring(4);
                
                Formula pc = new True();
                try {
                    pc = PC_PARSER.parse(pcStr);
                } catch (ExpressionFormatException e) {
                    Logger.INSTANCE.logException("Can't parse presence condition " + pcStr, e);
                }
                PC_CACHE.clear();
                
                blocks.add(new Block(pc, currentLocation, lineNumber));
                
            } else if (line.startsWith("#endif")) {
                blocks.add(new Block(new True(), currentLocation, lineNumber));
                
            } else if (line.startsWith("#")) {
                Logger.INSTANCE.logWarning("File " + sourceFile.getPath()
                        + " contains a preprocessor directive that wasn't understood:",
                        line);
                
            } else {
                if (blocks.isEmpty()) {
                    blocks.add(new Block(new True(), currentLocation, lineNumber));
                }
                blocks.get(blocks.size() - 1).addLine(line);
            }
        }
        
        in.close();
        
        // remove empty blocks, i.e. blocks generated between #endif and #if with nothing in between
        for (Iterator<Block> it = blocks.iterator(); it.hasNext();) {
               Block block = it.next();
               if (block.getLines().isEmpty()) {
                   it.remove();
               }
        }
        
        sourceFile.setBlocks(blocks);
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
    private int runTypeChef(SourceFile file, File piOutput, File pcFile, File stdOut, File stdErr) throws IOException, IllegalArgumentException {
        checkParameters();
        
        List<String> command = new ArrayList<>();
        if (exe.getName().endsWith(".jar")) {
            command.add("java");
            command.add("-jar");
            command.add(exe.getAbsolutePath());
        } else {
            command.add(exe.getAbsolutePath());
        }
        command.add("--platfromHeader=" + platformHeader.getAbsolutePath());
        command.add("--systemRoot=" + systemRoot.getAbsolutePath());
        for (File postIncludeDir : postIncludeDirs) {
            command.add("--postIncludes=" + postIncludeDir.getPath());
        }
        command.add("--lex");
        command.add("--prefixonly=CONFIG_");
        command.add("--output=" + piOutput.getAbsolutePath().substring(0, piOutput.getAbsolutePath().length() - 3));
        command.add("--writePI");
        command.add("--no-analysis");
        if (file.getPresenceCondition() != null) {
            try {
                BufferedWriter writer = new BufferedWriter(new FileWriter(pcFile));
                writer.write(file.getPresenceCondition().toString()); // TODO: format
                writer.newLine();
                writer.close();
                
                command.add("--filePC=" + pcFile.getAbsolutePath());
            } catch (IOException e) {
                Logger.INSTANCE.logException("Can't write presence condition file", e);
            }
            
        }
        command.add("--featureModelDimacs=" + new File(kconfigModelsBase.getPath() + ".dimacs").getAbsolutePath());
        command.add("--openFeat=" + new File(kconfigModelsBase.getPath() + ".features").getAbsolutePath());
        command.add("--include=" + new File(kconfigModelsBase.getPath() + ".completed.h").getAbsolutePath());
        command.add("--include=" + new File(kconfigModelsBase.getPath() + ".nonbool.h").getAbsolutePath());
        command.add("--include=" + partialConfHeader.getAbsolutePath());
        for (File sourceIncludeDir : sourceIncludeDirs) {
            command.add("--incdir=" + new File(sourceDir, sourceIncludeDir.getPath()).getAbsolutePath());
        }
        command.add(new File(sourceDir, file.getPath().getPath()).getAbsolutePath());
        
        
        ProcessBuilder builder = new ProcessBuilder(command);
        
        builder.redirectOutput(Redirect.to(stdOut));
        builder.redirectError(Redirect.to(stdErr));
        
        Process chef = builder.start();
        
        int status = -1;
        boolean finished = false;
        while (!finished) {
            try {
                status = chef.waitFor();
                finished = true;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return status;
    }
    
    /**
     * Runs TypeChef on a single {@link SourceFile} in the source code tree.
     * The output will be stored in the output .zip archive to be later used
     * by {@link #parseOutput(SourceFile)}.
     * 
     * @param file The {@link SourceFile} in the source code tree that should be parsed.
     * 
     * @throws IOException If running the TypeChef process or reading its output fails.
     * @throws IllegalArgumentException If any of the parameters in this object are faulty.
     */
    public void runOnFile(SourceFile file) throws IOException, IllegalArgumentException {
        checkParameters();
        
        File piFile = new File(file.getPath().getPath() + ".pi");
        
        if (!output.containsFile(piFile) || output.readFile(piFile).length() == 0) {
            String name = file.getPath().getPath().replace(File.separatorChar, '.') + ".";
            
            File tmpPiOutput = File.createTempFile(name, ".pi", workingDir);
            File tmpPCfile = File.createTempFile(name, ".pc", workingDir);
            File stdOut = File.createTempFile(name, ".stdout", workingDir);
            File stdErr = File.createTempFile(name, ".stderr", workingDir);
            
            int status = runTypeChef(file, tmpPiOutput, tmpPCfile, stdOut, stdErr);
    
            if (tmpPiOutput.isFile()) {
                output.copyFileToArchive(piFile, tmpPiOutput);
                tmpPiOutput.delete();
            }
            if (tmpPCfile.isFile()) {
                output.copyFileToArchive(new File(file.getPath().getPath() + ".pc"), tmpPCfile);
                tmpPCfile.delete();
            }
            if (stdOut.isFile()) {
                output.copyFileToArchive(new File(file.getPath().getPath() + ".stdout"), stdOut);
                stdOut.delete();
            }
            if (stdErr.isFile()) {
                output.copyFileToArchive(new File(file.getPath().getPath() + ".stderr"), stdErr);
                stdErr.delete();
            }
            
            if (status != 0) {
                Logger.INSTANCE.logError("The typechef instance for " + file.getPath() + " exited with status: " + status);
                return;
            }
        } else {
            Logger.INSTANCE.logInfo("Reusing .pi file for file " + file.getPath());
        }
    }
    
}
