package de.uni_hildesheim.sse.kernel_miner.code;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import de.uni_hildesheim.sse.kernel_miner.util.Files;
import de.uni_hildesheim.sse.kernel_miner.util.Logger;
import de.uni_hildesheim.sse.kernel_miner.util.logic.Formula;
import de.uni_hildesheim.sse.kernel_miner.util.logic.True;
import de.uni_hildesheim.sse.kernel_miner.util.parser.ExpressionFormatException;
import de.uni_hildesheim.sse.kernel_miner.util.parser.Parser;
import de.uni_hildesheim.sse.kernel_miner.util.parser.VariableCache;

/**
 * A parser for the .pi files that TypeChef outputs.
 * 
 * @author Adam Krafczyk
 */
class TypeChefParser {
    
    private static final VariableCache PC_CACHE = new VariableCache();
    
    private static final Parser<Formula> PC_PARSER = new Parser<>(new TypeChefPresenceConditionGrammar(PC_CACHE));
    
    private List<Block> blocks;
    
    private Block lastBlock;
    
    private int currentLineNumber;
    
    private String currentLocation;
    
    private SourceFile sourceFile;
    
    private File sourceDir;

    /**
     * Creates a parser for the given source file.
     * 
     * @param sourceFile The {@link SourceFile} for which the .pi file will be parsed.
     *      The {@link Block}s that this parser creates will be added to it.
     * @param sourceDir The directory of the source code tree. Used to relativise path names.
     */
    public TypeChefParser(SourceFile sourceFile, File sourceDir) {
        blocks = new LinkedList<>();
        this.sourceFile = sourceFile;
        this.sourceDir = sourceDir;
    }
    
    /**
     * Parses the given .pi file. The {@link SourceFile} will have the {@link Block}s
     * added to it afterwards.
     * 
     * @param in The stream that reads the content of the .pi file.
     * 
     * @throws IOException If reading the piFile stream fails.
     */
    public void parseFile(InputStream piFile) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(piFile));
        
        currentLocation = sourceFile.getPath().getPath();
        
        boolean inBlockComment = false;
        
        String line;
        while ((line = in.readLine()) != null) {
            currentLineNumber++;
            line = line.trim();
            
            // parse only lines that are not commented out
            boolean parseThisLine = true;
            
            if (inBlockComment) {
                parseThisLine = false;
                
                int endBlockIndex = line.indexOf("*/");
                if (endBlockIndex != -1) {
                    inBlockComment = false;
                    
                    if (endBlockIndex < line.length() - 2) {
                        parseThisLine = true;
                        line = line.substring(endBlockIndex + 2);
                    }
                }
                
            } else {
                int blockIndex = line.indexOf("/*");
                if (blockIndex != -1) {
                    inBlockComment = true;
                    
                    if (blockIndex == 0) {
                        parseThisLine = false;
                    } else {
                        line = line.substring(0, blockIndex);
                    }
                }
            }
            
            if (parseThisLine) {
                
                int lineCommentIndex = line.indexOf("//");
                if (lineCommentIndex != -1) {
                    line = line.substring(0, lineCommentIndex);
                }
                
                if (line.trim().length() != 0) {
                    parseLine(line);
                }
            }
        }
        
        removeEmptyBlocks();
        
        sourceFile.setBlocks(blocks);
    }
    
    /**
     * Adds a new block to the end of the blocks list.
     * 
     * @param pc The presence condition of the new block.
     */
    private void addBlock(Formula pc) {
        Block newBlock = new Block(pc, currentLocation, currentLineNumber);
        blocks.add(newBlock);
        lastBlock = newBlock;
    }
    
    /**
     * Parses a single line. Checks whether it opens or closes a block. If not,
     * then the contents are simply added to the current block.
     * 
     * @param line The line to parse.
     */
    private void parseLine(String line) {
        if (line.startsWith("#line ")) {
            String[] parts = line.split(" ");
            currentLocation = parts[2].substring(1, parts[2].length() - 1);
            currentLocation = Files.relativize(new File(currentLocation), sourceDir);
            if (lastBlock != null && !currentLocation.equals(lastBlock.getLocation())) {
                // add a new block to indicate the new file location
                addBlock(new True());
            }
            
        } else if (line.startsWith("#if ")) {
            String pcStr = line.substring(4);
            
            Formula pc = new True();
            try {
                pc = PC_PARSER.parse(pcStr);
            } catch (ExpressionFormatException e) {
                Logger.INSTANCE.logException("Can't parse presence condition " + pcStr, e);
            }
            PC_CACHE.clear();
            
            addBlock(pc);
            
        } else if (line.startsWith("#endif")) {
            addBlock(new True());
            
        } else if (line.startsWith("#")) {
            Logger.INSTANCE.logError("Found unkown preprocessor directive in file " + sourceFile.getPath() + ":",
                    line);
            
        } else {
            if (lastBlock == null) {
                addBlock(new True());
            }
            lastBlock.addLine(line);
        }
    }
    
    /**
     * Removes all empty blocks from the list of blocks. Empty blocks are generated
     * in between #endif and #if statements, as a side effect of this parser.
     */
    private void removeEmptyBlocks() {
        for (Iterator<Block> it = blocks.iterator(); it.hasNext();) {
               Block block = it.next();
               if (block.getLines().isEmpty()) {
                   it.remove();
               }
        }
    }
    
}
