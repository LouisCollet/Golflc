package calc;

import static interfaces.Log.LOG;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.EnumSource;
import static org.junit.jupiter.params.provider.EnumSource.Mode.EXCLUDE;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

// https://www.baeldung.com/junit-5-migration

@DisplayName("I'm a Test Class")
@TestMethodOrder(OrderAnnotation.class)
        
public class CalculatorTest {

    @BeforeAll
    public static void setUpClass() {
        LOG.debug("this is @BeforeAll");
        
    }
    @AfterAll
    public static void tearDownClass() {
        LOG.debug("this is @AfterAll");
    }
    
    @BeforeEach
    public void setUp() {
    }
    
    @AfterEach
    public void tearDown() {
    }
    
	@Test
	@DisplayName("1 + 1 = 2")
	void addsTwoNumbers() {
		Calculator calculator = new Calculator();
		assertEquals(2, calculator.add(1, 1), "1 + 1 should equal 2");
	}

	@ParameterizedTest(name = "{0} + {1} = {2}")
	@CsvSource({
			"0,    1,   1",
			"1,    2,   3",
			"49,  51, 100",
			"1,  100, 101"
	})
	void add(int first, int second, int expectedResult) {
            LOG.debug("entering add with first = " + first + " and second = " + second);
		Calculator calculator = new Calculator();
		assertEquals(expectedResult, calculator.add(first, second),
				() -> first + " + " + second + " should equal " + expectedResult);
        } 

//https://mkyong.com/junit5/junit-5-parameterized-tests/        
     // This test will run 3 times with different arguments
    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3})
    @DisplayName("Test with ValueSource 1, expected ok")
    void test_int_arrays(int arg) {
        Assertions.assertTrue(arg > 0);
    }

    @ParameterizedTest(name = "#{index} - Run test with args={0}")
    @ValueSource(ints = {1, 2, 3})
    @DisplayName("Test with ValueSource 2, expected ok")        
    void test_int_arrays_custom_name(int arg) {
        Assertions.assertTrue(arg > 0);
    }

    	
    
    
    
    @ParameterizedTest(name = "#{index} - Run test with args={0}")
    @ValueSource(strings = {"apple", "banana", "orange"})
    void test_string_arrays_custom_name(String arg) {
        Assertions.assertTrue(arg.length() > 1);
    }           
    
    boolean isEmpty(String input) {
        return (input == null || input.length() == 0);
    }

	// run 3 times, 1 for empty, 1 for null, 1 for ""
    @ParameterizedTest(name = "#{index} - isEmpty()? {0}")
    @EmptySource
    @NullSource
    //@NullAndEmptySource
    @ValueSource(strings = {""})
    void test_is_empty_true(String arg) {
        Assertions.assertTrue(isEmpty(arg));
    }

    @ParameterizedTest(name = "#{index} - isEmpty()? {0}")
    @ValueSource(strings = {" ", "\n", "a", "\t"})
    void test_is_empty_false(String arg) {
        Assertions.assertFalse(isEmpty(arg));
    }
    
        enum Size {
        XXS, XS, S, M, L, XL, XXL, XXXL;
    }

    @ParameterizedTest
    @EnumSource(Size.class)
    void test_enum(Size size) {
        Assertions.assertNotNull(size);
    }

    @ParameterizedTest(name = "#{index} - Is size contains {0}?")
    @EnumSource(value = Size.class, names = {"L", "XL", "XXL", "XXXL"})
    void test_enum_include(Size size) {
        Assertions.assertTrue(EnumSet.allOf(Size.class).contains(size));
    }

    // Size = M, L, XL, XXL, XXXL
    @ParameterizedTest
    @EnumSource(value = Size.class, mode = EXCLUDE, names = {"XXS", "XS", "S"})
    void test_enum_exclude(Size size) {
        EnumSet<Size> excludeSmallSize = EnumSet.range(Size.M, Size.XXXL);
        Assertions.assertTrue(excludeSmallSize.contains(size));
    }
    
    
    @ParameterizedTest(name = "#{index} - Test with String : {0}")
    @MethodSource("stringProvider")
    void test_method_string(String arg) {
        Assertions.assertNotNull(arg);
    }

    // this need static
    static Stream<String> stringProvider() {
        return Stream.of("java", "rust");
    }

    @ParameterizedTest(name = "#{index} - Test with Int : {0}")
    @MethodSource("rangeProvider")
    void test_method_int(int arg) {
        Assertions.assertTrue(arg < 10);
    }

    // this need static
    static IntStream rangeProvider() {
        return IntStream.range(0, 10);
    }
    
 @ParameterizedTest
    @MethodSource("stringIntAndListProvider")
    void testWithMultiArgMethodSource(String str, int length, List<String> list) {
        Assertions.assertTrue(str.length() > 0);
        Assertions.assertEquals(length, list.size());
    }

    static Stream<Arguments> stringIntAndListProvider() {
        return Stream.of(
                arguments("abc", 3, Arrays.asList("a", "b", "c")),
                arguments("lemon", 2, Arrays.asList("x", "y"))
        );
    }   
 @ParameterizedTest
    @CsvSource({
            "java,      4",
            "clojure,   7",
            "python,    6"
    })
    void test_csv(String str, int length) {
        Assertions.assertEquals(length, str.length());
    }   
//    //5.1 Imports comma-separated values (csv) from a file as arguments.
  //src/test/resources/simple.csv
    // Skip the first line
    @ParameterizedTest
    @CsvFileSource(resources = "/simple.csv", numLinesToSkip = 1)
            // 31-08-2020 déplacé simple.csv vers /src/test/resources/ suite update maven-resources vers 3.2.0
    void test_csv_file(String str, int length) {
        LOG.debug(" entering test_csv_file ");
      //  assertEquals(length, str.length());
        Assertions.assertEquals(length, str.length());
    }

} // end class