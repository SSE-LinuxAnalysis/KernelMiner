package de.uni_hildesheim.sse.kernel_miner.run;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import de.uni_hildesheim.sse.kernel_miner.util.Logger;
import de.uni_hildesheim.sse.kernel_miner.util.ZipArchive;

public class TypeChefResultAnalysis {
    
    private Properties config;
    
    public TypeChefResultAnalysis(String configFile) throws IOException {
        config = new Properties();
        try {
            config.load(new FileInputStream(new File(configFile)));
        } catch (FileNotFoundException e) {
            System.out.println("Warning: Log file not found; using default values");
        }
        
        String logFile = config.getProperty("logFile", "stdout");
        if (logFile.equals("stdout")) {
            Logger.init();
        } else {
            Logger.init(new FileOutputStream(new File(logFile)));
        }
    }
    
    public void start() throws IOException {
        ZipArchive pcArchive = new ZipArchive(new File(config.getProperty("typechefAnalyzer.pcArchive")));
        File pcFile = new File("pcs.csv");
        
        if (!pcArchive.containsFile(pcFile)) {
            throw new FileNotFoundException("pcArchive does not contain " + pcFile);
        }
        
        // TODO: temporary
        // find a variables

        boolean inVar = true;
        StringBuffer currentVar = new StringBuffer();
        
        Set<String> variables = new HashSet<>(30000);
        
        long numRead = 0;
        long t0 = System.currentTimeMillis();
        long size = pcArchive.getSize(pcFile);
        
        int read;
        byte[] buffer = new byte[65536 * 2];
        InputStream in = pcArchive.getInputStream(pcFile);
        while ((read = in.read(buffer)) != -1) {
            for (int i = 0; i < read; i++) {
                numRead++;
                
                if (System.currentTimeMillis() - t0 >= 5000) {
                    t0 = System.currentTimeMillis();
                    Logger.INSTANCE.logInfo(numRead + " / " + size + " bytes read",
                            variables.size() + " vars so far");
                }
                
                char c = (char) buffer[i];
                if (inVar) {
                    if (c == ';') {
                        inVar = false;
                        variables.add(currentVar.toString());
                    } else {
                        currentVar.append(c);
                    }
                } else {
                    if (c == '\n') {
                        inVar = true;
                        currentVar = new StringBuffer();
                    }
                }
            }
        }
        in.close();
        
        Logger.INSTANCE.logInfo(variables.size() + " variables found");
        
        
        System.out.println("Type variable names to search for them:");
        BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            String line = console.readLine();
            
            if (variables.contains(line)) {
                System.out.println("Found variable " + line);
            } else {
                System.out.println("Did not find variable " + line);
            }
        }
        
    }
    
    public static void main(String[] args) throws IOException {
        String configFile = "kernelminer.properties";
        if (args.length > 0) {
            configFile = args[0];
        }
        new TypeChefResultAnalysis(configFile).start();
    }

}
