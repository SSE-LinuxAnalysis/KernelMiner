package de.uni_hildesheim.sse.kernel_miner.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Writer;

import de.schlichtherle.truezip.file.TFile;
import de.schlichtherle.truezip.file.TFileInputStream;
import de.schlichtherle.truezip.file.TFileWriter;

/**
 * Wrapper for accessing files inside a zip archive.
 * 
 * @author Adam Krafczyk
 */
public class ZipArchive {
    
    private File zipFile;
    
    /**
     * Opens a zip archive. If the archive does not exist, then it is created
     * on the first {@link #writeFile(File, String)} call. Until then, it is
     * treated like an empty archive.
     * 
     * @param location The location of the archive file.
     */
    public ZipArchive(File location) {
        zipFile = new TFile(location);
    }
    
    /**
     * Checks, whether the archive contains the given file.
     * 
     * @param file The path of the file in the archive.
     * @return <code>true</code> if the file exists and is a file, <code>false</code> otherwise.
     */
    public boolean containsFile(File file) {
        TFile tfile = new TFile(zipFile, file.getPath());
        return tfile.exists();
    }
    
    /**
     * Reads the contents of a file in the archive.
     * 
     * @param file The path of the file in the archive.
     * @return The content of the file.
     * 
     * @throws FileNotFoundException If the archive does not contain the given file.
     * @throws IOException If reading the file fails.
     */
    public String readFile(File file) throws FileNotFoundException, IOException {
        if (!containsFile(file)) {
            throw new FileNotFoundException("Archive does not contain file " + file);
        }
        
        TFile tfile = new TFile(zipFile, file.getPath());
        
        TFileInputStream in = new TFileInputStream(tfile);
        String content = Files.readFile(in);
        in.close();
        
        return content;
    }
    
    /**
     * Writes, or overwrites the given file in the archive.
     * 
     * @param file The path of the file in the archive.
     * @param content The new content of the file.
     * 
     * @throws IOException If writing the file fails.
     */
    public void writeFile(File file, String content) throws IOException {
        TFile tfile = new TFile(zipFile, file.getPath());
        
        Writer out = new TFileWriter(tfile);
        out.write(content);
        out.close();
    }
    
    /**
     * Removes the given file from the archive.
     * 
     * @param file The path of the file in the archive.
     * 
     * @throws FileNotFoundException If the given file does not exist.
     * @throws IOException If an IO error occurs.
     */
    public void deleteFile(File file) throws FileNotFoundException, IOException {
        if (!containsFile(file)) {
            throw new FileNotFoundException("Archive does not contain file " + file);
        }
        
        TFile tfile = new TFile(zipFile, file.getPath());
        tfile.rm();
    }
    
    /**
     * Copies or overwrites the given file with the contents of <code>toCopy</code>.
     * 
     * @param file The path of the file in the archive.
     * @param toCopy The "real" file outside the archive to read the content of.
     * 
     * @throws FileNotFoundException If <code>toCopy</code> is not found.
     * @throws IOException If reading or writing the files fails.
     */
    public void copyFileToArchive(File file, File toCopy) throws FileNotFoundException, IOException {
        String content = Files.readFile(toCopy);
        writeFile(file, content);
    }
    
    /**
     * Extracts the given file from the archive into the given target file.
     * The file in the archive remains unchanged.
     * 
     * @param file The path of the file in the archive.
     * @param target The "real" target file outside the archive. This file will
     *      be overwritten or created with the content from the archive file.
     *      
     * @throws FileNotFoundException If the given file in the archive does not exist.
     * @throws IOException If reading or writing the files fails.
     */
    public void extract(File file, File target) throws FileNotFoundException, IOException {
        String content = readFile(file);
        Files.writeFile(target, content);
    }
    
}
