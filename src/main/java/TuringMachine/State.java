package TuringMachine;

/**
 * Represents a state in a Turing Machine.
 */
public class State {
    private String name;
    private boolean isAccept;
    private boolean isReject;

    /**
     * Constructs a new State.
     * @param name The name of the state.
     * @param isAccept True if this is an accepting state, false otherwise.
     * @param isReject True if this is a rejecting state, false otherwise.
     */
    public State(String name, boolean isAccept, boolean isReject) {
        this.name = name;
        this.isAccept = isAccept;
        this.isReject = isReject;
    }

    /**
     * Returns the name of the state.
     * @return The name of the state.
     */
    public String getName() {
        return name;
    }

    /**
     * Checks if this is an accepting state.
     * @return True if this is an accepting state, false otherwise.
     */
    public boolean isAccept() {
        return isAccept;
    }

    /**
     * Checks if this is a rejecting state.
     * @return True if this is a rejecting state, false otherwise.
     */
    public boolean isReject() {
        return isReject;
    }

    /**
     * Sets whether this is an accepting state.
     * @param accept True to make this an accepting state, false otherwise.
     */
    public void setAccept(boolean accept) {
        isAccept = accept;
    }

    /**
     * Sets whether this is a rejecting state.
     * @param reject True to make this a rejecting state, false otherwise.
     */
    public void setReject(boolean reject) {
        isReject = reject;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        State state = (State) obj;
        return name.equals(state.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return name;
    }
}
