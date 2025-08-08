package DeterministicFiniteAutomaton;

import java.util.Objects;

/**
 * Represents a symbol in the input alphabet of a Deterministic Finite Automaton (DFA).
 * Symbols are the basic input units that the automaton processes.
 */
public class Symbol {
  private final String value;

  /**
   * Constructs a new Symbol with the specified string value.
   *
   * @param value The string representation of this symbol, must not be null
   */
  public Symbol(String value) {
    this.value = value;
  }

  /**
   * Gets the string value of this symbol.
   *
   * @return The string representation of this symbol
   */
  public String getValue() {
    return value;
  }

  /**
   * Compares this symbol with another object for equality.
   * Two symbols are considered equal if they have the same string value.
   *
   * @param o The object to compare with
   * @return true if the objects are equal, false otherwise
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof DeterministicFiniteAutomaton.Symbol)) return false;
    DeterministicFiniteAutomaton.Symbol symbol = (DeterministicFiniteAutomaton.Symbol) o;
    return Objects.equals(value, symbol.value);
  }

  /**
   * Returns a hash code value for this symbol.
   * The hash code is based on the symbol's string value.
   *
   * @return A hash code value for this symbol
   */
  @Override
  public int hashCode() {
    return Objects.hash(value);
  }

  /**
   * Returns a string representation of this symbol.
   * The string representation is the same as the symbol's value.
   *
   * @return The string representation of this symbol
   */
  @Override
  public String toString() {
    return String.valueOf(value);
  }
}
