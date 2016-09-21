package de.uni_hildesheim.sse.kernel_miner.code;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.util.ArrayList;
import java.util.List;

import de.uni_hildesheim.sse.kernel_miner.util.Logger;

public class TypeChef {

    private File exe;
    
    private File linuxDir;
    
    private String arch;
    
    private File partialConfHeader;
    
    private File platformHeader;
    
    private File kconfigModelsDir;
    
    private List<File> postIncludeDirs;
    
    private List<File> linuxIncludeDirs;
    
    public TypeChef(File linuxDir, String arch, File kconfigModelsDir) throws IllegalArgumentException {
        setExe(new File("../typechef/typechef.sh"));
        setLinuxDir(linuxDir);
        setArch(arch);
        setPartialConfHeader(new File("res/typechef/partial_conf.h"));
        setPlatformHeader(new File("res/typechef/platform.h"));
        setKconfigModelsDir(kconfigModelsDir);
        
        postIncludeDirs = new ArrayList<>();
        addDefaultPostIncludeDirs();
        
        linuxIncludeDirs = new ArrayList<>();
        addDefaultLinuxIncludeDirs();
    }
    
    private void setExe(File exe) throws IllegalArgumentException {
        if (!exe.isFile()) {
            throw new IllegalArgumentException("Executable \"" + exe + "\" does not exist");
        }
        if (!exe.canExecute()) {
            throw new IllegalArgumentException("Executable \"" + exe + "\" can't be executed");
        }
        this.exe = exe;
    }
    
    public void setLinuxDir(File linuxDir) throws IllegalArgumentException {
        if (!linuxDir.isDirectory()) {
            throw new IllegalArgumentException("linuxDir \"" + linuxDir + "\" is not a directory.");
        }
        this.linuxDir = linuxDir;
    }
    
    /**
     * Linux include directories need to be updated after this, by calling {@link #addDefaultLinuxIncludeDirs()}.
     * 
     * @param arch
     */
    public void setArch(String arch) {
        this.arch = arch;
    }
    
    public void setPartialConfHeader(File partialConfHeader) throws IllegalArgumentException {
        if (!partialConfHeader.isFile()) {
            throw new IllegalArgumentException("Partial conf header \"" + partialConfHeader + "\" does not exist");
        }
        if (!partialConfHeader.canRead()) {
            throw new IllegalArgumentException("Partial conf header \"" + partialConfHeader + "\" is not readable");
        }
        this.partialConfHeader = partialConfHeader;
    }
    
    public void setPlatformHeader(File platformHeader) throws IllegalArgumentException {
        if (!platformHeader.isFile()) {
            throw new IllegalArgumentException("Platform header \"" + platformHeader + "\" does not exist");
        }
        if (!platformHeader.canRead()) {
            throw new IllegalArgumentException("Platform header \"" + platformHeader + "\" is not readable");
        }
        this.platformHeader = platformHeader;
    }
    
    public void setKconfigModelsDir(File kconfigDir) throws IllegalArgumentException {
        if (!kconfigDir.isDirectory()) {
            throw new IllegalArgumentException("Kconfig dir \"" + kconfigDir + "\" is not a directory");
        }
        this.kconfigModelsDir = kconfigDir;
    }
    
    public void addPostIncludeDir(File postIncludeDir) {
        postIncludeDirs.add(postIncludeDir);
    }
    
    public void addDefaultPostIncludeDirs() {
        addPostIncludeDir(new File("/usr/lib/gcc/x86_64-linux-gnu/5/include"));
        addPostIncludeDir(new File("/usr/include/x86_64-linux-gnu"));
    }
    
    public void clearPostIncludeDirs() {
        postIncludeDirs.clear();
    }
    
    public void addLinuxIncludeDir(File linuxIncludeDir) {
        linuxIncludeDirs.add(linuxIncludeDir);
    }
    
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
    
    public void clearLinuxIncludeDirs() {
        linuxIncludeDirs.clear();
    }
    
    private void parseOutput(File file, SourceFile sourceFile) throws IOException {
        BufferedReader in = new BufferedReader(new FileReader(file));
        
        Block currentBlock = null;
        String currentLocation = sourceFile.getPath().getPath();
        
        
        String line;
        int lineNumber = 0;
        while ((line = in.readLine()) != null) {
            lineNumber++;
            
            if (line.startsWith("#line")) {
                String[] parts = line.split(" ");
                currentLocation = parts[2].substring(1, parts[2].length() - 1);
                currentLocation = relativize(new File(currentLocation), linuxDir);
                
            } else if (line.startsWith("#if")) {
                Block newBlock = new Block(line.substring(4), currentLocation, lineNumber);
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
        
        if (sourceFile.getFirstBlock() != null) {
        
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
        
    }
    
    public void runOnFile(SourceFile file) throws IOException {
        File output = new File(file.getPath().getPath().replace('/', '.'));
        if (!output.mkdir()) {
            Logger.INSTANCE.logError("Can't create directory " + output.getAbsolutePath());
            return;
        }
        File piFile = new File(output, "chef.pi");
        
        List<String> command = new ArrayList<>();
        command.add(exe.getAbsolutePath());
        command.add("--platfromHeader=" + platformHeader.getAbsolutePath());
        for (File postIncludeDir : postIncludeDirs) {
            command.add("--postIncludes=" + relativize(postIncludeDir, new File("/")));
        }
        command.add("--lex");
        command.add("--prefixonly=CONFIG_");
        command.add("--output=" + new File(output, "chef").getAbsolutePath());
        command.add("--writePI");
        command.add("--no-analysis");
        if (file.getPresenceCondition() != null) {
            File pc = new File(output, "file.pc");
            
            try {
                BufferedWriter writer = new BufferedWriter(new FileWriter(pc));
                writer.write(file.getPresenceCondition().toString()); // TODO
                writer.newLine();
                writer.close();
                
                command.add("--filePC=" + pc.getAbsolutePath());
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
        
        // TODO: for debug only
        builder.redirectOutput(Redirect.to(new File(output, "stdout.log")));
        builder.redirectError(Redirect.to(new File(output, "stderr.log")));
        
        Process chef = builder.start();
        
        int status = -1;
        try {
            status = chef.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (status != 0) {
            String[] lines = new String[command.size() + 2];
            lines[0] = "A typechef instance exited with status: " + status;
            lines[1] = "The command was: ";
            for (int i = 0; i < command.size(); i++) {
                lines[i + 2] = command.get(i);
            }
            Logger.INSTANCE.logError(lines); 
            return;
        }
        
        parseOutput(piFile, file);
        
        piFile.delete();
    }
    
    public static String relativize(File path, File base) {
        return base.toURI().relativize(path.toURI()).getPath();
    }
    
}
