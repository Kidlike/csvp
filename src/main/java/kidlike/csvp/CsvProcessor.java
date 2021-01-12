package kidlike.csvp;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Optional;
import java.util.Scanner;
import java.util.stream.Collectors;
import javax.inject.Inject;

import io.quarkus.picocli.runtime.annotations.TopCommand;
import kidlike.csvp.config.InputConfig;
import kidlike.csvp.config.OutputConfig;
import kidlike.csvp.polyglot.PolyglotContext;
import kidlike.csvp.polyglot.PolyglotUtils;
import org.graalvm.polyglot.Value;
import picocli.CommandLine;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.Spec;

import static java.lang.String.format;
import static java.util.function.Predicate.not;

@TopCommand
@CommandLine.Command(name = "csvp", mixinStandardHelpOptions = true, version = "csvp 0.1",
    description = "A csv -> csv processor with user defined JS to transform each row.")
public class CsvProcessor implements Runnable {

    @Inject
    InputConfig inputConfig;

    @Inject
    OutputConfig outputConfig;

    @Inject
    OutputWriter outputWriter;

    @Spec
    CommandLine.Model.CommandSpec spec;

    Optional<File> inputFile = Optional.empty();

    @CommandLine.Option(
        names = {"-i", "--input"},
        paramLabel = "file",
        description = "Read from <file> instead of stdin.\nOmitting this option will run in interactive mode.")
    @SuppressWarnings("unused")
    public void setInputFile(File inputFile) {
        if (!inputFile.exists()) {
            throw new ParameterException(spec.commandLine(),
                format("Cannot access '%s'. No such file or directory.", inputFile.getAbsolutePath()));
        }
        if (inputFile.isDirectory()) {
            throw new ParameterException(spec.commandLine(),
                format("'%s' is a directory.", inputFile.getAbsolutePath()));
        }
        if (!inputFile.canRead()) {
            throw new ParameterException(spec.commandLine(),
                format("Cannot access '%s'. Permission denied.", inputFile.getAbsolutePath()));
        }
        this.inputFile = Optional.of(inputFile);
    }

    File outputFile;

    @CommandLine.Option(names = {"-o", "--output"},
        paramLabel = "file",
        description = "Write output to <file> instead of stdout.")
    @SuppressWarnings({"java:S899", "ResultOfMethodCallIgnored", "unused"})
    public void setOutputFile(File outputFile) {
        try {
            outputFile.createNewFile();
            outputFile.setWritable(true, true);
        } catch (Exception e) {
            throw new ParameterException(spec.commandLine(),
                format("Cannot write to '%s'. %s", outputFile.getAbsolutePath(), e.getMessage()));
        }
        this.outputFile = outputFile;
    }

    @Inject
    PolyglotContext polyglotContext;

    @Override
    @SuppressWarnings({"java:S106", "Legit usage of System.err"})
    public void run() {
        Value rowTransformJs = polyglotContext.eval("(function map(row){ " + outputConfig.getRowTransform() + "})");

        String wrapCellChar = outputConfig.getWrapCellsWith().orElse("");

        boolean isInteractiveMode = inputFile.isEmpty();
        boolean inputHasHeader = inputConfig.getHasHeader();

        if (isInteractiveMode) {
            System.err.print("[No input file -> running in interactive mode]\n");
            inputHasHeader = false;
        } else {
            printOutputHeaders(wrapCellChar);
        }

        try (Scanner input = getInput()) {
            input
                .useDelimiter(System.lineSeparator())
                .tokens()
                .skip(inputHasHeader ? 1 : 0)
                .filter(not(String::isBlank))
                .map(row -> Arrays.asList(row.split(inputConfig.getDelimiter(), -1)))
                .map(rowTransformJs::execute)
                .filter(value -> !value.isNull())
                .map(PolyglotUtils::toList)
                .forEachOrdered(row ->
                    outputWriter.println(
                        row.stream().map(cell ->
                            wrapCellChar + cell + wrapCellChar
                        ).collect(Collectors.joining(outputConfig.getDelimiter()))
                    )
                );
        }
    }

    /**
     * Returns an {@link Scanner input} object that points either stdin or a physical file (if the user provided one).
     */
    private Scanner getInput() {
        return inputFile
            .map(file -> {
                try {
                    return new Scanner(file);
                } catch (FileNotFoundException e) {
                    throw new ParameterException(spec.commandLine(),
                        format("Cannot read from file '%s'. %s", file.getAbsolutePath(), e.getMessage()));
                }
            })
            .orElseGet(() -> new Scanner(System.in));
    }

    private void printOutputHeaders(String wrapChar) {
        outputConfig.getHeaders().ifPresent(headers ->
            outputWriter.println(
                headers.stream().map(cell ->
                    wrapChar + cell + wrapChar
                ).collect(Collectors.joining(outputConfig.getDelimiter()))
            )
        );
    }
}
