package TuringMachine;

public class Transition {
    public final State nextState;
    public final char writeSymbol;
    public final Direction moveDirection;

    public Transition(State nextState, char writeSymbol, Direction moveDirection) {
        this.nextState = nextState;
        this.writeSymbol = writeSymbol;
        this.moveDirection = moveDirection;
    }
}
