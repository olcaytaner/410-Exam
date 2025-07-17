package DeterministicFiniteAutomaton;

import java.util.Set;

public class DFA extends AbstractAutomaton {
  public DFA(Set<State> states,
             Set<Symbol> alphabet,
             State startState,
             Set<State> finalStates,
             Set<Transition> transitions) {
    super(states, alphabet, finalStates, startState, transitions);
  }

  public boolean validate(String input) {
    if (input == null) {
      throw new IllegalArgumentException("Input string cannot be null");
    }
    
    State currentState = startState;

    for (char ch : input.toCharArray()) {
      Symbol symbol = new Symbol(ch);
      State nextState = null;

      for (Transition t : transitions) {
        if (t.getFrom().equals(currentState) && t.getSymbol().equals(symbol)) {
          nextState = t.getTo();
          break;
        }
      }

      if (nextState == null) {
        return false;
      }

      currentState = nextState;
    }

    return finalStates.contains(currentState);
  }

  @Override
  public String toGraphviz() {
    StringBuilder sb = new StringBuilder();
    sb.append("digraph DFA {\n");
    sb.append("    rankdir=LR;\n");
    sb.append("    node [shape = doublecircle]; ");

    for (State state : finalStates) {
      sb.append(state.getName()).append(" ");
    }
    sb.append(";\n");

    sb.append("    node [shape = circle];\n");
    sb.append("    __start [shape=point];\n");
    sb.append("    __start -> ").append(startState.getName()).append(";\n");

    for (Transition t : transitions) {
      sb.append("    ")
        .append(t.getFrom().getName())
        .append(" -> ")
        .append(t.getTo().getName())
        .append(" [label=\"")
        .append(t.getSymbol().getValue())
        .append("\"];\n");
    }

    sb.append("}");
    return sb.toString();
  }
}
