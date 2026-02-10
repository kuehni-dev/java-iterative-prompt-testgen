package dev.kuehni.llmtestgen.d4j;

import com.opencsv.CSVParser;
import dev.kuehni.llmtestgen.cobertura.CoverageResult;
import dev.kuehni.llmtestgen.major.Mutant;
import dev.kuehni.llmtestgen.major.MutantResult;
import dev.kuehni.llmtestgen.major.MutantState;
import dev.kuehni.llmtestgen.major.MutationTestResult;
import dev.kuehni.llmtestgen.util.xml.XmlUtils;
import jakarta.annotation.Nonnull;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.Objects;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static dev.kuehni.llmtestgen.util.functions.FunctionUtils.unchecked;

public class Defects4JUtils {

    private static final Pattern COMPILE_TEST_OUTPUT_PATTERN =
            Pattern.compile("compile[.\\-]tests?:\\n(?:^$\\n?|^\\s.+$\\n?)+", Pattern.MULTILINE);
    public static final Pattern JAVAC_PATTERN = Pattern.compile("^\\s*\\[javac] ", Pattern.MULTILINE);

    private Defects4JUtils() {}


    /**
     * Extract the `javac` program output of the `compile.tests` task from the stdout of a `defects4j compile` command.
     */
    @Nonnull
    public static String extractCompileTestJavac(@Nonnull String stdout) {
        Objects.requireNonNull(stdout, "stdout");

        // See /docs/defects4j.md

        return COMPILE_TEST_OUTPUT_PATTERN.matcher(stdout)
                .results()
                .map(MatchResult::group)
                .flatMap(match -> match.lines().skip(1)) // Skip the `compile.tests:` line
                .flatMap(line -> {
                    final var javacMatcher = JAVAC_PATTERN.matcher(line);
                    if (!javacMatcher.find()) {
                        // Filter out non-javac lines
                        return Stream.empty();
                    }
                    return Stream.of(javacMatcher.replaceFirst(""));
                })
                .collect(Collectors.joining("\n"));
    }

    @Nonnull
    public static MutationTestResult parseMutationTestResult(
            @Nonnull String fullyQualifiedSourceClassName,
            @Nonnull String killCsv,
            @Nonnull String mutantsLog
    ) {
        Objects.requireNonNull(fullyQualifiedSourceClassName, "fullyQualifiedSourceClassName");
        Objects.requireNonNull(killCsv, "killCsv");
        Objects.requireNonNull(mutantsLog, "mutantsLog");

        final var csvParser = new CSVParser();
        final var mutationResults = killCsv.lines()
                .skip(1)
                .map(unchecked(csvParser::parseLine))
                .collect(Collectors.toMap(
                        fields -> fields[0],
                        fields -> MutantState.parse(fields[1])
                ));

        final var results = mutantsLog.lines()
                .map(Mutant::parse)
                .filter(mutant -> mutant.className().equals(fullyQualifiedSourceClassName))
                .map(mutant -> new MutantResult(mutant, mutationResults.get(mutant.id())))
                .toList();
        return MutationTestResult.from(results);
    }

    @Nonnull
    public static CoverageResult parseCoverageResult(
            @Nonnull String fullyQualifiedTestClassName,
            @Nonnull InputStream readStream
    ) {
        Objects.requireNonNull(fullyQualifiedTestClassName, "fullyQualifiedTestClassName");
        Objects.requireNonNull(readStream, "readStream");

        final Document coverageDocument;
        try {
            final var documentBuilderFactory = DocumentBuilderFactory.newInstance();
            final var docBuilder = documentBuilderFactory.newDocumentBuilder();
            docBuilder.setEntityResolver((_, systemId) -> systemId.contains("cobertura") ?
                    new InputSource(new StringReader("")) : null);
            coverageDocument = docBuilder.parse(readStream);
        } catch (ParserConfigurationException ex) {
            throw new IllegalStateException("XML Parser configuration error", ex);
        } catch (SAXException ex) {
            throw new IllegalStateException("Failed to parse coverage XML", ex);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read coverage result", e);
        }

        coverageDocument.getDocumentElement().normalize();

        final var classes = coverageDocument.getElementsByTagName("class");
        final var classElement = XmlUtils.stream(classes)
                .flatMap(XmlUtils.filteringIsElement())
                .filter(XmlUtils.attributeEquals("name", fullyQualifiedTestClassName))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Could not find class with name " + fullyQualifiedTestClassName));

        final var lineRate = Double.parseDouble(classElement.getAttribute("line-rate"));
        final var branchRate = Double.parseDouble(classElement.getAttribute("branch-rate"));

        final var methods = XmlUtils.stream(classElement.getElementsByTagName("method"))
                .flatMap(XmlUtils.filteringIsElement())
                .map(Defects4JUtils::getMethodCoverageInfo)
                .toList();

        return new CoverageResult(methods, lineRate, branchRate);
    }

    @Nonnull
    private static CoverageResult.Method getMethodCoverageInfo(@Nonnull Element methodElement) {
        final var name = methodElement.getAttribute("name");
        final var constructor = "<clinit>".equals(name);
        final var signature = methodElement.getAttribute("signature");
        final var lineRate = Double.parseDouble(methodElement.getAttribute("line-rate"));
        final var branchRate = Double.parseDouble(methodElement.getAttribute("branch-rate"));

        final var lines = XmlUtils.stream(methodElement.getElementsByTagName("line"))
                .flatMap(XmlUtils.filteringIsElement())
                .flatMap(lineElem -> {
                    final var number = Integer.parseInt(lineElem.getAttribute("number"), 10);
                    final var hits = Integer.parseInt(lineElem.getAttribute("hits"), 10);
                    final var branch = Boolean.parseBoolean(lineElem.getAttribute("branch"));
                    final var conditionCoverage =
                            branch ? lineElem.getAttribute("condition-coverage") : null;

                    return Stream.of(new CoverageResult.Line(number, hits, conditionCoverage));
                })
                .toList();

        return new CoverageResult.Method(
                name,
                signature,
                constructor,
                lines,
                lineRate,
                branchRate
        );
    }
}
