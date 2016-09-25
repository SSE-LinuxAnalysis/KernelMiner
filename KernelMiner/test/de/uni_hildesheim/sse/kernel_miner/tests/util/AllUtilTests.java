package de.uni_hildesheim.sse.kernel_miner.tests.util;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import de.uni_hildesheim.sse.kernel_miner.tests.util.logic.FormulaTest;
import de.uni_hildesheim.sse.kernel_miner.tests.util.parser.ParserTest;

@RunWith(Suite.class)
@SuiteClasses({
    FilesTest.class,
    FormulaTest.class,
    ParserTest.class,
    ZipArchiveTest.class,
})
public class AllUtilTests {

}
