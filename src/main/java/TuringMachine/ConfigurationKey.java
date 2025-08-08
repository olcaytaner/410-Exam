package TuringMachine;

import java.util.Objects;

/**
 * Represents a key for the transition function of a Turing Machine.
 * It is a combination of a state and a symbol read from the tape.
 */
public class ConfigurationKey {
    private final State state;
    private final char symbolToRead;

    /**
     * Constructs a new ConfigurationKey.
     * @param state The current state.
     * @param symbolToRead The symbol read from the tape.
     */
    public ConfigurationKey(State state, char symbolToRead) {
        this.state = state;
        this.symbolToRead = symbolToRead;
    }

    /**
     * Returns the state of this configuration key.
     * @return The state.
     */
    public State getState() {
        return state;
    }

    /**
     * Returns the symbol read from the tape.
     * @return The symbol.
     */
    public char getSymbolToRead() {
        return symbolToRead;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ConfigurationKey)) return false;
        ConfigurationKey that = (ConfigurationKey) o;
        return symbolToRead == that.symbolToRead && state.equals(that.state);
    }

    @Override
    public int hashCode() {
        return Objects.hash(state, symbolToRead);
    }

    @Override
    public String toString() {
        return "(" + state + ", '" + symbolToRead + "')";
    }
}
