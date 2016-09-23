package de.uni_hildesheim.sse.kernel_miner.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

public class Files {

    public static String relativize(File path, File base) {
        return base.toURI().relativize(path.toURI()).getPath();
    }
    
    public static String readStream(InputStream in, Charset charset) throws IOException {
        StringBuffer content = new StringBuffer();
        byte[] buf = new byte[512];
        int read = 0;
        do {
            read = in.read(buf);
            if (read > 0) {
                // TODO: this might cut UTF-8 characters in half
                content.append(new String(buf, 0, read, charset));
            }
        } while (read != -1);
        
        return content.toString();
    }
    
    public static String readFile(InputStream in) throws IOException {
        return readStream(in, Charset.forName("UTF-8"));
    }
    
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
            } catch (IOException e) {}
        }
        
        return content;
    }
    
    public static String readFile(File file) throws FileNotFoundException, IOException {
        return readFile(file, Charset.forName("UTF-8"));
    }
    
    public static void writeFile(File file, String content) throws IOException {
        writeFile(file, content, Charset.forName("UTF-8"));
    }
    
    public static void writeFile(File file, String content, Charset charset) throws IOException {
        byte[] bytes = content.getBytes(charset);
        FileOutputStream out = new FileOutputStream(file);
        out.write(bytes);
        out.close();
    }
    
}
