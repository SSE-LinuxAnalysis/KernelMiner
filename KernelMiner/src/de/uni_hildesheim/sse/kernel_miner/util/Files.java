package de.uni_hildesheim.sse.kernel_miner.util;

import java.io.File;

public class Files {

    public static String relativize(File path, File base) {
        return base.toURI().relativize(path.toURI()).getPath();
    }
    
}
