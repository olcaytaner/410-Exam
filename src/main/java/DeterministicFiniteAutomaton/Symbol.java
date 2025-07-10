package DeterministicFiniteAutomaton;

import java.util.Objects;

public class Symbol {
  private final char value;

  public Symbol(char value) {
    this.value = value;
  }

  public char getValue() {
    return value;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Symbol)) return false;
    Symbol symbol = (Symbol) o;
    return value == symbol.value;
  }

  @Override
  public int hashCode() {
    return Objects.hash(value);
  }

  @Override
  public String toString() {
    return String.valueOf(value);
  }
}
