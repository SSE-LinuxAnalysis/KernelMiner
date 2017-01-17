package de.uni_hildesheim.sse.kernel_miner.kbuild;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.uni_hildesheim.sse.kernel_miner.code.SourceFile;
import de.uni_hildesheim.sse.kernel_miner.util.Logger;

public class KbuildParamFile {
    
    private Map<File, List<String>> parameters;

    public KbuildParamFile(File file) throws IOException {
        parameters = new HashMap<>();
        readFile(file);
    }
    
    public List<String> getExtraParameters(SourceFile file) {
        List<String> result = new ArrayList<>();
        
        for (Map.Entry<File, List<String>> entry : parameters.entrySet()) {
            if (file.getPath().getPath().startsWith(entry.getKey().getPath())) {
                result.addAll(entry.getValue());
            }
        }
        
        return result;
    }
    
    private void readFile(File file) throws IOException {
        BufferedReader in = new BufferedReader(new FileReader(file));
        
        String line;
        while ((line = in.readLine()) != null) {
            readLine(line);
        }
        
        in.close();
    }
    
    private void readLine(String line) {
        int colonIndex = line.indexOf(':');
        if (colonIndex <= 0) {
            Logger.INSTANCE.logError("Invalid line in KbuildParamFile:", line);
            return;
        }
        
        File path = new File(line.substring(0, colonIndex));
        
        if (line.charAt(colonIndex + 1) == ' ') {
            colonIndex++;
        }
        line = line.substring(colonIndex + 1);
        
        List<String> params = readParams(line);
        if (!parameters.containsKey(path)) {
            parameters.put(path, params);
        } else {
            parameters.get(path).addAll(params);
        }
    }
    
    private List<String> readParams(String line) {
        List<String> result = new ArrayList<>();
        
        StringBuilder current = new StringBuilder();
        
        boolean inSingleQuote = false;
        boolean inDoubleQuote = false;
        
        for (int i = 0; i < line.length(); i++) {
            if (line.charAt(i) == '\\') {
                i++;
                current.append(line.charAt(i));
                continue;
            }
            
            if (!inSingleQuote && !inDoubleQuote && Character.isWhitespace(line.charAt(i))) {
                if (!current.toString().trim().isEmpty()) {
                    result.add(current.toString());
                }
                current = new StringBuilder();
                
            } else if (line.charAt(i) == '\'') {
                inSingleQuote = !inSingleQuote;
                
            } else if (!inSingleQuote && line.charAt(i) == '"') {
                inDoubleQuote = !inDoubleQuote;
                
            } else {
                current.append(line.charAt(i));
            }
        }
        
        if (!current.toString().trim().isEmpty()) {
            result.add(current.toString());
        }
        
        return result;
    }
    
}
