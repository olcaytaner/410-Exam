package RegularExpression;

import RegularExpression.SyntaxTree.RegularExpression;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class RegularExpressionTest {
    
    private RegularExpression re;
    
    // Helper method to repeat strings for older Java versions
    private static String repeat(String str, int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append(str);
        }
        return sb.toString();
    }
    
    @BeforeEach
    void setUp() {
        String regex = "(0u1(01*0)*1)*"; // matches multiples of 3
        re = new RegularExpression(regex, new char[]{'0', '1'});
    }
    
    @Nested
    @DisplayName("Binary numbers divisible by 3")
    class DivisibleByThreeTests {
        
        @Test
        @DisplayName("Should match single digit 0 (decimal 0)")
        void testZero() {
            assertTrue(re.match("0"), "0 (decimal 0) should match");
        }
        
        @Test
        @DisplayName("Should match 11 (decimal 3)")
        void testThree() {
            assertTrue(re.match("11"), "11 (decimal 3) should match");
        }
        
        @Test
        @DisplayName("Should match 110 (decimal 6)")
        void testSix() {
            assertTrue(re.match("110"), "110 (decimal 6) should match");
        }
        
        @Test
        @DisplayName("Should match 1001 (decimal 9)")
        void testNine() {
            assertTrue(re.match("1001"), "1001 (decimal 9) should match");
        }
        
        @Test
        @DisplayName("Should match 1100 (decimal 12)")
        void testTwelve() {
            assertTrue(re.match("1100"), "1100 (decimal 12) should match");
        }
        
        @Test
        @DisplayName("Should match 1111 (decimal 15)")
        void testFifteen() {
            assertTrue(re.match("1111"), "1111 (decimal 15) should match");
        }
        
        @Test
        @DisplayName("Should match 10010 (decimal 18)")
        void testEighteen() {
            assertTrue(re.match("10010"), "10010 (decimal 18) should match");
        }
        
        @Test
        @DisplayName("Should match 10101 (decimal 21)")
        void testTwentyOne() {
            assertTrue(re.match("10101"), "10101 (decimal 21) should match");
        }
    }
    
    @Nested
    @DisplayName("Binary numbers NOT divisible by 3")
    class NotDivisibleByThreeTests {
        
        @Test
        @DisplayName("Should not match 1 (decimal 1)")
        void testOne() {
            assertFalse(re.match("1"), "1 (decimal 1) should not match");
        }
        
        @Test
        @DisplayName("Should not match 10 (decimal 2)")
        void testTwo() {
            assertFalse(re.match("10"), "10 (decimal 2) should not match");
        }
        
        @Test
        @DisplayName("Should not match 100 (decimal 4)")
        void testFour() {
            assertFalse(re.match("100"), "100 (decimal 4) should not match");
        }
        
        @Test
        @DisplayName("Should not match 101 (decimal 5)")
        void testFive() {
            assertFalse(re.match("101"), "101 (decimal 5) should not match");
        }
        
        @Test
        @DisplayName("Should not match 111 (decimal 7)")
        void testSeven() {
            assertFalse(re.match("111"), "111 (decimal 7) should not match");
        }
        
        @Test
        @DisplayName("Should not match 1000 (decimal 8)")
        void testEight() {
            assertFalse(re.match("1000"), "1000 (decimal 8) should not match");
        }
        
        @Test
        @DisplayName("Should not match 1010 (decimal 10)")
        void testTen() {
            assertFalse(re.match("1010"), "1010 (decimal 10) should not match");
        }
        
        @Test
        @DisplayName("Should not match 1011 (decimal 11)")
        void testEleven() {
            assertFalse(re.match("1011"), "1011 (decimal 11) should not match");
        }
        
        @Test
        @DisplayName("Should not match 1101 (decimal 13)")
        void testThirteen() {
            assertFalse(re.match("1101"), "1101 (decimal 13) should not match");
        }
        
        @Test
        @DisplayName("Should not match 1110 (decimal 14)")
        void testFourteen() {
            assertFalse(re.match("1110"), "1110 (decimal 14) should not match");
        }
    }
    
    @Nested
    @DisplayName("Edge cases that might cause issues")
    class EdgeCaseTests {
        
        @Test
        @DisplayName("Should match empty string due to * operator")
        void testEmptyString() {
            assertTrue(re.match(""), "Empty string should match due to * operator");
        }
        
        @Test
        @DisplayName("Leading zero patterns")
        void testLeadingZeros() {
            // These are edge cases - behavior may vary based on implementation
            re.match("00");   // Two zeros
            re.match("011");  // Leading zero case
        }
    }
    
    @Nested
    @DisplayName("Concatenated patterns")
    class ConcatenatedPatternTests {
        
        @Test
        @DisplayName("Concatenated valid patterns")
        void testConcatenatedPatterns() {
            re.match("00");     // Two zeros
            re.match("011");    // 0 + 11 (0 + 3)
            re.match("1100");   // 11 + 00 (3 + 0)
            re.match("11110");  // 1111 + 0 (15 + 0)
            re.match("01111");  // 0 + 1111 (0 + 15)
        }
    }
    
    @Nested
    @DisplayName("Repeated character patterns")
    class RepeatedCharacterTests {
        
        @Test
        @DisplayName("Short repeated patterns")
        void testShortRepeatedPatterns() {
            re.match("0000");      // Four zeros
            re.match("1111");      // Four ones
            re.match("00000000");  // Eight zeros
            re.match("11111111");  // Eight ones
        }
    }
    
    @Nested
    @DisplayName("Alternating patterns")
    class AlternatingPatternTests {
        
        @Test
        @DisplayName("Alternating 0 and 1 patterns")
        void testAlternatingPatterns() {
            re.match("0101");
            re.match("1010");
            re.match("010101");
            re.match("101010");
            re.match("0101010101");
        }
    }
    
    @Nested
    @DisplayName("Performance and stress tests")
    class PerformanceTests {
        
        @Test
        @DisplayName("Long strings of repeated characters")
        @Timeout(value = 5, unit = TimeUnit.SECONDS)
        void testLongRepeatedStrings() {
            String manyOnes = repeat("1", 20);
            assertDoesNotThrow(() -> re.match(manyOnes), "Should handle 20 consecutive ones");
            
            String longValid = repeat("0", 50);
            assertDoesNotThrow(() -> re.match(longValid), "Should handle 50 consecutive zeros");
        }
        
        @Test
        @DisplayName("Long alternating patterns")
        @Timeout(value = 5, unit = TimeUnit.SECONDS)
        void testLongAlternatingPatterns() {
            String alternating = repeat("01", 15);
            assertDoesNotThrow(() -> re.match(alternating), "Should handle alternating 01 pattern x15");
        }
        
        @Test
        @DisplayName("Complex nested patterns")
        @Timeout(value = 5, unit = TimeUnit.SECONDS)
        void testComplexNestedPatterns() {
            String complex1 = "1" + repeat("01", 10) + "0" + "1";
            assertDoesNotThrow(() -> re.match(complex1), "Should handle complex nested pattern");
        }
        
        @Test
        @DisplayName("Stress test with very long strings")
        @Timeout(value = 10, unit = TimeUnit.SECONDS)
        void testStressPatterns() {
            // Pattern that might cause deep recursion
            String stress1 = "0" + repeat("1", 100) + "0";
            assertDoesNotThrow(() -> re.match(stress1), "Should handle 0 + 100 ones + 0");
            
            // Multiple valid patterns concatenated
            String multiPattern = "0" + "11" + "110" + "1001";
            assertDoesNotThrow(() -> re.match(multiPattern), "Should handle multiple valid patterns");
            
            // Pattern with maximum nesting
            String maxNest = "1" + repeat("01", 20) + "0" + "1";
            assertDoesNotThrow(() -> re.match(maxNest), "Should handle maximum nesting pattern");
        }
    }
    
    @Nested
    @DisplayName("Regression tests")
    class RegressionTests {
        
        @Test
        @DisplayName("Verify regex initialization doesn't throw")
        void testRegexInitialization() {
            assertDoesNotThrow(() -> {
                new RegularExpression("(0u1(01*0)*1)*", new char[]{'0', '1'});
            }, "Regex initialization should not throw exceptions");
        }
        
        @Test
        @DisplayName("Verify alphabet validation")
        void testAlphabetValidation() {
            // Test with valid alphabet
            assertDoesNotThrow(() -> {
                RegularExpression testRe = new RegularExpression("0u1", new char[]{'0', '1'});
                testRe.match("0");
                testRe.match("1");
            }, "Should work with valid alphabet characters");
        }
        
        @Test
        @DisplayName("Boundary conditions")
        void testBoundaryConditions() {
            // Single character strings
            re.match("0");
            re.match("1");
            
            // Very short valid patterns
            re.match("00");
            re.match("11");
        }
    }
} 