package de.uni_hildesheim.sse.kernel_miner.code;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
    BlockTest.class,
    TypeChefPresenceConditionGrammarTest.class,
    TypeChefTest.class,
})
public class AllCodeTests {

}
