package NondeterministicFiniteAutomaton;

import java.util.Objects;

public class State {

  private String name;
  private boolean startState;
  private boolean finalState;

  public State(String name, boolean startState, boolean finalState){
      this.name = name;
      this.startState = startState;
      this.finalState = finalState;
  }

  public State(String name){
      this(name,false,false);
  }


    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        State state = (State) o;
        return startState == state.startState && finalState == state.finalState && Objects.equals(name, state.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, startState, finalState);
    }

    public String getName() {
        return name;
    }

    public boolean isStartState() {
        return startState;
    }

    public boolean isFinalState() {
        return finalState;
    }

    public void setStartState(boolean startState) {
        this.startState = startState;
    }

    public void setFinalState(boolean finalState) {
        this.finalState = finalState;
    }

    @Override
    public String toString() {
        return "State{" +
                "name='" + name + '\'' +
                ", startState=" + startState +
                ", finalState=" + finalState +
                "}\n";
    }

    public String prettyPrint() {
      StringBuilder sb = new StringBuilder();
      sb.append("State: ").append(name).append(" ");
      if (startState) {
          sb.append("Start state");
      }
      if (finalState) {
          sb.append("Final state");
      }
      sb.append("\n");
      return sb.toString();
    }
}
