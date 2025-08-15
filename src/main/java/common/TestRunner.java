package common;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

/**
 * Executes test cases against automaton implementations.
 * Compares actual results with expected results and generates test reports.
 */
public class TestRunner {

    /**
     * Result of running a test suite against an automaton.
     */
    public static class TestResult {
        private int totalTests;
        private int passedTests;
        private List<String> failures;
        private List<TestCaseResult> detailedResults;

        public TestResult() {
            this.failures = new ArrayList<>();
            this.detailedResults = new ArrayList<>();
        }

        public int getTotalTests() { return totalTests; }
        public int getPassedTests() { return passedTests; }
        public int getFailedTests() { return totalTests - passedTests; }
        public List<String> getFailures() { return failures; }
        public List<TestCaseResult> getDetailedResults() { return detailedResults; }

        public void setTotalTests(int total) { this.totalTests = total; }
        public void setPassedTests(int passed) { this.passedTests = passed; }
        public void addFailure(String failure) { this.failures.add(failure); }
        public void addResult(TestCaseResult result) { this.detailedResults.add(result); }

        @Override
        public String toString() {
            return String.format("Tests passed: %d/%d", passedTests, totalTests);
        }

        public String getDetailedReport() {
            StringBuilder sb = new StringBuilder();
            sb.append(toString()).append("\n");
            
            for (TestCaseResult result : detailedResults) {
                sb.append(result.toString()).append("\n");
            }
            
            return sb.toString();
        }
    }

    /**
     * Result of running a single test case.
     */
    public static class TestCaseResult {
        private String input;
        private boolean expectedAccept;
        private boolean actualAccept;
        private boolean passed;
        private String trace;

        public TestCaseResult(String input, boolean expectedAccept, boolean actualAccept, String trace) {
            this.input = input;
            this.expectedAccept = expectedAccept;
            this.actualAccept = actualAccept;
            this.passed = (expectedAccept == actualAccept);
            this.trace = trace;
        }

        public boolean isPassed() { return passed; }
        public String getInput() { return input; }
        public boolean getExpectedAccept() { return expectedAccept; }
        public boolean getActualAccept() { return actualAccept; }
        public String getTrace() { return trace; }

        @Override
        public String toString() {
            String status = passed ? "✓" : "✗";
            String inputDisplay = input.isEmpty() ? "ε" : "\"" + input + "\"";
            String expected = expectedAccept ? "ACCEPT" : "REJECT";
            String actual = actualAccept ? "ACCEPT" : "REJECT";
            
            if (passed) {
                return String.format("%s %s → %s", status, inputDisplay, expected);
            } else {
                return String.format("%s %s → %s (Expected: %s)", status, inputDisplay, actual, expected);
            }
        }
    }

    /**
     * Runs test cases from a file against the given automaton.
     * 
     * @param automaton the automaton to test
     * @param testFilePath path to the CSV test file
     * @return test results
     */
    public static TestResult runTests(Automaton automaton, String testFilePath) {
        TestResult result = new TestResult();
        
        try {
            List<TestCase> testCases = TestFileParser.parseTestFile(testFilePath);
            result.setTotalTests(testCases.size());
            
            if (testCases.isEmpty()) {
                result.addFailure("No test cases found in file: " + testFilePath);
                return result;
            }
            
            int passed = 0;
            
            for (int i = 0; i < testCases.size(); i++) {
                TestCase testCase = testCases.get(i);
                
                try {
                    Automaton.ExecutionResult execResult = automaton.execute(testCase.getInput());
                    boolean actualAccept = execResult.isAccepted();
                    boolean expectedAccept = testCase.shouldAccept();
                    
                    TestCaseResult testResult = new TestCaseResult(
                        testCase.getInput(), 
                        expectedAccept, 
                        actualAccept, 
                        execResult.getTrace()
                    );
                    
                    result.addResult(testResult);
                    
                    if (testResult.isPassed()) {
                        passed++;
                    } else {
                        result.addFailure(String.format("Test %d failed: %s", i + 1, testResult.toString()));
                    }
                    
                } catch (Exception e) {
                    String failure = String.format("Test %d error: %s with input '%s': %s", 
                                                 i + 1, e.getClass().getSimpleName(), testCase.getInput(), e.getMessage());
                    result.addFailure(failure);
                    
                    TestCaseResult testResult = new TestCaseResult(
                        testCase.getInput(), 
                        testCase.shouldAccept(), 
                        false, 
                        "Error: " + e.getMessage()
                    );
                    result.addResult(testResult);
                }
            }
            
            result.setPassedTests(passed);
            
        } catch (IOException e) {
            result.addFailure("Failed to read test file: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            result.addFailure("Invalid test file format: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * Runs a single test case against an automaton.
     * 
     * @param automaton the automaton to test
     * @param input the input string
     * @param expectedAccept whether the string should be accepted
     * @return test case result
     */
    public static TestCaseResult runSingleTest(Automaton automaton, String input, boolean expectedAccept) {
        try {
            Automaton.ExecutionResult execResult = automaton.execute(input);
            return new TestCaseResult(input, expectedAccept, execResult.isAccepted(), execResult.getTrace());
        } catch (Exception e) {
            return new TestCaseResult(input, expectedAccept, false, "Error: " + e.getMessage());
        }
    }
}