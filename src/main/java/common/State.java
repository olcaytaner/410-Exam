package common;

import java.util.Objects;

/**
 * Represents a standardized state that can be used across different types of automata.
 * This class provides a unified interface for states in DFA, NFA, PDA, and Turing Machines.
 * 
 * Features:
 * - Immutable name for state identity
 * - Support for start, accept, and reject states
 * - Consistent naming conventions
 * - Proper equals/hashCode implementation
 * - Flexible constructors for different use cases
 */
public class State {
    private final String name;
    private boolean isStart;
    private boolean isAccept;
    private boolean isReject;

    /**
     * Constructs a state with the given name and default flags (all false).
     * @param name The unique identifier for the state, must not be null or empty
     * @throws IllegalArgumentException if name is null or empty
     */
    public State(String name) {
        this(name, false, false, false);
    }

    /**
     * Constructs a state with the given name and start/accept flags.
     * Reject flag is set to false (suitable for DFA, NFA, PDA).
     * @param name The unique identifier for the state, must not be null or empty
     * @param isStart True if this is the start state
     * @param isAccept True if this is an accepting state
     * @throws IllegalArgumentException if name is null or empty
     */
    public State(String name, boolean isStart, boolean isAccept) {
        this(name, isStart, isAccept, false);
    }

    /**
     * Constructs a state with all possible flags (suitable for Turing Machines).
     * @param name The unique identifier for the state, must not be null or empty
     * @param isStart True if this is the start state
     * @param isAccept True if this is an accepting state
     * @param isReject True if this is a rejecting state
     * @throws IllegalArgumentException if name is null or empty, or if both isAccept and isReject are true
     */
    public State(String name, boolean isStart, boolean isAccept, boolean isReject) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("State name cannot be null or empty");
        }
        if (isAccept && isReject) {
            throw new IllegalArgumentException("A state cannot be both accepting and rejecting");
        }
        
        this.name = name.trim();
        this.isStart = isStart;
        this.isAccept = isAccept;
        this.isReject = isReject;
    }

    /**
     * Gets the name of this state.
     * @return The state's name
     */
    public String getName() {
        return name;
    }

    /**
     * Checks if this is the start state.
     * @return True if this is the start state, false otherwise
     */
    public boolean isStart() {
        return isStart;
    }

    /**
     * Checks if this is an accepting state.
     * @return True if this is an accepting state, false otherwise
     */
    public boolean isAccept() {
        return isAccept;
    }

    /**
     * Checks if this is a rejecting state.
     * This is primarily used for Turing Machines.
     * @return True if this is a rejecting state, false otherwise
     */
    public boolean isReject() {
        return isReject;
    }



    /**
     * Sets whether this state is the start state.
     * @param isStart True to mark as start state, false otherwise
     */
    public void setStart(boolean isStart) {
        this.isStart = isStart;
    }

    /**
     * Sets whether this state is an accepting state.
     * If set to true, automatically sets reject to false.
     * @param isAccept True to mark as accepting state, false otherwise
     */
    public void setAccept(boolean isAccept) {
        this.isAccept = isAccept;
        if (isAccept) {
            this.isReject = false;
        }
    }

    /**
     * Sets whether this state is a rejecting state.
     * If set to true, automatically sets accept to false.
     * @param isReject True to mark as rejecting state, false otherwise
     */
    public void setReject(boolean isReject) {
        this.isReject = isReject;
        if (isReject) {
            this.isAccept = false;
        }
    }



    /**
     * Checks if this state is a normal (non-special) state.
     * A normal state is neither start, accept, nor reject.
     * @return True if this is a normal state, false otherwise
     */
    public boolean isNormal() {
        return !isStart && !isAccept && !isReject;
    }

    /**
     * Compares this state with another object for equality.
     * Two states are considered equal if they have the same name.
     * This follows the principle that state identity is determined by name only.
     * @param obj The object to compare with
     * @return True if the objects are equal, false otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        State other = (State) obj;
        return Objects.equals(name, other.name);
    }

    /**
     * Returns a hash code value for this state.
     * The hash code is based only on the state's name.
     * @return A hash code value for this state
     */
    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    /**
     * Returns a string representation of this state.
     * For simple display, returns just the name.
     * @return The name of the state
     */
    @Override
    public String toString() {
        return name;
    }

    /**
     * Returns a detailed string representation of this state including all flags.
     * Useful for debugging and detailed output.
     * @return A detailed string describing the state and its properties
     */
    public String toDetailedString() {
        StringBuilder sb = new StringBuilder();
        sb.append("State{name='").append(name).append("'");
        
        if (isStart) sb.append(", START");
        if (isAccept) sb.append(", ACCEPT");
        if (isReject) sb.append(", REJECT");
        if (isNormal()) sb.append(", NORMAL");
        
        sb.append("}");
        return sb.toString();
    }

    /**
     * Returns a human-readable representation of the state.
     * @return Formatted string describing the state
     */
    public String prettyPrint() {
        StringBuilder sb = new StringBuilder();
        sb.append("State: ").append(name);
        
        if (isStart) sb.append(" [START]");
        if (isAccept) sb.append(" [ACCEPT]");
        if (isReject) sb.append(" [REJECT]");
        
        sb.append("\n");
        return sb.toString();
    }
}
