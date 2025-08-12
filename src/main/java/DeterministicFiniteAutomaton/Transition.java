package DeterministicFiniteAutomaton;

import common.State;
import common.Symbol;

import java.util.Objects;

/**
 * Represents a transition between two states in a Deterministic Finite Automaton (DFA).
 * A transition defines how the automaton moves from one state to another when reading a specific input symbol.
 */
public class Transition {
  private final State from;
  private final State to;
  private final Symbol symbol;

  /**
   * Constructs a new Transition with the specified source state, symbol, and destination state.
   *
   * @param from The source state of the transition, must not be null
   * @param symbol The input symbol that triggers this transition, must not be null
   * @param to The destination state of the transition, must not be null
   */
  public Transition(State from, Symbol symbol, State to) {
    if (from == null || symbol == null || to == null) {
      throw new IllegalArgumentException("Transition parameters cannot be null");
    }
    this.from = from;
    this.symbol = symbol;
    this.to = to;
  }

  /**
   * Gets the source state of this transition.
   *
   * @return The state from which this transition originates
   */
  public State getFrom() {
    return from;
  }

  /**
   * Gets the destination state of this transition.
   *
   * @return The state to which this transition leads
   */
  public State getTo() {
    return to;
  }

  /**
   * Gets the input symbol that triggers this transition.
   *
   * @return The symbol that causes this transition to be taken
   */
  public Symbol getSymbol() {
    return symbol;
  }

  /**
   * Compares this transition with another object for equality.
   * Two transitions are considered equal if they have the same from state,
   * to state, and symbol.
   *
   * @param o The object to compare with
   * @return true if the objects are equal, false otherwise
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Transition)) return false;
    Transition that = (Transition) o;
    return from.equals(that.from) &&
      to.equals(that.to) &&
      symbol.equals(that.symbol);
  }

  /**
   * Returns a hash code value for this transition.
   * The hash code is based on the from state, to state, and symbol.
   *
   * @return A hash code value for this transition
   */
  @Override
  public int hashCode() {
    return Objects.hash(from, to, symbol);
  }

  /**
   * Returns a string representation of this transition.
   * The format is "from --symbol--> to".
   *
   * @return A string representation of this transition
   */
  @Override
  public String toString() {
    return from + " --" + symbol + "--> " + to;
  }
}
