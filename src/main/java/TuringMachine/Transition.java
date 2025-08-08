package TuringMachine;

/**
 * Represents a transition in a Turing Machine.
 */
public class Transition {

    public final State nextState;
    /**
     * The symbol to write on the tape.
     */
    public final char symbolToWrite;
    /**
     * The direction to move the tape head.
     */
    public final Direction moveDirection;

    /**
     * Constructs a new Transition.
     * @param nextState The next state.
     * @param symbolToWrite The symbol to write on the tape.
     * @param moveDirection The direction to move the tape head.
     */
    public Transition(State nextState, char symbolToWrite, Direction moveDirection) {
        this.nextState = nextState;
        this.symbolToWrite = symbolToWrite;
        this.moveDirection = moveDirection;
    }

    /**
     * Returns the next state of the transition.
     * @return The next state.
     */
    public State getNextState() {
        return nextState;
    }

    /**
     * Returns the symbol to write on the tape.
     * @return The symbol to write.
     */
    public char getSymbolToWrite() {
        return symbolToWrite;
    }

    /**
     * Returns the direction to move the tape head.
     * @return The direction to move.
     */
    public Direction getMoveDirection() {
        return moveDirection;
    }
}
