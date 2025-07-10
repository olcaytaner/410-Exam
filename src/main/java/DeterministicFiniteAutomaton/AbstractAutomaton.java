package DeterministicFiniteAutomaton;

import java.util.Set;

public abstract class AbstractAutomaton {
  protected Set<State> states;
  protected Set<Symbol> alphabet;
  protected Set<Transition> transitions;

  protected State startState;
  protected Set<State> finalStates;

  public AbstractAutomaton(Set<State> states,
                          Set<Symbol> alphabet,
                          Set<State> finalStates,
                          State startState,
                          Set<Transition> transitions) {
    this.states = states;
    this.alphabet = alphabet;
    this.finalStates = finalStates;
    this.startState = startState;
    this.transitions = transitions;
  }

  public Set<State> getStates() {
    return states;
  }

  public Set<Symbol> getAlphabet() {
    return alphabet;
  }

  public Set<Transition> getTransitions() {
    return transitions;
  }

  public State getStartState() {
    return startState;
  }

  public Set<State> getFinalStates() {
    return finalStates;
  }

  public abstract String toGraphviz();
}
