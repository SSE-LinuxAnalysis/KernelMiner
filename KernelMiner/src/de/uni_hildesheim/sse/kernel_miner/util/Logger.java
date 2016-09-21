package de.uni_hildesheim.sse.kernel_miner.util;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class Logger {
    
    public static Logger INSTANCE;
    
    public static void init() {
        init(System.out);
    }
    
    public static void init(OutputStream target) {
        init(target, Charset.forName("UTF-8"));
    }
    
    public static void init(OutputStream target, Charset charset) {
        INSTANCE = new Logger(target, charset);
    }
    
    private OutputStream target;
    
    private Charset charset;
    
    private Logger(OutputStream target, Charset charset) {
        this.target = target;
        this.charset = charset;
    }
    
    private String constructHeader(String level) {
        StringBuffer hdr = new StringBuffer();
        hdr.append('[').append(level).append("] [").append(Thread.currentThread().getName()).append("] ");
        return hdr.toString();
    }
    
    private void log(String level, String... lines) {
        String header = constructHeader(level);
        String indent = header.replaceAll(".", " ");
        
        StringBuffer str = new StringBuffer(header);
        for (int i = 0; i < lines.length; i++) {
            if (i != 0) {
                str.append(indent);
            }
            str.append(lines[i]).append('\n');
        }
        byte[] bytes = str.toString().getBytes(charset);
        
        synchronized (this) {
            try {
                target.write(bytes);
                target.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    public void logInfo(String... lines) {
        log("info", lines);
    }
    
    public void logWarning(String... lines) {
        log("warning", lines);
    }
    
    public void logError(String... lines) {
        log("error", lines);
    }
    
    private void exceptionToString(Throwable e, List<String> lines) {
        lines.add(e.toString());
        
        StackTraceElement[] stack = e.getStackTrace();
        for (int i = 0; i < stack.length; i++) {
            lines.add("    at " + stack[i].toString());
        }
        
        Throwable cause = e.getCause();
        if (cause != null) {
            lines.add("Caused by:");
            exceptionToString(cause, lines);
        }
    }
    
    public void logException(String comment, Throwable e) {
        List<String> lines = new ArrayList<>(e.getStackTrace().length + 2);
        lines.add(comment + ":");
        exceptionToString(e, lines);
        log("error", lines.toArray(new String[0]));
    }

}
