package de.uni_hildesheim.sse.kernel_miner.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

/**
 * Helper functions to work with files.
 * 
 * @author Adam Krafczyk
 */
public class Files {
    
    /**
     * No instances allowed
     */
    private Files() {
    }

    /**
     * Relativizes the path of a file, compared to another given file.
     * 
     * @param file The file, of which the location should be represented relative to <code>base</code>.
     * @param base The base file which should be the base of the relative path to <code>file</code>.
     * @return A string representing the path to <code>file</code> relative from <code>base</code>.
     */
    public static String relativize(File file, File base) {
        return base.toURI().relativize(file.toURI()).getPath();
    }
    
    /**
     * Reads the complete contents the given stream returns. The stream is read
     * until it's <code>read</code> method returns -1.
     * 
     * @param in The stream to read completely.
     * @param charset The charset to interpret the read bytes as.
     * @return The complete string read from the stream.
     * 
     * @throws IOException If reading the stream throws an exception.
     */
    public static String readStream(InputStream in, Charset charset) throws IOException {
        ByteArrayOutputStream content = new ByteArrayOutputStream(512);
        
        int read;
        while ((read = in.read()) != -1) {
            content.write(read);
        }
        
        return new String(content.toByteArray(), charset);
    }
    
    /**
     * Reads the complete contents the given stream returns. The stream is read
     * until it's <code>read</code> method returns -1. The stream's bytes are
     * interpreted to be in UTF-8.
     * 
     * @param in The stream to read completely.
     * @return The complete string read from the stream.
     * 
     * @throws IOException If reading the stream throws an exception.
     */
    public static String readStream(InputStream in) throws IOException {
        return readStream(in, Charset.forName("UTF-8"));
    }
    
    /**
     * Reads the complete contents of the given file.
     * 
     * @param file The file to read completely.
     * @param charset The charset to interpret the read bytes as.
     * @return The complete contents of the given file.
     * 
     * @throws FileNotFoundException If the given file does not exist.
     * @throws IOException If reading the file throws an exception.
     */
    public static String readFile(File file, Charset charset) throws FileNotFoundException, IOException {
        String content = null;
        FileInputStream in = null;
        try {
            in = new FileInputStream(file);
            content = readStream(in, charset);
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
            }
        }
        
        return content;
    }
    
    /**
     * Reads the complete contents of the given file. The read bytes are interpreted
     * as UTF-8.
     * 
     * @param file The file to read completely.
     * @return The complete contents of the given file.
     * 
     * @throws FileNotFoundException If the given file does not exist.
     * @throws IOException If reading the file throws an exception.
     */
    public static String readFile(File file) throws FileNotFoundException, IOException {
        return readFile(file, Charset.forName("UTF-8"));
    }
    
    /**
     * Writes the given string into the given file. If the file exists, then
     * it is overwritten. If it doesn't exist, it is created.
     * The content is written in UTF-8 encoding.
     * 
     * @param file The file to write into.
     * @param content The content to write into the file.
     * 
     * @throws IOException If writing the file throws an exception.
     */
    public static void writeFile(File file, String content) throws IOException {
        writeFile(file, content, Charset.forName("UTF-8"));
    }
    
    /**
     * Writes the given string into the given file. If the file exists, then
     * it is overwritten. If it doesn't exist, it is created.
     * 
     * @param file The file to write into.
     * @param content The content to write into the file.
     * @param charset The charset to write the contents into the file.
     * 
     * @throws IOException If writing the file throws an exception.
     */
    public static void writeFile(File file, String content, Charset charset) throws IOException {
        byte[] bytes = content.getBytes(charset);
        FileOutputStream out = new FileOutputStream(file);
        out.write(bytes);
        out.close();
    }
    
}
