package TuringMachine;

import java.util.Objects;

public class ConfigurationKey {
    private final State state;
    private final char readSymbol;

    public ConfigurationKey(State state, char readSymbol) {
        this.state = state;
        this.readSymbol = readSymbol;
    }

    public State getState() {
        return state;
    }

    public char getReadSymbol() {
        return readSymbol;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ConfigurationKey)) return false;
        ConfigurationKey that = (ConfigurationKey) o;
        return readSymbol == that.readSymbol && state.equals(that.state);
    }

    @Override
    public int hashCode() {
        return Objects.hash(state, readSymbol);
    }

    @Override
    public String toString() {
        return "(" + state + ", '" + readSymbol + "')";
    }
}
