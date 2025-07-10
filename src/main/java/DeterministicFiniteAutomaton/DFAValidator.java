package DeterministicFiniteAutomaton;

import java.util.*;

public class DFAValidator {
  public static void validateCompleteness(AbstractAutomaton dfa) {
    Set<Symbol> alphabet = dfa.getAlphabet();

    Map<State, Set<Symbol>> transitionSymbols = new HashMap<>();

    for (Transition t : dfa.getTransitions()) {
      transitionSymbols
        .computeIfAbsent(t.getFrom(), k -> new HashSet<>())
        .add(t.getSymbol());
    }

    List<String> errors = new ArrayList<>();

    for (State state : dfa.getStates()) {
      Set<Symbol> defined = transitionSymbols.getOrDefault(state, Collections.emptySet());

      for (Symbol symbol : alphabet) {
        if (!defined.contains(symbol)) {
          errors.add("State '" + state.getName() +
            "' missing transition for symbol '" + symbol.getValue() + "'");
        }
      }
    }

    if (!errors.isEmpty()) {
      throw new IllegalStateException("Incomplete DFA:\n" + String.join("\n", errors));
    }

    System.out.println("DFA is complete.");
  }
}
