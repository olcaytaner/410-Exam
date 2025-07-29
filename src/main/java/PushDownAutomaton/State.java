package PushDownAutomaton;

import java.util.Objects;

/**
 * Represents a single state within an automaton.
 * It stores its name and its status as a start or accept state.
 * States are considered equal if their names are the same.
 */
public class State {
    private String name;
    private boolean isStart;
    private boolean isAccept;

    /**
     * Constructs a state with a given name.
     * @param name The unique identifier for the state.
     */
    public State(String name) {
        this.name = name;
        this.isStart = false;
        this.isAccept = false;
    }

    /**
     * Gets the name of the state.
     * @return The state's name.
     */
    public String getName() {
        return name;
    }

    /**
     * Checks if this is the start state.
     * @return true if it is the start state, false otherwise.
     */
    public boolean isStart() {
        return isStart;
    }

    /**
     * Checks if this is an accept (final) state.
     * @return true if it is an accept state, false otherwise.
     */
    public boolean isAccept() {
        return isAccept;
    }

    /**
     * Sets this state as the start state.
     * @param isStart true to mark as start state, false otherwise.
     */
    public void setAsStart(boolean isStart) {
        this.isStart = isStart;
    }

    /**
     * Sets this state as an accept state.
     * @param isAccept true to mark as accept state, false otherwise.
     */
    public void setAsAccept(boolean isAccept) {
        this.isAccept = isAccept;
    }

    /**
     * Compares this state to another object for equality.
     * Two states are equal if their names are equal.
     * @param o The object to compare with.
     * @return true if the objects are equal, false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        State that = (State) o;
        return Objects.equals(name, that.name);
    }

    /**
     * Generates a hash code for the state based on its name.
     * @return The hash code.
     */
    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    /**
     * Returns the string representation of the state, which is its name.
     * @return The name of the state.
     */
    @Override
    public String toString() {
        return name;
    }
}