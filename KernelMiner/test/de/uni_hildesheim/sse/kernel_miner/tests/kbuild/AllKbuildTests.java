package de.uni_hildesheim.sse.kernel_miner.tests.kbuild;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
    KbuildMinerTest.class,
    KbuildMinerPcGrammarTest.class,
})
public class AllKbuildTests {

}
