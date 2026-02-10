package dev.kuehni.llmtestgen.d4j;

import dev.kuehni.llmtestgen.major.Mutant;
import dev.kuehni.llmtestgen.major.MutantResult;
import dev.kuehni.llmtestgen.major.MutantState;
import dev.kuehni.llmtestgen.major.MutationTestResult;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Defects4JUtilsTest {

    @Test
    void extractCompileTestJavac_succeeds_when_inputIsValid() {
        final var stdout = """
                Running ant (compile)...................................................... OK
                Running ant (compile.tests)................................................ FAIL
                Executed command:  cd /workspace/Mockito/38f && ant -f /defects4j/framework/projects/defects4j.build.xml -Dd4j.home=/defects4j -Dd4j.dir.projects=/defects4j/framework/projects -Dbasedir=/workspace/Mockito/38f  compile.tests 2>&1
                Buildfile: /defects4j/framework/projects/defects4j.build.xml
                
                compile:
                
                gradle.compile:
                
                ant.compile:
                
                clean:
                
                prepare:
                    [mkdir] Created dir: /workspace/Mockito/38f/target/classes
                    [mkdir] Created dir: /workspace/Mockito/38f/target/test-classes
                    [mkdir] Created dir: /workspace/Mockito/38f/target/reports/junit
                
                compile:
                    [javac] /workspace/Mockito/38f/build.xml:76: warning: 'includeantruntime' was not set, defaulting to build.sysclasspath=last; set to false for repeatable builds
                    [javac] Compiling 169 source files to /workspace/Mockito/38f/target/classes
                    [javac] warning: [options] bootstrap class path not set in conjunction with -source 6
                    [javac] warning: [options] source value 6 is obsolete and will be removed in a future release
                    [javac] warning: [options] target value 1.6 is obsolete and will be removed in a future release
                    [javac] warning: [options] To suppress warnings about obsolete options, use -Xlint:-options.
                    [javac] /workspace/Mockito/38f/src/org/mockito/configuration/DefaultMockitoConfiguration.java:7: warning: [deprecation] ReturnValues in org.mockito has been deprecated
                    [javac] import org.mockito.ReturnValues;
                    [javac]                   ^
                    [javac] /workspace/Mockito/38f/src/org/mockito/configuration/IMockitoConfiguration.java:7: warning: [deprecation] ReturnValues in org.mockito has been deprecated
                    [javac] import org.mockito.ReturnValues;
                    [javac]                   ^
                    [javac] /workspace/Mockito/38f/src/org/mockito/internal/configuration/GlobalConfiguration.java:7: warning: [deprecation] ReturnValues in org.mockito has been deprecated
                    [javac] import org.mockito.ReturnValues;
                    [javac]                   ^
                    [javac] /workspace/Mockito/38f/src/org/mockito/internal/runners/JUnit44RunnerImpl.java:7: warning: [deprecation] InitializationError in org.junit.internal.runners has been deprecated
                    [javac] import org.junit.internal.runners.InitializationError;
                    [javac]                                  ^
                    [javac] /workspace/Mockito/38f/src/org/mockito/internal/runners/JUnit44RunnerImpl.java:8: warning: [deprecation] JUnit4ClassRunner in org.junit.internal.runners has been deprecated
                    [javac] import org.junit.internal.runners.JUnit4ClassRunner;
                    [javac]                                  ^
                    [javac] /workspace/Mockito/38f/src/org/mockito/internal/stubbing/answers/AnswerReturnValuesAdapter.java:7: warning: [deprecation] ReturnValues in org.mockito has been deprecated
                    [javac] import org.mockito.ReturnValues;
                    [javac]                   ^
                    [javac] /workspace/Mockito/38f/src/org/mockito/internal/creation/MethodInterceptorFilter.java:50: warning: [deprecation] Integer(int) in Integer has been deprecated
                    [javac]         return new Integer(System.identityHashCode(mock));
                    [javac]                ^
                    [javac] /workspace/Mockito/38f/src/org/mockito/MockitoAnnotations.java:99: warning: [deprecation] isAccessible() in AccessibleObject has been deprecated
                    [javac]                     boolean wasAccessible = field.isAccessible();
                    [javac]                                                  ^
                    [javac] /workspace/Mockito/38f/src/org/mockito/internal/configuration/ClassPathLoader.java:27: warning: [deprecation] newInstance() in Class has been deprecated
                    [javac]             return (IMockitoConfiguration) configClass.newInstance();
                    [javac]                                                       ^
                    [javac]   where T is a type-variable:
                    [javac]     T extends Object declared in class Class
                    [javac] /workspace/Mockito/38f/src/org/mockito/internal/util/Primitives.java:29: warning: [deprecation] Character(char) in Character has been deprecated
                    [javac]         wrapperReturnValues.put(Character.class, new Character((char) 0));
                    [javac]                                                  ^
                    [javac] /workspace/Mockito/38f/src/org/mockito/internal/util/Primitives.java:30: warning: [deprecation] Byte(byte) in Byte has been deprecated
                    [javac]         wrapperReturnValues.put(Byte.class, new Byte((byte) 0));
                    [javac]                                             ^
                    [javac] /workspace/Mockito/38f/src/org/mockito/internal/util/Primitives.java:31: warning: [deprecation] Short(short) in Short has been deprecated
                    [javac]         wrapperReturnValues.put(Short.class, new Short((short) 0));
                    [javac]                                              ^
                    [javac] /workspace/Mockito/38f/src/org/mockito/internal/util/Primitives.java:32: warning: [deprecation] Integer(int) in Integer has been deprecated
                    [javac]         wrapperReturnValues.put(Integer.class, new Integer(0));
                    [javac]                                                ^
                    [javac] /workspace/Mockito/38f/src/org/mockito/internal/util/Primitives.java:33: warning: [deprecation] Long(long) in Long has been deprecated
                    [javac]         wrapperReturnValues.put(Long.class, new Long(0));
                    [javac]                                             ^
                    [javac] /workspace/Mockito/38f/src/org/mockito/internal/util/Primitives.java:34: warning: [deprecation] Float(float) in Float has been deprecated
                    [javac]         wrapperReturnValues.put(Float.class, new Float(0));
                    [javac]                                              ^
                    [javac] /workspace/Mockito/38f/src/org/mockito/internal/util/Primitives.java:35: warning: [deprecation] Double(double) in Double has been deprecated
                    [javac]         wrapperReturnValues.put(Double.class, new Double(0));
                    [javac]                                               ^
                    [javac] /workspace/Mockito/38f/src/org/mockito/internal/util/copy/AccessibilityChanger.java:29: warning: [deprecation] isAccessible() in AccessibleObject has been deprecated
                    [javac]         wasAccessible = field.isAccessible();
                    [javac]                              ^
                    [javac] 21 warnings
                
                gradle.compile.mutants:
                
                ant.compile.mutants:
                     [exec] /defects4j/framework/projects/Mockito/chooseDepedencyVersion.sh: line 8: ./gradlew: No such file or directory
                
                compile.tests:
                
                gradle.compile.tests:
                
                ant.compile.tests:
                
                clean:
                
                prepare:
                    [mkdir] Created dir: /workspace/Mockito/38f/target/classes
                    [mkdir] Created dir: /workspace/Mockito/38f/target/test-classes
                    [mkdir] Created dir: /workspace/Mockito/38f/target/reports/junit
                
                compile:
                    [javac] /workspace/Mockito/38f/build.xml:76: warning: 'includeantruntime' was not set, defaulting to build.sysclasspath=last; set to false for repeatable builds
                    [javac] Compiling 169 source files to /workspace/Mockito/38f/target/classes
                    [javac] warning: [options] bootstrap class path not set in conjunction with -source 6
                    [javac] warning: [options] source value 6 is obsolete and will be removed in a future release
                    [javac] warning: [options] target value 1.6 is obsolete and will be removed in a future release
                    [javac] warning: [options] To suppress warnings about obsolete options, use -Xlint:-options.
                    [javac] /workspace/Mockito/38f/src/org/mockito/configuration/DefaultMockitoConfiguration.java:7: warning: [deprecation] ReturnValues in org.mockito has been deprecated
                    [javac] import org.mockito.ReturnValues;
                    [javac]                   ^
                    [javac] /workspace/Mockito/38f/src/org/mockito/configuration/IMockitoConfiguration.java:7: warning: [deprecation] ReturnValues in org.mockito has been deprecated
                    [javac] import org.mockito.ReturnValues;
                    [javac]                   ^
                    [javac] /workspace/Mockito/38f/src/org/mockito/internal/configuration/GlobalConfiguration.java:7: warning: [deprecation] ReturnValues in org.mockito has been deprecated
                    [javac] import org.mockito.ReturnValues;
                    [javac]                   ^
                    [javac] /workspace/Mockito/38f/src/org/mockito/internal/runners/JUnit44RunnerImpl.java:7: warning: [deprecation] InitializationError in org.junit.internal.runners has been deprecated
                    [javac] import org.junit.internal.runners.InitializationError;
                    [javac]                                  ^
                    [javac] /workspace/Mockito/38f/src/org/mockito/internal/runners/JUnit44RunnerImpl.java:8: warning: [deprecation] JUnit4ClassRunner in org.junit.internal.runners has been deprecated
                    [javac] import org.junit.internal.runners.JUnit4ClassRunner;
                    [javac]                                  ^
                    [javac] /workspace/Mockito/38f/src/org/mockito/internal/stubbing/answers/AnswerReturnValuesAdapter.java:7: warning: [deprecation] ReturnValues in org.mockito has been deprecated
                    [javac] import org.mockito.ReturnValues;
                    [javac]                   ^
                    [javac] /workspace/Mockito/38f/src/org/mockito/internal/creation/MethodInterceptorFilter.java:50: warning: [deprecation] Integer(int) in Integer has been deprecated
                    [javac]         return new Integer(System.identityHashCode(mock));
                    [javac]                ^
                    [javac] /workspace/Mockito/38f/src/org/mockito/MockitoAnnotations.java:99: warning: [deprecation] isAccessible() in AccessibleObject has been deprecated
                    [javac]                     boolean wasAccessible = field.isAccessible();
                    [javac]                                                  ^
                    [javac] /workspace/Mockito/38f/src/org/mockito/internal/configuration/ClassPathLoader.java:27: warning: [deprecation] newInstance() in Class has been deprecated
                    [javac]             return (IMockitoConfiguration) configClass.newInstance();
                    [javac]                                                       ^
                    [javac]   where T is a type-variable:
                    [javac]     T extends Object declared in class Class
                    [javac] /workspace/Mockito/38f/src/org/mockito/internal/util/Primitives.java:29: warning: [deprecation] Character(char) in Character has been deprecated
                    [javac]         wrapperReturnValues.put(Character.class, new Character((char) 0));
                    [javac]                                                  ^
                    [javac] /workspace/Mockito/38f/src/org/mockito/internal/util/Primitives.java:30: warning: [deprecation] Byte(byte) in Byte has been deprecated
                    [javac]         wrapperReturnValues.put(Byte.class, new Byte((byte) 0));
                    [javac]                                             ^
                    [javac] /workspace/Mockito/38f/src/org/mockito/internal/util/Primitives.java:31: warning: [deprecation] Short(short) in Short has been deprecated
                    [javac]         wrapperReturnValues.put(Short.class, new Short((short) 0));
                    [javac]                                              ^
                    [javac] /workspace/Mockito/38f/src/org/mockito/internal/util/Primitives.java:32: warning: [deprecation] Integer(int) in Integer has been deprecated
                    [javac]         wrapperReturnValues.put(Integer.class, new Integer(0));
                    [javac]                                                ^
                    [javac] /workspace/Mockito/38f/src/org/mockito/internal/util/Primitives.java:33: warning: [deprecation] Long(long) in Long has been deprecated
                    [javac]         wrapperReturnValues.put(Long.class, new Long(0));
                    [javac]                                             ^
                    [javac] /workspace/Mockito/38f/src/org/mockito/internal/util/Primitives.java:34: warning: [deprecation] Float(float) in Float has been deprecated
                    [javac]         wrapperReturnValues.put(Float.class, new Float(0));
                    [javac]                                              ^
                    [javac] /workspace/Mockito/38f/src/org/mockito/internal/util/Primitives.java:35: warning: [deprecation] Double(double) in Double has been deprecated
                    [javac]         wrapperReturnValues.put(Double.class, new Double(0));
                    [javac]                                               ^
                    [javac] /workspace/Mockito/38f/src/org/mockito/internal/util/copy/AccessibilityChanger.java:29: warning: [deprecation] isAccessible() in AccessibleObject has been deprecated
                    [javac]         wasAccessible = field.isAccessible();
                    [javac]                              ^
                    [javac] 21 warnings
                
                compile.test:
                    [javac] /workspace/Mockito/38f/build.xml:82: warning: 'includeantruntime' was not set, defaulting to build.sysclasspath=last; set to false for repeatable builds
                    [javac] Compiling 162 source files to /workspace/Mockito/38f/target/test-classes
                    [javac] warning: [options] bootstrap class path not set in conjunction with -source 6
                    [javac] warning: [options] source value 6 is obsolete and will be removed in a future release
                    [javac] warning: [options] target value 1.6 is obsolete and will be removed in a future release
                    [javac] warning: [options] To suppress warnings about obsolete options, use -Xlint:-options.
                    [javac] /workspace/Mockito/38f/test/org/mockitousage/junitrunner/JUnit44RunnerTest.java:14: warning: [deprecation] MockitoJUnit44Runner in org.mockito.runners has been deprecated
                    [javac] import org.mockito.runners.MockitoJUnit44Runner;
                    [javac]                           ^
                    [javac] /workspace/Mockito/38f/test/org/mockitousage/stacktrace/PointingStackTraceToActualInvocationTest.java:14: warning: [deprecation] MockitoJUnit44Runner in org.mockito.runners has been deprecated
                    [javac] import org.mockito.runners.MockitoJUnit44Runner;
                    [javac]                           ^
                    [javac] /workspace/Mockito/38f/test/org/mockitoutil/ExtraMatchers.java:102: error: cannot find symbol
                    [javac]                 boolean containsSublist = Collections.indexOfSubList((List<?>) value, Arrays.asList(elements)) != -1;
                    [javac]                                                                       ^
                    [javac]   symbol: class List
                    [javac] /workspace/Mockito/38f/test/org/mockito/internal/matchers/EqualsTest.java:15: warning: [deprecation] Integer(int) in Integer has been deprecated
                    [javac]         assertEquals(new Equals(new Integer(2)), new Equals(new Integer(2)));
                    [javac]                                 ^
                    [javac] /workspace/Mockito/38f/test/org/mockito/internal/matchers/EqualsTest.java:15: warning: [deprecation] Integer(int) in Integer has been deprecated
                    [javac]         assertEquals(new Equals(new Integer(2)), new Equals(new Integer(2)));
                    [javac]                                                             ^
                    [javac] /workspace/Mockito/38f/test/org/mockito/internal/matchers/apachecommons/EqualsBuilderTest.java:967: warning: [deprecation] Integer(int) in Integer has been deprecated
                    [javac]         Object[] x1 = new Object[] { new Integer(1), null, new Integer(3) };
                    [javac]                                      ^
                    [javac] /workspace/Mockito/38f/test/org/mockito/internal/matchers/apachecommons/EqualsBuilderTest.java:967: warning: [deprecation] Integer(int) in Integer has been deprecated
                    [javac]         Object[] x1 = new Object[] { new Integer(1), null, new Integer(3) };
                    [javac]                                                            ^
                    [javac] /workspace/Mockito/38f/test/org/mockito/internal/matchers/apachecommons/EqualsBuilderTest.java:968: warning: [deprecation] Integer(int) in Integer has been deprecated
                    [javac]         Object[] x2 = new Object[] { new Integer(1), new Integer(2), new Integer(3) };
                    [javac]                                      ^
                    [javac] /workspace/Mockito/38f/test/org/mockito/internal/matchers/apachecommons/EqualsBuilderTest.java:968: warning: [deprecation] Integer(int) in Integer has been deprecated
                    [javac]         Object[] x2 = new Object[] { new Integer(1), new Integer(2), new Integer(3) };
                    [javac]                                                      ^
                    [javac] /workspace/Mockito/38f/test/org/mockito/internal/matchers/apachecommons/EqualsBuilderTest.java:968: warning: [deprecation] Integer(int) in Integer has been deprecated
                    [javac]         Object[] x2 = new Object[] { new Integer(1), new Integer(2), new Integer(3) };
                    [javac]                                                                      ^
                    [javac] /workspace/Mockito/38f/test/org/mockito/internal/stubbing/answers/AnswersValidatorTest.java:64: warning: [deprecation] Boolean(boolean) in Boolean has been deprecated
                    [javac]         validator.validate(new Returns(new Boolean(true)), new InvocationBuilder().method("booleanObjectReturningMethod").toInvocation());
                    [javac]                                        ^
                    [javac] /workspace/Mockito/38f/test/org/mockito/internal/util/copy/LenientCopyToolTest.java:133: warning: [deprecation] isAccessible() in AccessibleObject has been deprecated
                    [javac]         assertFalse(privateField.isAccessible());
                    [javac]                                 ^
                    [javac] /workspace/Mockito/38f/test/org/mockito/internal/util/copy/LenientCopyToolTest.java:140: warning: [deprecation] isAccessible() in AccessibleObject has been deprecated
                    [javac]         assertFalse(privateField.isAccessible());
                    [javac]                                 ^
                    [javac] /workspace/Mockito/38f/test/org/mockito/internal/verification/argumentmatching/ArgumentMatchingToolTest.java:50: warning: [deprecation] Long(long) in Long has been deprecated
                    [javac]         Long longPretendingAnInt = new Long(20);
                    [javac]                                    ^
                    [javac] /workspace/Mockito/38f/test/org/mockito/internal/verification/argumentmatching/ArgumentMatchingToolTest.java:58: warning: [deprecation] Integer(int) in Integer has been deprecated
                    [javac]         assertEquals(new Integer(1), suspicious[0]);
                    [javac]                      ^
                    [javac] /workspace/Mockito/38f/test/org/mockitousage/PlaygroundTest.java:38: warning: [deprecation] Long(long) in Long has been deprecated
                    [javac]         verify(boo).withLong(new Long(100));
                    [javac]                              ^
                    [javac] /workspace/Mockito/38f/test/org/mockitousage/basicapi/UsingVarargsTest.java:94: warning: [deprecation] Integer(int) in Integer has been deprecated
                    [javac]         mock.withObjectVarargs(2, "1", new ArrayList<Object>(), new Integer(1));
                    [javac]                                                                 ^
                    [javac] /workspace/Mockito/38f/test/org/mockitousage/basicapi/UsingVarargsTest.java:95: warning: [deprecation] Integer(int) in Integer has been deprecated
                    [javac]         mock.withObjectVarargs(3, new Integer(1));
                    [javac]                                   ^
                    [javac] /workspace/Mockito/38f/test/org/mockitousage/basicapi/UsingVarargsTest.java:98: warning: [deprecation] Integer(int) in Integer has been deprecated
                    [javac]         verify(mock).withObjectVarargs(2, "1", new ArrayList<Object>(), new Integer(1));
                    [javac]                                                                         ^
                    [javac] /workspace/Mockito/38f/test/org/mockitousage/basicapi/UsingVarargsTest.java:149: warning: non-varargs call of varargs method with inexact argument type for last parameter;
                    [javac]         when(mixedVarargs.doSomething("hello", null)).thenReturn("hello");
                    [javac]                                                ^
                    [javac]   cast to String for a varargs call
                    [javac]   cast to String[] for a non-varargs call and to suppress this warning
                    [javac] /workspace/Mockito/38f/test/org/mockitousage/basicapi/UsingVarargsTest.java:150: warning: non-varargs call of varargs method with inexact argument type for last parameter;
                    [javac]         when(mixedVarargs.doSomething("goodbye", null)).thenReturn("goodbye");
                    [javac]                                                  ^
                    [javac]   cast to String for a varargs call
                    [javac]   cast to String[] for a non-varargs call and to suppress this warning
                    [javac] /workspace/Mockito/38f/test/org/mockitousage/basicapi/UsingVarargsTest.java:152: warning: non-varargs call of varargs method with inexact argument type for last parameter;
                    [javac]         String result = mixedVarargs.doSomething("hello", null);
                    [javac]                                                           ^
                    [javac]   cast to String for a varargs call
                    [javac]   cast to String[] for a non-varargs call and to suppress this warning
                    [javac] /workspace/Mockito/38f/test/org/mockitousage/basicapi/UsingVarargsTest.java:155: warning: non-varargs call of varargs method with inexact argument type for last parameter;
                    [javac]         verify(mixedVarargs).doSomething("hello", null);
                    [javac]                                                   ^
                    [javac]   cast to String for a varargs call
                    [javac]   cast to String[] for a non-varargs call and to suppress this warning
                    [javac] /workspace/Mockito/38f/test/org/mockitousage/basicapi/UsingVarargsTest.java:162: warning: non-varargs call of varargs method with inexact argument type for last parameter;
                    [javac]         when(mixedVarargs.doSomething("one", "two", null)).thenReturn("hello");
                    [javac]                                                     ^
                    [javac]   cast to String for a varargs call
                    [javac]   cast to String[] for a non-varargs call and to suppress this warning
                    [javac] /workspace/Mockito/38f/test/org/mockitousage/basicapi/UsingVarargsTest.java:163: warning: non-varargs call of varargs method with inexact argument type for last parameter;
                    [javac]         when(mixedVarargs.doSomething("1", "2", null)).thenReturn("goodbye");
                    [javac]                                                 ^
                    [javac]   cast to String for a varargs call
                    [javac]   cast to String[] for a non-varargs call and to suppress this warning
                    [javac] /workspace/Mockito/38f/test/org/mockitousage/basicapi/UsingVarargsTest.java:165: warning: non-varargs call of varargs method with inexact argument type for last parameter;
                    [javac]         String result = mixedVarargs.doSomething("one", "two", null);
                    [javac]                                                                ^
                    [javac]   cast to String for a varargs call
                    [javac]   cast to String[] for a non-varargs call and to suppress this warning
                    [javac] /workspace/Mockito/38f/test/org/mockitousage/bugs/NPEWithIsAClassMatcherTest.java:42: warning: [deprecation] Integer(int) in Integer has been deprecated
                    [javac]         verify(mock).intArgumentMethod(eq(new Integer(100)));
                    [javac]                                           ^
                    [javac] /workspace/Mockito/38f/test/org/mockitousage/matchers/MatchersTest.java:301: warning: [deprecation] Integer(int) in Integer has been deprecated
                    [javac]         when(mock.oneArray(aryEq(new Object[] { "Test", new Integer(4) }))).thenReturn("9");
                    [javac]                                                         ^
                    [javac] /workspace/Mockito/38f/test/org/mockitousage/matchers/MatchersTest.java:312: warning: [deprecation] Integer(int) in Integer has been deprecated
                    [javac]         assertEquals("9", mock.oneArray(new Object[] { "Test", new Integer(4) }));
                    [javac]                                                                ^
                    [javac] /workspace/Mockito/38f/test/org/mockitousage/matchers/MatchersTest.java:314: warning: [deprecation] Integer(int) in Integer has been deprecated
                    [javac]         assertEquals(null, mock.oneArray(new Object[] { "Test", new Integer(999) }));
                    [javac]                                                                 ^
                    [javac] /workspace/Mockito/38f/test/org/mockitousage/matchers/MatchersTest.java:315: warning: [deprecation] Integer(int) in Integer has been deprecated
                    [javac]         assertEquals(null, mock.oneArray(new Object[] { "Test", new Integer(4), "x" }));
                    [javac]                                                                 ^
                    [javac] /workspace/Mockito/38f/test/org/mockitousage/misuse/DescriptiveMessagesOnMisuseTest.java:96: warning: non-varargs call of varargs method with inexact argument type for last parameter;
                    [javac]         verifyNoMoreInteractions(null);
                    [javac]                                  ^
                    [javac]   cast to Object for a varargs call
                    [javac]   cast to Object[] for a non-varargs call and to suppress this warning
                    [javac] /workspace/Mockito/38f/test/org/mockitousage/misuse/InvalidUsageTest.java:68: warning: non-varargs call of varargs method with inexact argument type for last parameter;
                    [javac]         when(mock.simpleMethod()).thenThrow(null);
                    [javac]                                             ^
                    [javac]   cast to Throwable for a varargs call
                    [javac]   cast to Throwable[] for a non-varargs call and to suppress this warning
                    [javac] /workspace/Mockito/38f/test/org/mockitousage/stubbing/ReturningDefaultValuesTest.java:37: warning: [deprecation] Byte(byte) in Byte has been deprecated
                    [javac]         assertEquals(new Byte((byte) 0), mock.byteObjectReturningMethod());
                    [javac]                      ^
                    [javac] /workspace/Mockito/38f/test/org/mockitousage/stubbing/ReturningDefaultValuesTest.java:38: warning: [deprecation] Short(short) in Short has been deprecated
                    [javac]         assertEquals(new Short((short) 0), mock.shortObjectReturningMethod());
                    [javac]                      ^
                    [javac] /workspace/Mockito/38f/test/org/mockitousage/stubbing/ReturningDefaultValuesTest.java:39: warning: [deprecation] Integer(int) in Integer has been deprecated
                    [javac]         assertEquals(new Integer(0), mock.integerReturningMethod());
                    [javac]                      ^
                    [javac] /workspace/Mockito/38f/test/org/mockitousage/stubbing/ReturningDefaultValuesTest.java:40: warning: [deprecation] Long(long) in Long has been deprecated
                    [javac]         assertEquals(new Long(0L), mock.longObjectReturningMethod());
                    [javac]                      ^
                    [javac] /workspace/Mockito/38f/test/org/mockitousage/stubbing/ReturningDefaultValuesTest.java:41: warning: [deprecation] Float(float) in Float has been deprecated
                    [javac]         assertEquals(new Float(0.0F), mock.floatObjectReturningMethod(), 0.0F);
                    [javac]                      ^
                    [javac] /workspace/Mockito/38f/test/org/mockitousage/stubbing/ReturningDefaultValuesTest.java:42: warning: [deprecation] Double(double) in Double has been deprecated
                    [javac]         assertEquals(new Double(0.0D), mock.doubleObjectReturningMethod(), 0.0D);
                    [javac]                      ^
                    [javac] /workspace/Mockito/38f/test/org/mockitousage/stubbing/ReturningDefaultValuesTest.java:43: warning: [deprecation] Character(char) in Character has been deprecated
                    [javac]         assertEquals(new Character((char) 0), mock.charObjectReturningMethod());
                    [javac]                      ^
                    [javac] /workspace/Mockito/38f/test/org/mockitousage/stubbing/ReturningDefaultValuesTest.java:44: warning: [deprecation] Boolean(boolean) in Boolean has been deprecated
                    [javac]         assertEquals(new Boolean(false), mock.booleanObjectReturningMethod());
                    [javac]                      ^
                    [javac] /workspace/Mockito/38f/test/org/mockitousage/stubbing/StubbingConsecutiveAnswersTest.java:37: warning: non-varargs call of varargs method with inexact argument type for last parameter;
                    [javac]         when(mock.simpleMethod()).thenReturn(null, null);
                    [javac]                                                    ^
                    [javac]   cast to String for a varargs call
                    [javac]   cast to String[] for a non-varargs call and to suppress this warning
                    [javac] /workspace/Mockito/38f/test/org/mockitousage/stubbing/StubbingUsingDoReturnTest.java:224: warning: [deprecation] Integer(int) in Integer has been deprecated
                    [javac]         doReturn(new Integer(2)).when(mock).intReturningMethod();
                    [javac]                  ^
                    [javac] /workspace/Mockito/38f/test/org/mockitousage/stubbing/StubbingWithThrowablesTest.java:51: warning: [deprecation] <T>stubVoid(T) in Mockito has been deprecated
                    [javac]         stubVoid(mock).toThrow(expected).on().clear();
                    [javac]         ^
                    [javac]   where T is a type-variable:
                    [javac]     T extends Object declared in method <T>stubVoid(T)
                    [javac] /workspace/Mockito/38f/test/org/mockitousage/stubbing/StubbingWithThrowablesTest.java:62: warning: [deprecation] <T>stubVoid(T) in Mockito has been deprecated
                    [javac]         stubVoid(mock).toThrow(new ExceptionOne()).on().clear();
                    [javac]         ^
                    [javac]   where T is a type-variable:
                    [javac]     T extends Object declared in method <T>stubVoid(T)
                    [javac] /workspace/Mockito/38f/test/org/mockitousage/stubbing/StubbingWithThrowablesTest.java:63: warning: [deprecation] <T>stubVoid(T) in Mockito has been deprecated
                    [javac]         stubVoid(mock).toThrow(new ExceptionTwo()).on().clear();
                    [javac]         ^
                    [javac]   where T is a type-variable:
                    [javac]     T extends Object declared in method <T>stubVoid(T)
                    [javac] /workspace/Mockito/38f/test/org/mockitousage/stubbing/StubbingWithThrowablesTest.java:117: warning: non-varargs call of varargs method with inexact argument type for last parameter;
                    [javac]         when(mock.add("monkey island")).thenThrow(null);
                    [javac]                                                   ^
                    [javac]   cast to Throwable for a varargs call
                    [javac]   cast to Throwable[] for a non-varargs call and to suppress this warning
                    [javac] /workspace/Mockito/38f/test/org/mockitousage/stubbing/StubbingWithThrowablesTest.java:124: warning: [deprecation] <T>stubVoid(T) in Mockito has been deprecated
                    [javac]         stubVoid(mock).toThrow(new ExceptionTwo()).on().clear();
                    [javac]         ^
                    [javac]   where T is a type-variable:
                    [javac]     T extends Object declared in method <T>stubVoid(T)
                    [javac] /workspace/Mockito/38f/test/org/mockitousage/stubbing/StubbingWithThrowablesTest.java:126: warning: [deprecation] <T>stubVoid(T) in Mockito has been deprecated
                    [javac]         stubVoid(mockTwo).toThrow(new ExceptionThree()).on().clear();
                    [javac]         ^
                    [javac]   where T is a type-variable:
                    [javac]     T extends Object declared in method <T>stubVoid(T)
                    [javac] /workspace/Mockito/38f/test/org/mockitousage/stubbing/StubbingWithThrowablesTest.java:157: warning: [deprecation] <T>stubVoid(T) in Mockito has been deprecated
                    [javac]         stubVoid(mock).toThrow(new RuntimeException()).on().clone();
                    [javac]         ^
                    [javac]   where T is a type-variable:
                    [javac]     T extends Object declared in method <T>stubVoid(T)
                    [javac] /workspace/Mockito/38f/test/org/mockitousage/stubbing/StubbingWithThrowablesTest.java:177: warning: [deprecation] <T>stubVoid(T) in Mockito has been deprecated
                    [javac]         stubVoid(mock).toThrow(new RuntimeException()).on().clone();
                    [javac]         ^
                    [javac]   where T is a type-variable:
                    [javac]     T extends Object declared in method <T>stubVoid(T)
                    [javac] /workspace/Mockito/38f/test/org/mockitousage/verification/BasicVerificationInOrderTest.java:253: warning: non-varargs call of varargs method with inexact argument type for last parameter;
                    [javac]         inOrder(null);
                    [javac]                 ^
                    [javac]   cast to Object for a varargs call
                    [javac]   cast to Object[] for a non-varargs call and to suppress this warning
                    [javac] /workspace/Mockito/38f/test/org/mockitousage/verification/NoMoreInteractionsVerificationTest.java:102: warning: non-varargs call of varargs method with inexact argument type for last parameter;
                    [javac]         verifyNoMoreInteractions(null);
                    [javac]                                  ^
                    [javac]   cast to Object for a varargs call
                    [javac]   cast to Object[] for a non-varargs call and to suppress this warning
                    [javac] 1 error
                    [javac] 56 warnings
                
                BUILD FAILED
                /defects4j/framework/projects/Mockito/Mockito.build.xml:162: The following error occurred while executing this line:
                /defects4j/framework/projects/Mockito/Mockito.build.xml:70: The following error occurred while executing this line:
                /workspace/Mockito/38f/build.xml:82: Compile failed; see the compiler error output for details.
                
                Total time: 1 second
                Cannot compile tests! at /defects4j/framework/bin/d4j/d4j-compile line 83.
                Compilation failed in require at /defects4j/framework/bin/defects4j line 195.
                """;

        final var expected = """
                /workspace/Mockito/38f/build.xml:82: warning: 'includeantruntime' was not set, defaulting to build.sysclasspath=last; set to false for repeatable builds
                Compiling 162 source files to /workspace/Mockito/38f/target/test-classes
                warning: [options] bootstrap class path not set in conjunction with -source 6
                warning: [options] source value 6 is obsolete and will be removed in a future release
                warning: [options] target value 1.6 is obsolete and will be removed in a future release
                warning: [options] To suppress warnings about obsolete options, use -Xlint:-options.
                /workspace/Mockito/38f/test/org/mockitousage/junitrunner/JUnit44RunnerTest.java:14: warning: [deprecation] MockitoJUnit44Runner in org.mockito.runners has been deprecated
                import org.mockito.runners.MockitoJUnit44Runner;
                                          ^
                /workspace/Mockito/38f/test/org/mockitousage/stacktrace/PointingStackTraceToActualInvocationTest.java:14: warning: [deprecation] MockitoJUnit44Runner in org.mockito.runners has been deprecated
                import org.mockito.runners.MockitoJUnit44Runner;
                                          ^
                /workspace/Mockito/38f/test/org/mockitoutil/ExtraMatchers.java:102: error: cannot find symbol
                                boolean containsSublist = Collections.indexOfSubList((List<?>) value, Arrays.asList(elements)) != -1;
                                                                                      ^
                  symbol: class List
                /workspace/Mockito/38f/test/org/mockito/internal/matchers/EqualsTest.java:15: warning: [deprecation] Integer(int) in Integer has been deprecated
                        assertEquals(new Equals(new Integer(2)), new Equals(new Integer(2)));
                                                ^
                /workspace/Mockito/38f/test/org/mockito/internal/matchers/EqualsTest.java:15: warning: [deprecation] Integer(int) in Integer has been deprecated
                        assertEquals(new Equals(new Integer(2)), new Equals(new Integer(2)));
                                                                            ^
                /workspace/Mockito/38f/test/org/mockito/internal/matchers/apachecommons/EqualsBuilderTest.java:967: warning: [deprecation] Integer(int) in Integer has been deprecated
                        Object[] x1 = new Object[] { new Integer(1), null, new Integer(3) };
                                                     ^
                /workspace/Mockito/38f/test/org/mockito/internal/matchers/apachecommons/EqualsBuilderTest.java:967: warning: [deprecation] Integer(int) in Integer has been deprecated
                        Object[] x1 = new Object[] { new Integer(1), null, new Integer(3) };
                                                                           ^
                /workspace/Mockito/38f/test/org/mockito/internal/matchers/apachecommons/EqualsBuilderTest.java:968: warning: [deprecation] Integer(int) in Integer has been deprecated
                        Object[] x2 = new Object[] { new Integer(1), new Integer(2), new Integer(3) };
                                                     ^
                /workspace/Mockito/38f/test/org/mockito/internal/matchers/apachecommons/EqualsBuilderTest.java:968: warning: [deprecation] Integer(int) in Integer has been deprecated
                        Object[] x2 = new Object[] { new Integer(1), new Integer(2), new Integer(3) };
                                                                     ^
                /workspace/Mockito/38f/test/org/mockito/internal/matchers/apachecommons/EqualsBuilderTest.java:968: warning: [deprecation] Integer(int) in Integer has been deprecated
                        Object[] x2 = new Object[] { new Integer(1), new Integer(2), new Integer(3) };
                                                                                     ^
                /workspace/Mockito/38f/test/org/mockito/internal/stubbing/answers/AnswersValidatorTest.java:64: warning: [deprecation] Boolean(boolean) in Boolean has been deprecated
                        validator.validate(new Returns(new Boolean(true)), new InvocationBuilder().method("booleanObjectReturningMethod").toInvocation());
                                                       ^
                /workspace/Mockito/38f/test/org/mockito/internal/util/copy/LenientCopyToolTest.java:133: warning: [deprecation] isAccessible() in AccessibleObject has been deprecated
                        assertFalse(privateField.isAccessible());
                                                ^
                /workspace/Mockito/38f/test/org/mockito/internal/util/copy/LenientCopyToolTest.java:140: warning: [deprecation] isAccessible() in AccessibleObject has been deprecated
                        assertFalse(privateField.isAccessible());
                                                ^
                /workspace/Mockito/38f/test/org/mockito/internal/verification/argumentmatching/ArgumentMatchingToolTest.java:50: warning: [deprecation] Long(long) in Long has been deprecated
                        Long longPretendingAnInt = new Long(20);
                                                   ^
                /workspace/Mockito/38f/test/org/mockito/internal/verification/argumentmatching/ArgumentMatchingToolTest.java:58: warning: [deprecation] Integer(int) in Integer has been deprecated
                        assertEquals(new Integer(1), suspicious[0]);
                                     ^
                /workspace/Mockito/38f/test/org/mockitousage/PlaygroundTest.java:38: warning: [deprecation] Long(long) in Long has been deprecated
                        verify(boo).withLong(new Long(100));
                                             ^
                /workspace/Mockito/38f/test/org/mockitousage/basicapi/UsingVarargsTest.java:94: warning: [deprecation] Integer(int) in Integer has been deprecated
                        mock.withObjectVarargs(2, "1", new ArrayList<Object>(), new Integer(1));
                                                                                ^
                /workspace/Mockito/38f/test/org/mockitousage/basicapi/UsingVarargsTest.java:95: warning: [deprecation] Integer(int) in Integer has been deprecated
                        mock.withObjectVarargs(3, new Integer(1));
                                                  ^
                /workspace/Mockito/38f/test/org/mockitousage/basicapi/UsingVarargsTest.java:98: warning: [deprecation] Integer(int) in Integer has been deprecated
                        verify(mock).withObjectVarargs(2, "1", new ArrayList<Object>(), new Integer(1));
                                                                                        ^
                /workspace/Mockito/38f/test/org/mockitousage/basicapi/UsingVarargsTest.java:149: warning: non-varargs call of varargs method with inexact argument type for last parameter;
                        when(mixedVarargs.doSomething("hello", null)).thenReturn("hello");
                                                               ^
                  cast to String for a varargs call
                  cast to String[] for a non-varargs call and to suppress this warning
                /workspace/Mockito/38f/test/org/mockitousage/basicapi/UsingVarargsTest.java:150: warning: non-varargs call of varargs method with inexact argument type for last parameter;
                        when(mixedVarargs.doSomething("goodbye", null)).thenReturn("goodbye");
                                                                 ^
                  cast to String for a varargs call
                  cast to String[] for a non-varargs call and to suppress this warning
                /workspace/Mockito/38f/test/org/mockitousage/basicapi/UsingVarargsTest.java:152: warning: non-varargs call of varargs method with inexact argument type for last parameter;
                        String result = mixedVarargs.doSomething("hello", null);
                                                                          ^
                  cast to String for a varargs call
                  cast to String[] for a non-varargs call and to suppress this warning
                /workspace/Mockito/38f/test/org/mockitousage/basicapi/UsingVarargsTest.java:155: warning: non-varargs call of varargs method with inexact argument type for last parameter;
                        verify(mixedVarargs).doSomething("hello", null);
                                                                  ^
                  cast to String for a varargs call
                  cast to String[] for a non-varargs call and to suppress this warning
                /workspace/Mockito/38f/test/org/mockitousage/basicapi/UsingVarargsTest.java:162: warning: non-varargs call of varargs method with inexact argument type for last parameter;
                        when(mixedVarargs.doSomething("one", "two", null)).thenReturn("hello");
                                                                    ^
                  cast to String for a varargs call
                  cast to String[] for a non-varargs call and to suppress this warning
                /workspace/Mockito/38f/test/org/mockitousage/basicapi/UsingVarargsTest.java:163: warning: non-varargs call of varargs method with inexact argument type for last parameter;
                        when(mixedVarargs.doSomething("1", "2", null)).thenReturn("goodbye");
                                                                ^
                  cast to String for a varargs call
                  cast to String[] for a non-varargs call and to suppress this warning
                /workspace/Mockito/38f/test/org/mockitousage/basicapi/UsingVarargsTest.java:165: warning: non-varargs call of varargs method with inexact argument type for last parameter;
                        String result = mixedVarargs.doSomething("one", "two", null);
                                                                               ^
                  cast to String for a varargs call
                  cast to String[] for a non-varargs call and to suppress this warning
                /workspace/Mockito/38f/test/org/mockitousage/bugs/NPEWithIsAClassMatcherTest.java:42: warning: [deprecation] Integer(int) in Integer has been deprecated
                        verify(mock).intArgumentMethod(eq(new Integer(100)));
                                                          ^
                /workspace/Mockito/38f/test/org/mockitousage/matchers/MatchersTest.java:301: warning: [deprecation] Integer(int) in Integer has been deprecated
                        when(mock.oneArray(aryEq(new Object[] { "Test", new Integer(4) }))).thenReturn("9");
                                                                        ^
                /workspace/Mockito/38f/test/org/mockitousage/matchers/MatchersTest.java:312: warning: [deprecation] Integer(int) in Integer has been deprecated
                        assertEquals("9", mock.oneArray(new Object[] { "Test", new Integer(4) }));
                                                                               ^
                /workspace/Mockito/38f/test/org/mockitousage/matchers/MatchersTest.java:314: warning: [deprecation] Integer(int) in Integer has been deprecated
                        assertEquals(null, mock.oneArray(new Object[] { "Test", new Integer(999) }));
                                                                                ^
                /workspace/Mockito/38f/test/org/mockitousage/matchers/MatchersTest.java:315: warning: [deprecation] Integer(int) in Integer has been deprecated
                        assertEquals(null, mock.oneArray(new Object[] { "Test", new Integer(4), "x" }));
                                                                                ^
                /workspace/Mockito/38f/test/org/mockitousage/misuse/DescriptiveMessagesOnMisuseTest.java:96: warning: non-varargs call of varargs method with inexact argument type for last parameter;
                        verifyNoMoreInteractions(null);
                                                 ^
                  cast to Object for a varargs call
                  cast to Object[] for a non-varargs call and to suppress this warning
                /workspace/Mockito/38f/test/org/mockitousage/misuse/InvalidUsageTest.java:68: warning: non-varargs call of varargs method with inexact argument type for last parameter;
                        when(mock.simpleMethod()).thenThrow(null);
                                                            ^
                  cast to Throwable for a varargs call
                  cast to Throwable[] for a non-varargs call and to suppress this warning
                /workspace/Mockito/38f/test/org/mockitousage/stubbing/ReturningDefaultValuesTest.java:37: warning: [deprecation] Byte(byte) in Byte has been deprecated
                        assertEquals(new Byte((byte) 0), mock.byteObjectReturningMethod());
                                     ^
                /workspace/Mockito/38f/test/org/mockitousage/stubbing/ReturningDefaultValuesTest.java:38: warning: [deprecation] Short(short) in Short has been deprecated
                        assertEquals(new Short((short) 0), mock.shortObjectReturningMethod());
                                     ^
                /workspace/Mockito/38f/test/org/mockitousage/stubbing/ReturningDefaultValuesTest.java:39: warning: [deprecation] Integer(int) in Integer has been deprecated
                        assertEquals(new Integer(0), mock.integerReturningMethod());
                                     ^
                /workspace/Mockito/38f/test/org/mockitousage/stubbing/ReturningDefaultValuesTest.java:40: warning: [deprecation] Long(long) in Long has been deprecated
                        assertEquals(new Long(0L), mock.longObjectReturningMethod());
                                     ^
                /workspace/Mockito/38f/test/org/mockitousage/stubbing/ReturningDefaultValuesTest.java:41: warning: [deprecation] Float(float) in Float has been deprecated
                        assertEquals(new Float(0.0F), mock.floatObjectReturningMethod(), 0.0F);
                                     ^
                /workspace/Mockito/38f/test/org/mockitousage/stubbing/ReturningDefaultValuesTest.java:42: warning: [deprecation] Double(double) in Double has been deprecated
                        assertEquals(new Double(0.0D), mock.doubleObjectReturningMethod(), 0.0D);
                                     ^
                /workspace/Mockito/38f/test/org/mockitousage/stubbing/ReturningDefaultValuesTest.java:43: warning: [deprecation] Character(char) in Character has been deprecated
                        assertEquals(new Character((char) 0), mock.charObjectReturningMethod());
                                     ^
                /workspace/Mockito/38f/test/org/mockitousage/stubbing/ReturningDefaultValuesTest.java:44: warning: [deprecation] Boolean(boolean) in Boolean has been deprecated
                        assertEquals(new Boolean(false), mock.booleanObjectReturningMethod());
                                     ^
                /workspace/Mockito/38f/test/org/mockitousage/stubbing/StubbingConsecutiveAnswersTest.java:37: warning: non-varargs call of varargs method with inexact argument type for last parameter;
                        when(mock.simpleMethod()).thenReturn(null, null);
                                                                   ^
                  cast to String for a varargs call
                  cast to String[] for a non-varargs call and to suppress this warning
                /workspace/Mockito/38f/test/org/mockitousage/stubbing/StubbingUsingDoReturnTest.java:224: warning: [deprecation] Integer(int) in Integer has been deprecated
                        doReturn(new Integer(2)).when(mock).intReturningMethod();
                                 ^
                /workspace/Mockito/38f/test/org/mockitousage/stubbing/StubbingWithThrowablesTest.java:51: warning: [deprecation] <T>stubVoid(T) in Mockito has been deprecated
                        stubVoid(mock).toThrow(expected).on().clear();
                        ^
                  where T is a type-variable:
                    T extends Object declared in method <T>stubVoid(T)
                /workspace/Mockito/38f/test/org/mockitousage/stubbing/StubbingWithThrowablesTest.java:62: warning: [deprecation] <T>stubVoid(T) in Mockito has been deprecated
                        stubVoid(mock).toThrow(new ExceptionOne()).on().clear();
                        ^
                  where T is a type-variable:
                    T extends Object declared in method <T>stubVoid(T)
                /workspace/Mockito/38f/test/org/mockitousage/stubbing/StubbingWithThrowablesTest.java:63: warning: [deprecation] <T>stubVoid(T) in Mockito has been deprecated
                        stubVoid(mock).toThrow(new ExceptionTwo()).on().clear();
                        ^
                  where T is a type-variable:
                    T extends Object declared in method <T>stubVoid(T)
                /workspace/Mockito/38f/test/org/mockitousage/stubbing/StubbingWithThrowablesTest.java:117: warning: non-varargs call of varargs method with inexact argument type for last parameter;
                        when(mock.add("monkey island")).thenThrow(null);
                                                                  ^
                  cast to Throwable for a varargs call
                  cast to Throwable[] for a non-varargs call and to suppress this warning
                /workspace/Mockito/38f/test/org/mockitousage/stubbing/StubbingWithThrowablesTest.java:124: warning: [deprecation] <T>stubVoid(T) in Mockito has been deprecated
                        stubVoid(mock).toThrow(new ExceptionTwo()).on().clear();
                        ^
                  where T is a type-variable:
                    T extends Object declared in method <T>stubVoid(T)
                /workspace/Mockito/38f/test/org/mockitousage/stubbing/StubbingWithThrowablesTest.java:126: warning: [deprecation] <T>stubVoid(T) in Mockito has been deprecated
                        stubVoid(mockTwo).toThrow(new ExceptionThree()).on().clear();
                        ^
                  where T is a type-variable:
                    T extends Object declared in method <T>stubVoid(T)
                /workspace/Mockito/38f/test/org/mockitousage/stubbing/StubbingWithThrowablesTest.java:157: warning: [deprecation] <T>stubVoid(T) in Mockito has been deprecated
                        stubVoid(mock).toThrow(new RuntimeException()).on().clone();
                        ^
                  where T is a type-variable:
                    T extends Object declared in method <T>stubVoid(T)
                /workspace/Mockito/38f/test/org/mockitousage/stubbing/StubbingWithThrowablesTest.java:177: warning: [deprecation] <T>stubVoid(T) in Mockito has been deprecated
                        stubVoid(mock).toThrow(new RuntimeException()).on().clone();
                        ^
                  where T is a type-variable:
                    T extends Object declared in method <T>stubVoid(T)
                /workspace/Mockito/38f/test/org/mockitousage/verification/BasicVerificationInOrderTest.java:253: warning: non-varargs call of varargs method with inexact argument type for last parameter;
                        inOrder(null);
                                ^
                  cast to Object for a varargs call
                  cast to Object[] for a non-varargs call and to suppress this warning
                /workspace/Mockito/38f/test/org/mockitousage/verification/NoMoreInteractionsVerificationTest.java:102: warning: non-varargs call of varargs method with inexact argument type for last parameter;
                        verifyNoMoreInteractions(null);
                                                 ^
                  cast to Object for a varargs call
                  cast to Object[] for a non-varargs call and to suppress this warning
                1 error
                56 warnings""";

        final var actual = Defects4JUtils.extractCompileTestJavac(stdout);
        assertEquals(expected, actual);
    }

    @Test
    void extractCompileTestJavac_succeeds_with_variousValidTaskNames() {
        final var taskNames = List.of(
                "compile.test",
                "compile.tests",
                "compile-test",
                "compile-tests"
        );

        for (final var taskName : taskNames) {
            final var stdout = """
                    compile:
                        [javac] example
                    
                    %s:
                        [javac] /workspace/Mockito/38f/test/org/mockitoutil/ExtraMatchers.java:102: error: cannot find symbol
                        [javac]                 boolean containsSublist = Collections.indexOfSubList((List<?>) value, Arrays.asList(elements)) != -1;
                        [javac]                                                                       ^
                    
                    BUILD FAILED
                    """.formatted(taskName);
            final var expected = """
                    /workspace/Mockito/38f/test/org/mockitoutil/ExtraMatchers.java:102: error: cannot find symbol
                                    boolean containsSublist = Collections.indexOfSubList((List<?>) value, Arrays.asList(elements)) != -1;
                                                                                          ^""";

            final var actual = Defects4JUtils.extractCompileTestJavac(stdout);
            assertEquals(expected, actual, taskName);
        }
    }


    @Test
    void parseMutationTestResult_succeeds_when_inputIsValid() {
        final var killCsv = """
                MutantNo,[FAIL | TIME | EXC | LIVE | UNCOV]
                46,FAIL
                47,EXC
                51,UNCOV
                53,LIVE
                """;
        final var mutantsLog = """
                46:LVR:0:NEG:org.apache.commons.lang3.math.NumberUtils@toFloat(java.lang.String):199:7385:0.0F |==> -1.0F
                47:ROR:==(java.lang.Object,java.lang.Object):FALSE(java.lang.Object,java.lang.Object):org.apache.commons.lang3.math.NumberUtils@toFloat(java.lang.String,float):222:8162:str == null |==> false
                51:LVR:0:POS:org.apache.commons.lang3.math.NumberUtils@toByte(java.lang.String):303:10776:0 |==> 1
                53:ROR:==(java.lang.Object,java.lang.Object):FALSE(java.lang.Object,java.lang.Object):org.apache.commons.lang3.math.NumberUtils@toByte(java.lang.String,byte):324:11475:str == null |==> false
                """;

        final var expected = new MutationTestResult(
                List.of(
                        new MutantResult(
                                new Mutant(
                                        "46",
                                        "LVR",
                                        "0",
                                        "NEG",
                                        "org.apache.commons.lang3.math.NumberUtils",
                                        "toFloat(java.lang.String)",
                                        "199",
                                        "7385",
                                        "0.0F",
                                        "-1.0F"
                                ),
                                MutantState.FAIL
                        ),
                        new MutantResult(
                                new Mutant(
                                        "47",
                                        "ROR",
                                        "==(java.lang.Object,java.lang.Object)",
                                        "FALSE(java.lang.Object,java.lang.Object)",
                                        "org.apache.commons.lang3.math.NumberUtils",
                                        "toFloat(java.lang.String,float)",
                                        "222",
                                        "8162",
                                        "str == null",
                                        "false"
                                ),
                                MutantState.EXCEPTION
                        ),
                        new MutantResult(
                                new Mutant(
                                        "51",
                                        "LVR",
                                        "0",
                                        "POS",
                                        "org.apache.commons.lang3.math.NumberUtils",
                                        "toByte(java.lang.String)",
                                        "303",
                                        "10776",
                                        "0",
                                        "1"
                                ),
                                MutantState.UNCOVERED
                        ),
                        new MutantResult(
                                new Mutant(
                                        "53",
                                        "ROR",
                                        "==(java.lang.Object,java.lang.Object)",
                                        "FALSE(java.lang.Object,java.lang.Object)",
                                        "org.apache.commons.lang3.math.NumberUtils",
                                        "toByte(java.lang.String,byte)",
                                        "324",
                                        "11475",
                                        "str == null",
                                        "false"
                                ),
                                MutantState.LIVE
                        )
                ),
                4,
                3,
                2
        );

        final var actual = Defects4JUtils.parseMutationTestResult(
                "org.apache.commons.lang3.math.NumberUtils",
                killCsv,
                mutantsLog
        );
        assertEquals(expected, actual);
    }
}