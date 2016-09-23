package de.uni_hildesheim.sse.kernel_miner.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Writer;

import de.schlichtherle.truezip.file.TFile;
import de.schlichtherle.truezip.file.TFileReader;
import de.schlichtherle.truezip.file.TFileWriter;

/**
 * Wrapper for accessing files inside a zip archive.
 * 
 * @author Adam Krafczyk
 */
public class ZipFile {
    
    private File zipFile;
    
    /**
     * Opens a zip archive. If the archive does not exist, then it is created
     * on the first {@link #writeFile(File, String)} call. Until then, it is
     * treated like an empty archive.
     * 
     * @param location The location of the archive file.
     */
    public ZipFile(File location) {
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
     * @return The contents of the file, with \n as line break character.
     * 
     * @throws FileNotFoundException If the archive does not contain the given file.
     * @throws IOException If reading the file fails.
     */
    public String readFile(File file) throws FileNotFoundException, IOException {
        if (!containsFile(file)) {
            throw new FileNotFoundException("Archive does not contain file " + file);
        }
        
        TFile tfile = new TFile(zipFile, file.getPath());
        
        BufferedReader in = new BufferedReader(new TFileReader(tfile));

        StringBuffer content = new StringBuffer();
        String line;
        while ((line = in.readLine()) != null) {
            content.append(line).append('\n');
        }
        
        in.close();
        
        return content.toString();
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
    
}
