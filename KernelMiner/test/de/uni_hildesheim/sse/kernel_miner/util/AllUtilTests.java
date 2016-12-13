package de.uni_hildesheim.sse.kernel_miner.util;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import de.uni_hildesheim.sse.kernel_miner.util.logic.FormulaTest;
import de.uni_hildesheim.sse.kernel_miner.util.parser.ParserTest;

@RunWith(Suite.class)
@SuiteClasses({
    FilesTest.class,
    FormulaTest.class,
    LoggerTest.class,
    ParserTest.class,
    ZipArchiveTest.class,
})
public class AllUtilTests {

}
