package common;

/**
 * Represents a single test case for automaton execution.
 * Contains input string and expected acceptance result.
 */
public class TestCase {
    private String input;
    private boolean shouldAccept;

    public TestCase(String input, boolean shouldAccept) {
        this.input = input;
        this.shouldAccept = shouldAccept;
    }

    public String getInput() {
        return input;
    }

    public boolean shouldAccept() {
        return shouldAccept;
    }

    @Override
    public String toString() {
        return String.format("TestCase{input='%s', shouldAccept=%s}", input, shouldAccept);
    }
}