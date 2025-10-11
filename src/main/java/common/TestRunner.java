package common;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.*;

/**
 * Executes test cases against automaton implementations.
 * Compares actual results with expected results and generates test reports.
 */
public class TestRunner {
    
    /**
     * Callback interface for reporting test execution progress
     */
    public interface TestProgressCallback {
        /**
         * Called when a test is about to start
         */
        void onTestStarted(int currentTest, int totalTests, String input);
        
        /**
         * Called when a test has completed
         */
        void onTestCompleted(int currentTest, int totalTests, String input, boolean passed);
    }
    
    /**
     * Progress information for a single test
     */
    public static class TestProgress {
        private final int currentTest;
        private final int totalTests;
        private final String currentInput;
        private final boolean isCompleted;
        private final boolean passed;
        
        private TestProgress(int currentTest, int totalTests, String currentInput, boolean isCompleted, boolean passed) {
            this.currentTest = currentTest;
            this.totalTests = totalTests;
            this.currentInput = currentInput;
            this.isCompleted = isCompleted;
            this.passed = passed;
        }
        
        public static TestProgress started(int currentTest, int totalTests, String input) {
            return new TestProgress(currentTest, totalTests, input, false, false);
        }
        
        public static TestProgress completed(int currentTest, int totalTests, String input, boolean passed) {
            return new TestProgress(currentTest, totalTests, input, true, passed);
        }
        
        public int getCurrentTest() { return currentTest; }
        public int getTotalTests() { return totalTests; }
        public String getCurrentInput() { return currentInput; }
        public boolean isCompleted() { return isCompleted; }
        public boolean isPassed() { return passed; }
        public int getProgressPercentage() {
            if (totalTests == 0) return 0;
            return (int) ((double) currentTest / totalTests * 100);
        }
    }
    
    /**
     * Default timeout for entire test suite execution in milliseconds (5 seconds)
     */
    public static final long DEFAULT_TIMEOUT_MS = 5000;
    
    /**
     * Thread pool for executing tests with timeout
     */
    private static final ExecutorService executor = Executors.newCachedThreadPool();

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
        private int timeoutCount;
        private int maxPoints;

        public TestResult() {
            this.failures = new ArrayList<>();
            this.detailedResults = new ArrayList<>();
            this.truePositives = 0;
            this.trueNegatives = 0;
            this.falsePositives = 0;
            this.falseNegatives = 0;
            this.timeoutCount = 0;
            this.maxPoints = 10;
        }

        public int getTotalTests() { return totalTests; }
        public int getPassedTests() { return passedTests; }
        public int getFailedTests() { return totalTests - passedTests; }
        public List<String> getFailures() { return failures; }
        public List<TestCaseResult> getDetailedResults() { return detailedResults; }
        public int getTimeoutCount() { return timeoutCount; }

        // Classification metrics getters
        public int getTruePositives() { return truePositives; }
        public int getTrueNegatives() { return trueNegatives; }
        public int getFalsePositives() { return falsePositives; }
        public int getFalseNegatives() { return falseNegatives; }

        // Grading getters and setters
        public void setMaxPoints(int max) { this.maxPoints = max; }
        public int getMaxPoints() { return maxPoints; }

        public void setTotalTests(int total) { this.totalTests = total; }
        public void setPassedTests(int passed) { this.passedTests = passed; }
        public void addFailure(String failure) { this.failures.add(failure); }
        public void addResult(TestCaseResult result) { this.detailedResults.add(result); }

        // Classification metrics setters
        public void incrementTruePositives() { this.truePositives++; }
        public void incrementTrueNegatives() { this.trueNegatives++; }
        public void incrementFalsePositives() { this.falsePositives++; }
        public void incrementFalseNegatives() { this.falseNegatives++; }
        public void incrementTimeoutCount() { this.timeoutCount++; }
        
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

        /**
         * Calculate points using logarithmic grading formula.
         *
         * For cases with false positives (FP > 0):
         *   - Error rate > 50%: 1 point
         *   - Error rate > 10%: 2 points
         *   - Error rate ≤ 10%: 6 points
         *
         * For cases without false positives (FP = 0):
         *   - Uses logarithmic scale: 2 + round(8 * log(1 + 35*correctness) / log(36))
         *   - Points range from 2 to 10 based on true positives
         *   - Correctness = TP / (TP + FN), where TP + FN = total strings that should be accepted
         *
         * @return calculated points (0 if no expected accepts)
         */
        public int calculatePoints() {
            // Total strings that should be accepted = TP + FN
            int totalRequired = truePositives + falseNegatives;
            if (totalRequired == 0) {
                return 0; // Cannot calculate without any expected accepts
            }

            int generated = truePositives + falsePositives;
            double errorRate = generated > 0 ? (double) falsePositives / generated : 0.0;

            if (falsePositives > 0) {
                // Cases with false positives - based on error rate
                if (errorRate > 0.5) {
                    return 1;
                } else if (errorRate > 0.1) {
                    return 2;
                } else {
                    return 6;
                }
            } else {
                // Cases without false positives - logarithmic scale
                double correctness = (double) truePositives / totalRequired;
                double k = 35.0;
                double logScale = Math.log(1 + k * correctness) / Math.log(1 + k);
                int points = 2 + (int) Math.round(8.0 * logScale);

                // Clamp to valid range [2, maxPoints]
                return Math.max(2, Math.min(maxPoints, points));
            }
        }

        /**
         * Get the calculated points for this test result.
         *
         * @return points earned (0 if totalRequiredStrings not set)
         */
        public int getPoints() {
            return calculatePoints();
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
            if (timeoutCount > 0) {
                sb.append(String.format("Timeouts: %d\n", timeoutCount));
            }
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
            sb.append(String.format("F1 Score:    %6.2f%%\n", getF1Score()));

            // Display points if there are expected accepts (TP + FN > 0)
            if ((truePositives + falseNegatives) > 0) {
                sb.append(String.format("Points:      %d/%d\n", getPoints(), maxPoints));
            }
            sb.append("\n");
            
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
        private boolean timedOut;

        public TestCaseResult(String input, boolean expectedAccept, boolean actualAccept, String trace) {
            this.input = input;
            this.expectedAccept = expectedAccept;
            this.actualAccept = actualAccept;
            this.timedOut = trace != null && trace.startsWith("TIMEOUT:");
            this.passed = !timedOut && (expectedAccept == actualAccept);
            this.trace = trace;
        }

        public boolean isPassed() { return passed; }
        public String getInput() { return input; }
        public boolean getExpectedAccept() { return expectedAccept; }
        public boolean getActualAccept() { return actualAccept; }
        public String getTrace() { return trace; }
        public boolean isTimedOut() { return timedOut; }

        @Override
        public String toString() {
            // Hide correct results (TP and TN cases)
            if (passed) {
                return ""; // Return empty string to hide correct results
            }
            
            String inputDisplay = input.isEmpty() ? "ε" : "\"" + input + "\"";
            
            // Handle timeout cases specially
            if (timedOut) {
                String expected = expectedAccept ? "ACCEPT" : "REJECT";
                return String.format("%s → TIMEOUT (Expected: %s)", inputDisplay, expected);
            }
            
            // Show only errors with FP/FN classification
            String expected = expectedAccept ? "ACCEPT" : "REJECT";
            String actual = actualAccept ? "ACCEPT" : "REJECT";
            
            return String.format("%s → %s (Expected: %s)", inputDisplay, actual, expected);
        }
    }

    /**
     * Runs test cases from a file against the given automaton with default timeout.
     * 
     * @param automaton the automaton to test
     * @param testFilePath path to the CSV test file
     * @return test results
     */
    public static TestResult runTests(Automaton automaton, String testFilePath) {
        return runTests(automaton, testFilePath, DEFAULT_TIMEOUT_MS);
    }
    
    /**
     * Runs test cases from a file against the given automaton with default timeout and progress callback.
     * 
     * @param automaton the automaton to test
     * @param testFilePath path to the CSV test file
     * @param progressCallback callback to report progress (can be null)
     * @return test results
     */
    public static TestResult runTests(Automaton automaton, String testFilePath, TestProgressCallback progressCallback) {
        return runTests(automaton, testFilePath, DEFAULT_TIMEOUT_MS, progressCallback);
    }
    
    /**
     * Runs test cases from a file against the given automaton with specified timeout for entire test suite.
     * 
     * @param automaton the automaton to test
     * @param testFilePath path to the CSV test file
     * @param totalTimeoutMs timeout in milliseconds for the entire test suite
     * @return test results
     */
    public static TestResult runTests(Automaton automaton, String testFilePath, long totalTimeoutMs) {
        return runTests(automaton, testFilePath, totalTimeoutMs, null);
    }
    
    /**
     * Runs test cases from a file against the given automaton with specified timeout and progress callback.
     * 
     * @param automaton the automaton to test
     * @param testFilePath path to the CSV test file
     * @param totalTimeoutMs timeout in milliseconds for the entire test suite
     * @param progressCallback callback to report progress (can be null)
     * @return test results
     */
    public static TestResult runTests(Automaton automaton, String testFilePath, long totalTimeoutMs, TestProgressCallback progressCallback) {
        // Execute entire test suite with timeout
        Future<TestResult> future = executor.submit(() -> runTestsWithoutTimeout(automaton, testFilePath, progressCallback));
        
        try {
            return future.get(totalTimeoutMs, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            future.cancel(true);
            // Create a result indicating the entire test suite timed out
            TestResult result = new TestResult();
            try {
                List<TestCase> testCases = TestFileParser.parseTestFile(testFilePath);
                result.setTotalTests(testCases.size());
                result.incrementTimeoutCount();
                result.addFailure("TIMEOUT: Entire test suite exceeded " + totalTimeoutMs + "ms");
                
                // Add a timeout result for each test case
                for (TestCase testCase : testCases) {
                    TestCaseResult testResult = new TestCaseResult(
                        testCase.getInput(), 
                        testCase.shouldAccept(), 
                        false, 
                        "TIMEOUT: Test suite execution exceeded " + totalTimeoutMs + "ms"
                    );
                    result.addResult(testResult);
                }
            } catch (Exception ex) {
                result.addFailure("Error parsing test file during timeout: " + ex.getMessage());
            }
            return result;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            TestResult result = new TestResult();
            result.addFailure("Test execution was interrupted");
            return result;
        } catch (ExecutionException e) {
            TestResult result = new TestResult();
            result.addFailure("Test execution error: " + e.getCause().getMessage());
            return result;
        }
    }
    
    /**
     * Runs tests without timeout (used internally by the timeout wrapper)
     */
    private static TestResult runTestsWithoutTimeout(Automaton automaton, String testFilePath) {
        return runTestsWithoutTimeout(automaton, testFilePath, null);
    }
    
    /**
     * Runs tests without timeout with progress callback (used internally by the timeout wrapper)
     */
    private static TestResult runTestsWithoutTimeout(Automaton automaton, String testFilePath, TestProgressCallback progressCallback) {
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
                
                // Report test started
                if (progressCallback != null) {
                    progressCallback.onTestStarted(i + 1, testCases.size(), testCase.getInput());
                }
                
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
                    
                    // Report test completed
                    if (progressCallback != null) {
                        progressCallback.onTestCompleted(i + 1, testCases.size(), testCase.getInput(), testResult.isPassed());
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
                    
                    // Report test completed with error
                    if (progressCallback != null) {
                        progressCallback.onTestCompleted(i + 1, testCases.size(), testCase.getInput(), false);
                    }
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
     * Runs a single test case against an automaton with default timeout.
     * 
     * @param automaton the automaton to test
     * @param input the input string
     * @param expectedAccept whether the string should be accepted
     * @return test case result
     */
    public static TestCaseResult runSingleTest(Automaton automaton, String input, boolean expectedAccept) {
        return runSingleTest(automaton, input, expectedAccept, DEFAULT_TIMEOUT_MS);
    }
    
    /**
     * Runs a single test case against an automaton with specified timeout.
     * 
     * @param automaton the automaton to test
     * @param input the input string
     * @param expectedAccept whether the string should be accepted
     * @param timeoutMs timeout in milliseconds
     * @return test case result
     */
    public static TestCaseResult runSingleTest(Automaton automaton, String input, boolean expectedAccept, long timeoutMs) {
        try {
            Future<Automaton.ExecutionResult> future = executor.submit(() -> 
                automaton.execute(input)
            );
            
            Automaton.ExecutionResult execResult;
            try {
                execResult = future.get(timeoutMs, TimeUnit.MILLISECONDS);
            } catch (TimeoutException e) {
                future.cancel(true);
                return new TestCaseResult(input, expectedAccept, false, 
                    "TIMEOUT: Execution exceeded " + timeoutMs + "ms");
            }
            
            return new TestCaseResult(input, expectedAccept, execResult.isAccepted(), execResult.getTrace());
        } catch (Exception e) {
            return new TestCaseResult(input, expectedAccept, false, "Error: " + e.getMessage());
        }
    }
}