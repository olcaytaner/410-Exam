package PushDownAutomaton;

public interface State {
    String getName();

    boolean isStart();

    boolean isAccept();
}