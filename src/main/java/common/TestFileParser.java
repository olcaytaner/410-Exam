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
 */
public class TestFileParser {

    /**
     * Parses a CSV test file and returns a list of test cases.
     * 
     * @param filePath path to the test file
     * @return list of parsed test cases
     * @throws IOException if file cannot be read
     * @throws IllegalArgumentException if file format is invalid
     */
    public static List<TestCase> parseTestFile(String filePath) throws IOException {
        List<TestCase> testCases = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            int lineNumber = 0;
            
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                
                // Skip empty lines and comments
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
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
        
        return testCases;
    }
}