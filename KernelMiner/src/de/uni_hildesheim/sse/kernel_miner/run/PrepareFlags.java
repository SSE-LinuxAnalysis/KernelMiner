package de.uni_hildesheim.sse.kernel_miner.run;

import java.io.File;
import java.io.IOException;

import de.fosd.typechef.linux.MakeIncludeAnalysis;
import scala.Option;

public class PrepareFlags {

    public static void main(String[] args) throws IOException {
//        List<SourceFile> files = KbuildMiner.readOutput(new File("/home/adam/typechef_tests/kbuild_pcs/linux-4.4/x86.pcs.txt"));
        File linuxDir = new File("/home/adam/typechef_tests/linux-releases/linux-4.4");
        File output = new File("kbuildparam.sh");
        
        Option<File> opt1;
        opt1 = Option.apply(output);
        Option<File> opt2;
        opt2 = Option.empty();
        MakeIncludeAnalysis.analyze(linuxDir, opt1, opt2);
    }
    
}
