package common;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Parser for CSV-format test files.
 * Expected format: inputString,expectedResult
 * Where expectedResult is 1 for accept, 0 for reject.
 *
 * Supports optional headers:
 * #min_points=N
 * #max_points=N
 */
public class TestFileParser {

    /**
     * Result of parsing a test file, including test cases and point configuration.
     */
    public static class TestFileResult {
        private final List<TestCase> testCases;
        private final int minPoints;
        private final int maxPoints;

        public TestFileResult(List<TestCase> testCases, int minPoints, int maxPoints) {
            this.testCases = testCases;
            this.minPoints = minPoints;
            this.maxPoints = maxPoints;
        }

        public List<TestCase> getTestCases() {
            return testCases;
        }

        public int getMinPoints() {
            return minPoints;
        }

        public int getMaxPoints() {
            return maxPoints;
        }
    }

    /**
     * Parses a CSV test file and returns test cases with point configuration.
     *
     * @param filePath path to the test file
     * @return TestFileResult containing test cases and point configuration
     * @throws IOException if file cannot be read
     * @throws IllegalArgumentException if file format is invalid
     */
    public static TestFileResult parseTestFile(String filePath) throws IOException {
        List<TestCase> testCases = new ArrayList<>();
        int minPoints = 4;  // Default
        int maxPoints = 10; // Default

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            int lineNumber = 0;

            while ((line = reader.readLine()) != null) {
                lineNumber++;

                // Skip empty lines
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }

                // Parse header lines
                if (line.startsWith("#")) {
                    if (line.startsWith("#min_points=")) {
                        try {
                            minPoints = Integer.parseInt(line.substring("#min_points=".length()).trim());
                        } catch (NumberFormatException e) {
                            throw new IllegalArgumentException(
                                String.format("Invalid min_points value at line %d: '%s'", lineNumber, line));
                        }
                        continue;
                    } else if (line.startsWith("#max_points=")) {
                        try {
                            maxPoints = Integer.parseInt(line.substring("#max_points=".length()).trim());
                        } catch (NumberFormatException e) {
                            throw new IllegalArgumentException(
                                String.format("Invalid max_points value at line %d: '%s'", lineNumber, line));
                        }
                        continue;
                    } else {
                        // Other comments are ignored
                        continue;
                    }
                }

                // Parse CSV line
                String[] parts = line.split(",", 2);
                if (parts.length != 2) {
                    throw new IllegalArgumentException(
                        String.format("Invalid format at line %d: '%s'. Expected: inputString,expectedResult",
                                    lineNumber, line));
                }

                String input = parts[0].trim();
                String expectedStr = parts[1].trim();

                // Convert expected result to boolean
                boolean shouldAccept;
                if ("1".equals(expectedStr)) {
                    shouldAccept = true;
                } else if ("0".equals(expectedStr)) {
                    shouldAccept = false;
                } else {
                    throw new IllegalArgumentException(
                        String.format("Invalid expected result at line %d: '%s'. Must be 1 (accept) or 0 (reject)",
                                    lineNumber, expectedStr));
                }

                testCases.add(new TestCase(input, shouldAccept));
            }
        }

        return new TestFileResult(testCases, minPoints, maxPoints);
    }
}