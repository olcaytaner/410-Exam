package PushDownAutomaton;

public class PDATransition extends Transition {
    private final String stackPop;
    private final String stackPush;

    public PDATransition(State fromState,
                         String inputSymbol,
                         String stackPop,
                         State toState,
                         String stackPush) {
        super(fromState, inputSymbol, toState);
        this.stackPop = stackPop;
        this.stackPush = stackPush;
    }

    public String getStackPop() {
        return stackPop;
    }

    public String getStackPush() {
        return stackPush;
    }
}