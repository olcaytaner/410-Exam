package DeterministicFiniteAutomaton;

public class State {
  private final String name;
  private boolean isStart;
  private boolean isFinal;

  public State(String name) {
    this.name = name;
    this.isStart = false;
    this.isFinal = false;
  }

  public State(String name, boolean isStart, boolean isFinal) {
    this.name = name;
    this.isStart = isStart;
    this.isFinal = isFinal;
  }

  public String getName() {
    return name;
  }

  public boolean isStart() {
    return isStart;
  }

  public void setStart(boolean isStart) {
    this.isStart = isStart;
  }

  public boolean isFinal() {
    return isFinal;
  }

  public void setFinal(boolean isFinal) {
    this.isFinal = isFinal;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof State)) return false;
    State other = (State) o;
    return name.equals(other.name);
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
