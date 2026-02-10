package dev.kuehni.llmtestgen.fixer;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import dev.kuehni.llmtestgen.dto.TestClassInfo;
import jakarta.annotation.Nonnull;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class ParsedTestClassTest {

    static Path NULL_PATH = Path.of("/dev/null");

    @Test
    void testMethodNames_returnsTestMethods() {
        final var testCode = """
                import static org.junit.Assert.assertTrue;
                
                public class NumberUtilsTest {
                
                    @Test
                    public void testConstructor() {
                        assertTrue(true);
                    }
                
                    @Test
                    public void testToIntString() {
                        assertTrue(true);
                    }
                
                    public void notATestMethod() {
                    }
                
                    @Test
                    public void testToIntStringI() {
                        assertTrue(true);
                    }
                }
                """;

        final var info = new TestClassInfo(NULL_PATH, NULL_PATH, "", "NumberUtilsTest");
        final var parsedClass = new ParsedTestClass(info, testCode, parseJava(testCode));

        final var expected = new String[]{
                "testConstructor",
                "testToIntString",
                "testToIntStringI"
        };
        final var actual = parsedClass.testMethodNames();

        assertArrayEquals(expected, actual);
    }


    @Nonnull
    CompilationUnit parseJava(@Nonnull String code) {
        return new JavaParser().parse(code).getResult().orElseThrow();
    }
}