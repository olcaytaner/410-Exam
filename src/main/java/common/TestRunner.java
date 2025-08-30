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
        private int truePositives;
        private int trueNegatives;
        private int falsePositives;
        private int falseNegatives;

        public TestResult() {
            this.failures = new ArrayList<>();
            this.detailedResults = new ArrayList<>();
            this.truePositives = 0;
            this.trueNegatives = 0;
            this.falsePositives = 0;
            this.falseNegatives = 0;
        }

        public int getTotalTests() { return totalTests; }
        public int getPassedTests() { return passedTests; }
        public int getFailedTests() { return totalTests - passedTests; }
        public List<String> getFailures() { return failures; }
        public List<TestCaseResult> getDetailedResults() { return detailedResults; }
        
        // Classification metrics getters
        public int getTruePositives() { return truePositives; }
        public int getTrueNegatives() { return trueNegatives; }
        public int getFalsePositives() { return falsePositives; }
        public int getFalseNegatives() { return falseNegatives; }

        public void setTotalTests(int total) { this.totalTests = total; }
        public void setPassedTests(int passed) { this.passedTests = passed; }
        public void addFailure(String failure) { this.failures.add(failure); }
        public void addResult(TestCaseResult result) { this.detailedResults.add(result); }
        
        // Classification metrics setters
        public void incrementTruePositives() { this.truePositives++; }
        public void incrementTrueNegatives() { this.trueNegatives++; }
        public void incrementFalsePositives() { this.falsePositives++; }
        public void incrementFalseNegatives() { this.falseNegatives++; }
        
        // Score calculation methods
        public double getAccuracy() {
            int total = truePositives + trueNegatives + falsePositives + falseNegatives;
            return total == 0 ? 0.0 : (double)(truePositives + trueNegatives) / total * 100.0;
        }
        
        public double getPrecision() {
            int positiveResults = truePositives + falsePositives;
            return positiveResults == 0 ? 0.0 : (double)truePositives / positiveResults * 100.0;
        }
        
        public double getRecall() {
            int actualPositives = truePositives + falseNegatives;
            return actualPositives == 0 ? 0.0 : (double)truePositives / actualPositives * 100.0;
        }
        
        public double getSpecificity() {
            int actualNegatives = trueNegatives + falsePositives;
            return actualNegatives == 0 ? 0.0 : (double)trueNegatives / actualNegatives * 100.0;
        }
        
        public double getF1Score() {
            double precision = getPrecision() / 100.0; // Convert to decimal
            double recall = getRecall() / 100.0; // Convert to decimal
            return (precision + recall == 0) ? 0.0 : 2.0 * (precision * recall) / (precision + recall) * 100.0;
        }

        @Override
        public String toString() {
            return String.format("Tests passed: %d/%d", passedTests, totalTests);
        }

        public String getDetailedReport() {
            StringBuilder sb = new StringBuilder();
            
            // Classification Statistics Header
            sb.append("Classification Statistics:\n");
            sb.append(String.format("True Positives (TP): %-4d    False Positives (FP): %d\n", 
                                  truePositives, falsePositives));
            sb.append(String.format("True Negatives (TN): %-4d    False Negatives (FN): %d\n", 
                                  trueNegatives, falseNegatives));
            sb.append(String.format("Total Tests: %d\n\n", getTotalTests()));
            
            // Calculated Scores
            sb.append("Calculated Scores:\n");
            sb.append(String.format("Accuracy:    %6.2f%% (%d/%d)\n", 
                                  getAccuracy(), truePositives + trueNegatives, getTotalTests()));
            sb.append(String.format("Precision:   %6.2f%% (%d/%d)\n", 
                                  getPrecision(), truePositives, truePositives + falsePositives));
            sb.append(String.format("Recall:      %6.2f%% (%d/%d)\n", 
                                  getRecall(), truePositives, truePositives + falseNegatives));
            sb.append(String.format("Specificity: %6.2f%% (%d/%d)\n", 
                                  getSpecificity(), trueNegatives, trueNegatives + falsePositives));
            sb.append(String.format("F1 Score:    %6.2f%%\n\n", getF1Score()));
            
            // Show failures if any
            if (!getFailures().isEmpty()) {
                sb.append("Failure Details:\n");
                for (String failure : getFailures()) {
                    sb.append("• ").append(failure).append("\n");
                }
            } else {
                sb.append("No errors - all test cases classified correctly!\n");
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
            // Hide correct results (TP and TN cases)
            if (passed) {
                return ""; // Return empty string to hide correct results
            }
            
            // Show only errors with FP/FN classification
            String inputDisplay = input.isEmpty() ? "ε" : "\"" + input + "\"";
            String expected = expectedAccept ? "ACCEPT" : "REJECT";
            String actual = actualAccept ? "ACCEPT" : "REJECT";
            
            return String.format("%s → %s (Expected: %s)", inputDisplay, actual, expected);
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
                    
                    // Count classification metrics
                    if (expectedAccept && actualAccept) {
                        result.incrementTruePositives(); // TP: Expected ACCEPT, Got ACCEPT
                    } else if (!expectedAccept && !actualAccept) {
                        result.incrementTrueNegatives(); // TN: Expected REJECT, Got REJECT
                    } else if (!expectedAccept && actualAccept) {
                        result.incrementFalsePositives(); // FP: Expected REJECT, Got ACCEPT
                    } else if (expectedAccept && !actualAccept) {
                        result.incrementFalseNegatives(); // FN: Expected ACCEPT, Got REJECT
                    }
                    
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