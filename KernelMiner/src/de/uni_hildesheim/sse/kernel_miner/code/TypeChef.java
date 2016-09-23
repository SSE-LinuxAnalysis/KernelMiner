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
import java.util.List;

import de.uni_hildesheim.sse.kernel_miner.util.Files;
import de.uni_hildesheim.sse.kernel_miner.util.Logger;
import de.uni_hildesheim.sse.kernel_miner.util.ZipArchive;

/**
 * This class utilizes TypeChef to populate {@link SourceFile}s with {@link Block}s,
 * containing their presence conditions.
 * 
 * @author Adam Krafczyk
 */
public class TypeChef {

//    private static final Parser<Formula> PC_PARSER = new Parser<>(new TypeChefPresenceConditionGrammar());
    
    private File exe;
    
    private File linuxDir;
    
    private String arch;
    
    private File partialConfHeader;
    
    private File platformHeader;
    
    private File kconfigModelsDir;
    
    private List<File> postIncludeDirs;
    
    private List<File> linuxIncludeDirs;
    
    private ZipArchive output;
    
    /**
     * Creates a {@link TypeChef} instance with default parameters (arch x86) that
     * should work on recent Linux versions when running on an Ubuntu machine.
     * 
     * @param linuxDir The directory of the Linux source tree.
     * @param kconfigModelsDir The directory containing the Kconfig models generated by KconfigReader.
     *      It should contain the files <code>&lt;arch&gt;.dimacs</code>, <code>&lt;arch&gt;.features</code>,
     *      <code>&lt;arch&gt;.completed.h</code> and <code>&lt;arch&gt;.nonbool.h</code>.
     */
    public TypeChef(File linuxDir, File kconfigModelsDir) {
        setExe(new File("../typechef/typechef.sh"));
        setLinuxDir(linuxDir);
        setArch("x86");
        setPartialConfHeader(new File("res/typechef/partial_conf.h"));
        setPlatformHeader(new File("res/typechef/platform.h"));
        setKconfigModelsDir(kconfigModelsDir);
        
        postIncludeDirs = new ArrayList<>();
        addDefaultPostIncludeDirs();
        
        linuxIncludeDirs = new ArrayList<>();
        addDefaultLinuxIncludeDirs();
        
        output = new ZipArchive(new File("typechef_output_" + linuxDir.getName() + ".zip"));
    }
    
    /**
     * Sets the path to the TypeChef executable.
     * 
     * @param exe Path to the TypeChef executable.
     */
    private void setExe(File exe) {
        this.exe = exe;
    }
    
    /**
     * Sets the path to the Linux source tree.
     * 
     * @param linuxDir The directory of the Linux source tree.
     */
    public void setLinuxDir(File linuxDir) {
        this.linuxDir = linuxDir;
    }
    
    /**
     * Sets the architecture that should be analyzed.
     * Linux include directories need to be updated after this,
     * by calling {@link #clearLinuxIncludeDirs()} and {@link #addDefaultLinuxIncludeDirs()}.
     * 
     * @param arch The architecture that should be analyzed.
     */
    public void setArch(String arch) {
        this.arch = arch;
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
     * Sets the path to the Kconfig models. It should contain the files
     * <code>&lt;arch&gt;.dimacs</code>, <code>&lt;arch&gt;.features</code>,
     * <code>&lt;arch&gt;.completed.h</code> and <code>&lt;arch&gt;.nonbool.h</code>.
     * 
     * @param kconfigModelsDir The directory containing the Kconfig models (e.g. generated by KconfigReader).
     */
    public void setKconfigModelsDir(File kconfigModelsDir) {
        this.kconfigModelsDir = kconfigModelsDir;
    }
    
    /**
     * Adds a system include directory, where TypeChef will search for header files.
     * 
     * @param postIncludeDir The system include directory.
     */
    public void addPostIncludeDir(File postIncludeDir) {
        postIncludeDirs.add(postIncludeDir);
    }
    
    /**
     * Adds the default list of system include directories. This works on an
     * Ubuntu machine.
     */
    public void addDefaultPostIncludeDirs() {
        addPostIncludeDir(new File("/usr/lib/gcc/x86_64-linux-gnu/5/include"));
        addPostIncludeDir(new File("/usr/include/x86_64-linux-gnu"));
    }
    
    /**
     * Clears the list of system include directories.
     */
    public void clearPostIncludeDirs() {
        postIncludeDirs.clear();
    }
    
    /**
     * Adds an include directory inside the Linux source tree, where TypeChef
     * will search for header files.
     * 
     * @param linuxIncludeDir The include directory, relative to the Linux source tree root.
     */
    public void addLinuxIncludeDir(File linuxIncludeDir) {
        linuxIncludeDirs.add(linuxIncludeDir);
    }
    
    /**
     * Adds the default include directories in the Linux source tree. This depends
     * on the architecture that is set via {@link #setArch(String)}. This should
     * work on recent Linux versions.
     */
    public void addDefaultLinuxIncludeDirs() {
        addLinuxIncludeDir(new File("include"));
        addLinuxIncludeDir(new File("arch/" + arch + "/include"));
        addLinuxIncludeDir(new File("arch/" + arch + "/include/generated"));
        addLinuxIncludeDir(new File("arch/" + arch + "/include/uapi"));
        addLinuxIncludeDir(new File("arch/" + arch + "/include/generated/uapi"));
        addLinuxIncludeDir(new File("arch/" + arch + "/include/asm/mach-default"));
        addLinuxIncludeDir(new File("arch/" + arch + "/include/asm/mach-generic"));
        addLinuxIncludeDir(new File("arch/" + arch + "/include/asm/mach-voyager"));
        addLinuxIncludeDir(new File("include/uapi"));
    }
    
    /**
     * Clears the list of include directories in the Linux source tree.
     */
    public void clearLinuxIncludeDirs() {
        linuxIncludeDirs.clear();
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
     * Checks whether all parameters are set to sane values.
     * 
     * @throws IllegalArgumentException If any of the parameters is not set correctly.
     */
    private void checkParameters() throws IllegalArgumentException {
        if (!exe.isFile()) {
            throw new IllegalArgumentException("Executable \"" + exe + "\" does not exist");
        }
        if (!exe.canExecute()) {
            throw new IllegalArgumentException("Executable \"" + exe + "\" can't be executed");
        }
        
        if (!linuxDir.isDirectory()) {
            throw new IllegalArgumentException("linuxDir \"" + linuxDir + "\" is not a directory.");
        }
        if (!linuxDir.canRead()) {
            throw new IllegalArgumentException("linuxDir \"" + linuxDir + "\" is not readable.");
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
        
        if (!kconfigModelsDir.isDirectory()) {
            throw new IllegalArgumentException("Kconfig dir \"" + kconfigModelsDir + "\" is not a directory");
        }
    }
    
    /**
     * Parses the output of TypeChef and populates the {@link SourceFile} with {@link Block}s
     * filled with presence conditions.
     * 
     * @param piFile The .pi file generated by the lexer of TypeChef.
     * @param sourceFile The {@link SourceFile} object representing the source file
     *      in the Linux tree, where the {@link Block}s will be addded to.
     *      
     * @throws IOException If reading the .pi file fails.
     */
    private void parseOutput(SourceFile sourceFile) throws IOException {
        File piFile = new File(sourceFile.getPath().getPath() + ".pi");
        
        BufferedReader in;
        try {
            in = new BufferedReader(new StringReader(output.readFile(piFile)));
        } catch (FileNotFoundException e) {
            Logger.INSTANCE.logWarning("TypeChef did not output a .pi file for " + sourceFile.getPath());
            return;
        }
        
        Block currentBlock = null;
        String currentLocation = sourceFile.getPath().getPath();
        
        String line;
        int lineNumber = 0;
        while ((line = in.readLine()) != null) {
            lineNumber++;
            
            if (line.startsWith("#line")) {
                String[] parts = line.split(" ");
                currentLocation = parts[2].substring(1, parts[2].length() - 1);
                currentLocation = Files.relativize(new File(currentLocation), linuxDir);
                
            } else if (line.startsWith("#if")) {
                String pcStr = line.substring(4);
                
//                Formula pc = new True();
//                try {
//                    pc = PC_PARSER.parse(pcStr);
//                } catch (ExpressionFormatException e) {
//                    Logger.INSTANCE.logException("Can't parse presence condition " + pcStr, e);
//                }
                
                Block newBlock = new Block(pcStr, currentLocation, lineNumber);
                if (currentBlock != null) {
                    currentBlock.setNext(newBlock);
                }
                currentBlock = newBlock;
                if (sourceFile.getFirstBlock() == null) {
                    sourceFile.setFirstBlock(currentBlock);
                }
                
            } else if (line.startsWith("#endif")) {
                Block newBlock = new Block("", currentLocation, lineNumber);
                currentBlock.setNext(newBlock);
                currentBlock = newBlock;
                
            } else {
                if (currentBlock == null) {
                    currentBlock = new Block("", currentLocation, lineNumber);
                }
                if (sourceFile.getFirstBlock() == null) {
                    sourceFile.setFirstBlock(currentBlock);
                }
                currentBlock.addLine(line);
            }
        }
        
        in.close();
        
        // remove empty blocks, i.e. blocks generated between #endif and #if with nothing in between
        currentBlock = sourceFile.getFirstBlock();
        while (currentBlock != null) {
            
            Block next = currentBlock.getNext();
            while (next != null && next.getLines().size() == 0) {
                next = next.getNext();
            }
            
            currentBlock.setNext(next);
            currentBlock = next;
        }
    }
    
    private int runTypeChef(SourceFile file, File piOutput, File pcFile, File stdOut, File stdErr) throws IOException, IllegalArgumentException {
        checkParameters();
        
        List<String> command = new ArrayList<>();
        command.add(exe.getAbsolutePath());
        command.add("--platfromHeader=" + platformHeader.getAbsolutePath());
        for (File postIncludeDir : postIncludeDirs) {
            command.add("--postIncludes=" + Files.relativize(postIncludeDir, new File("/")));
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
        command.add("--featureModelDimacs=" + new File(kconfigModelsDir, arch + ".dimacs").getAbsolutePath());
        command.add("--openFeat=" + new File(kconfigModelsDir, arch + ".features").getAbsolutePath());
        command.add("--include=" + new File(kconfigModelsDir, arch + ".completed.h").getAbsolutePath());
        command.add("--include=" + new File(kconfigModelsDir, arch + ".nonbool.h").getAbsolutePath());
        command.add("--include=" + partialConfHeader.getAbsolutePath());
        for (File linuxIncludeDir : linuxIncludeDirs) {
            command.add("--incdir=" + new File(linuxDir, linuxIncludeDir.getPath()).getAbsolutePath());
        }
        command.add(new File(linuxDir, file.getPath().getPath()).getAbsolutePath());
        
        
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
     * Runs TypeChef on a single {@link SourceFile} in the Linux source tree.
     * the {@link SourceFile} will be filled with {@link Block}s containing the
     * presence conditions.
     * 
     * @param file The {@link SourceFile} in the Linux source tree.
     * 
     * @throws IOException If running the TypeChef process or reading its output fails.
     * @throws IllegalArgumentException If any of the parameters in this object are faulty.
     */
    public void runOnFile(SourceFile file) throws IOException, IllegalArgumentException {
        checkParameters();
        
        File piFile = new File(file.getPath().getPath() + ".pi");
        
        if (!output.containsFile(piFile)) {
            File tmpPiOutput = File.createTempFile("typechef", ".tmp.pi", new File("."));
            File tmpPCfile = File.createTempFile("typechef", ".tmp.pc", new File("."));
            File stdOut = File.createTempFile("typechef", ".stdout.pc", new File("."));
            File stdErr = File.createTempFile("typechef", ".stderr.pc", new File("."));
            
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
        }
        
        parseOutput(file);
    }
    
}
