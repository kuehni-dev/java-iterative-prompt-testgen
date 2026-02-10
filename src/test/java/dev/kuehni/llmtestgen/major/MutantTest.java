package dev.kuehni.llmtestgen.major;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class MutantTest {

    @Test
    void parse_succeeds_when_inputIsCorrect() {
        final var first = Mutant.parse("1:LVR:0:POS:org.apache.commons.lang3.math.NumberUtils:34:1218:0L |==> 1L");
        assertEquals("1", first.id());
        assertEquals("LVR", first.operator());
        assertEquals("0", first.originalSignature());
        assertEquals("POS", first.mutationStrategy());
        assertEquals("org.apache.commons.lang3.math.NumberUtils", first.className());
        assertNull(first.methodSignature());
        assertEquals("34", first.lineNumber());
        assertEquals("1218", first.fileOffset());
        assertEquals("0L", first.originalExpression());
        assertEquals("1L", first.mutatedExpression());

        final var second = Mutant.parse(
                "99:COR:&&(boolean,boolean):LHS(boolean,boolean):org.apache.commons.lang3.math.NumberUtils@createNumber(java.lang.String):477:17194:hexDigits == 16 && firstSigDigit > '7' |==> hexDigits == 16");
        assertEquals("99", second.id());
        assertEquals("COR", second.operator());
        assertEquals("&&(boolean,boolean)", second.originalSignature());
        assertEquals("LHS(boolean,boolean)", second.mutationStrategy());
        assertEquals("org.apache.commons.lang3.math.NumberUtils", second.className());
        assertEquals("createNumber(java.lang.String)", second.methodSignature());
        assertEquals("477", second.lineNumber());
        assertEquals("17194", second.fileOffset());
        assertEquals("hexDigits == 16 && firstSigDigit > '7'", second.originalExpression());
        assertEquals("hexDigits == 16", second.mutatedExpression());

        final var third = Mutant.parse(
                "412:STD:<INC>:<NO-OP>:org.apache.commons.lang3.math.NumberUtils@createBigInteger(java.lang.String):739:27665:pos++; |==> <NO-OP>");
        assertEquals("412", third.id());
        assertEquals("STD", third.operator());
        assertEquals("<INC>", third.originalSignature());
        assertEquals("<NO-OP>", third.mutationStrategy());
        assertEquals("org.apache.commons.lang3.math.NumberUtils", third.className());
        assertEquals("createBigInteger(java.lang.String)", third.methodSignature());
        assertEquals("739", third.lineNumber());
        assertEquals("27665", third.fileOffset());
        assertEquals("pos++;", third.originalExpression());
        assertEquals("<NO-OP>", third.mutatedExpression());
    }
}
