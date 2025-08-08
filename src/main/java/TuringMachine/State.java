package TuringMachine;

/**
 * Represents a state in a Turing Machine.
 */
public class State extends common.State {

    /**
     * Constructs a new State.
     * @param name The name of the state.
     * @param isAccept True if this is an accepting state, false otherwise.
     * @param isReject True if this is a rejecting state, false otherwise.
     */
    public State(String name, boolean isAccept, boolean isReject) {
        super(name, false, isAccept, isReject);
    }
}
