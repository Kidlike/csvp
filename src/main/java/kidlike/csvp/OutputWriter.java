package kidlike.csvp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;

import picocli.CommandLine;
import picocli.CommandLine.Model.ArgSpec;

/**
 * Handles writing strings to the configured output. Also graceful teardown.
 *
 * <hr/>
 *
 * Unfortunately if OutputWriter would extend PrintStream, then it can not be injected.
 *
 * <p>The produced error by the compiler is:</p>
 * <pre>
 * It's not possible to add a synthetic constructor with no parameters to the unproxyable bean class
 * </pre>
 */
@SuppressWarnings({"java:S106", "Legit usage of System.out"})
@ApplicationScoped
public class OutputWriter {
    private final PrintStream printStream;

    public OutputWriter(CommandLine.ParseResult parseResult) throws IOException {
        ArgSpec outputFileArg = parseResult.matchedOption("-o");
        if (outputFileArg != null) {
            File outputFile = outputFileArg.getValue();
            printStream = new PrintStream(new FileOutputStream(outputFile), false, StandardCharsets.UTF_8);
        } else {
            printStream = new PrintStream(System.out, true, StandardCharsets.UTF_8);
        }
    }

    @PreDestroy
    public void teardown() {
        printStream.close();
        if (printStream.checkError()) {
            System.err.println("Something went wrong during flushing/closing the output.");
        }
    }

    public void println(String x) {
        printStream.println(x);
    }
}
