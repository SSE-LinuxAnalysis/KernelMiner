package de.uni_hildesheim.sse.kernel_miner.tests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import de.uni_hildesheim.sse.kernel_miner.tests.kbuild.AllKbuildTests;
import de.uni_hildesheim.sse.kernel_miner.tests.util.AllUtilTests;

@RunWith(Suite.class)
@SuiteClasses({
    AllKbuildTests.class,
    AllUtilTests.class,
})
public class AllKernelMinerTests {

}
