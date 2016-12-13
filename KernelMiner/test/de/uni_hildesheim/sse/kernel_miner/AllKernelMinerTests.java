package de.uni_hildesheim.sse.kernel_miner;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import de.uni_hildesheim.sse.kernel_miner.code.AllCodeTests;
import de.uni_hildesheim.sse.kernel_miner.kbuild.AllKbuildTests;
import de.uni_hildesheim.sse.kernel_miner.util.AllUtilTests;

@RunWith(Suite.class)
@SuiteClasses({
    AllCodeTests.class,
    AllKbuildTests.class,
    AllUtilTests.class,
})
public class AllKernelMinerTests {

}
