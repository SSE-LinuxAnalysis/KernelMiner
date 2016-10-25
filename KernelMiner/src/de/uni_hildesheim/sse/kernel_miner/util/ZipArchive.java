package de.uni_hildesheim.sse.kernel_miner.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

import de.schlichtherle.truezip.file.TFile;
import de.schlichtherle.truezip.file.TFileInputStream;
import de.schlichtherle.truezip.file.TFileOutputStream;

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
        return tfile.exists() && tfile.isFile();
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
        InputStream in = getInputStream(file);
        String content = Files.readStream(in);
        in.close();
        
        return content;
    }
    
    /**
     * Retrieves an input stream to directly read from a file in the archive.
     * The stream must be closed by the caller.
     * 
     * @param file The path of the file in the archive.
     * @return An {@link InputStream} for the contents of the file.
     * 
     * @throws FileNotFoundException If the archive does not contain the given file.
     */
    public InputStream getInputStream(File file) throws FileNotFoundException {
        if (!containsFile(file)) {
            throw new FileNotFoundException("Archive does not contain file " + file);
        }
        
        TFile tfile = new TFile(zipFile, file.getPath());
        
        return new TFileInputStream(tfile);
    }
    
    /**
     * Returns the (uncompressed) size of the given file in the archive.
     * 
     * @param file The path of the file in the archive.
     * @return The size of the file in bytes.
     * 
     * @throws FileNotFoundException If the archive does not contain the given file.
     */
    public long getSize(File file) throws FileNotFoundException {
        if (!containsFile(file)) {
            throw new FileNotFoundException("Archive does not contain file " + file);
        }
        
        TFile tfile = new TFile(zipFile, file.getPath());
        
        return tfile.length();
    }
    
    /**
     * Retrieves an {@link OutputStream} to write (or overwrite) the specified
     * file in the archive. The caller must close the stream.
     * 
     * @param file The path of the file in the archive.
     * @return A stream to write to the specified file.
     * 
     * @throws IOException If creating the stream fails.
     */
    public OutputStream getOutputStream(File file) throws IOException {
        TFile tfile = new TFile(zipFile, file.getPath());
        
        return new TFileOutputStream(tfile);
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
        OutputStream out = getOutputStream(file);
        out.write(content.getBytes(Charset.forName("UTF-8")));
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
