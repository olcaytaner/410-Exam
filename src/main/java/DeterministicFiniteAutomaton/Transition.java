package DeterministicFiniteAutomaton;

import java.util.Objects;

public class Transition {
  private final State from;
  private final State to;
  private final Symbol symbol;

  public Transition(State from, Symbol symbol, State to) {
    this.from = from;
    this.symbol = symbol;
    this.to = to;
  }

  public State getFrom() {
    return from;
  }

  public State getTo() {
    return to;
  }

  public Symbol getSymbol() {
    return symbol;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Transition)) return false;
    Transition that = (Transition) o;
    return from.equals(that.from) &&
      to.equals(that.to) &&
      symbol.equals(that.symbol);
  }

  @Override
  public int hashCode() {
    return Objects.hash(from, to, symbol);
  }

  @Override
  public String toString() {
    return from + " --" + symbol + "--> " + to;
  }
}
