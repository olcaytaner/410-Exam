package NondeterministicFiniteAutomaton;

import java.util.Objects;

/**
 * Represents a state in a nondeterministic finite automaton (NFA).
 * <p>
 * Each state has a name and flags indicating whether it is a start state or a final state.
 * </p>
 */
public class State {

  private final String name;
  private boolean startState;
  private boolean finalState;

    /**
     * Constructs a State with the specified name, start state status, and final state status.
     *
     * @param name the name of the state
     * @param startState true if this state is the start state
     * @param finalState true if this state is a final state
     */
    public State(String name, boolean startState, boolean finalState){
        this.name = name;
        this.startState = startState;
        this.finalState = finalState;
    }

    /**
     * Constructs a State with the specified name and default values for start and final state (false).
     *
     * @param name the name of the state
     */
    public State(String name){
        this(name,false,false);
    }

    /**
     * Checks if this state is equal to another object.
     * Equality is based on the state's name, start state, and final state flags.
     *
     * @param o the object to compare
     * @return true if equal, false otherwise
     */
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

    /**
     * Returns a human-readable representation of the state.
     *
     * @return formatted string describing the state
     */
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
