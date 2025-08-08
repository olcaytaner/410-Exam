package DeterministicFiniteAutomaton;

/**
 * Represents a state in a Deterministic Finite Automaton (DFA).
 * Each state has a name and can be marked as a start state, a final (accepting) state, or both.
 */
public class State {
  private final String name;
  private boolean isStart;
  private boolean isFinal;

  /**
   * Constructs a new State with the specified name.
   * The state is neither a start state nor a final state by default.
   *
   * @param name The name of the state, must not be null or empty
   */
  public State(String name) {
    this.name = name;
    this.isStart = false;
    this.isFinal = false;
  }

  /**
   * Constructs a new State with the specified name and state flags.
   *
   * @param name The name of the state, must not be null or empty
   * @param isStart Whether this state is a start state
   * @param isFinal Whether this state is a final (accepting) state
   */
  public State(String name, boolean isStart, boolean isFinal) {
    this.name = name;
    this.isStart = isStart;
    this.isFinal = isFinal;
  }

  /**
   * Gets the name of this state.
   *
   * @return The name of the state
   */
  public String getName() {
    return name;
  }

  /**
   * Checks if this state is a start state.
   *
   * @return true if this is a start state, false otherwise
   */
  public boolean isStart() {
    return isStart;
  }

  /**
   * Sets whether this state is a start state.
   *
   * @param isStart true to mark as a start state, false otherwise
   */
  public void setStart(boolean isStart) {
    this.isStart = isStart;
  }

  /**
   * Checks if this state is a final (accepting) state.
   *
   * @return true if this is a final state, false otherwise
   */
  public boolean isFinal() {
    return isFinal;
  }

  /**
   * Sets whether this state is a final (accepting) state.
   *
   * @param isFinal true to mark as a final state, false otherwise
   */
  public void setFinal(boolean isFinal) {
    this.isFinal = isFinal;
  }

  /**
   * Compares this state with another object for equality.
   * Two states are considered equal if they have the same name.
   *
   * @param o The object to compare with
   * @return true if the objects are equal, false otherwise
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof DeterministicFiniteAutomaton.State)) return false;
    DeterministicFiniteAutomaton.State other = (DeterministicFiniteAutomaton.State) o;
    return name.equals(other.name);
  }

  /**
   * Returns a hash code value for this state.
   * The hash code is based on the state's name.
   *
   * @return A hash code value for this state
   */
  @Override
  public int hashCode() {
    return name.hashCode();
  }

  /**
   * Returns a string representation of this state.
   * The string representation is the name of the state.
   *
   * @return The name of the state
   */
  @Override
  public String toString() {
    return name;
  }
}
