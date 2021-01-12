package kidlike.csvp;

import javax.enterprise.inject.Any;
import javax.inject.Inject;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@Disabled("java.lang.IllegalAccessError: superclass access check failed: " +
    "class com.oracle.truffle.polyglot.PolyglotImpl (in unnamed module @0x71f10042) cannot access " +
    "class org.graalvm.polyglot.impl.AbstractPolyglotImpl (in module org.graalvm.sdk) because " +
    "module org.graalvm.sdk does not export org.graalvm.polyglot.impl to unnamed module @0x71f10042." +
    "Related issue: " + "https://github.com/quarkusio/quarkus/issues/10226")
@QuarkusTest
class CSVTransformationsTest {
    @Inject
    @Any
    CsvProcessor csvTransformations;

    @Test
    public void test() {
        csvTransformations.run();
    }
}
