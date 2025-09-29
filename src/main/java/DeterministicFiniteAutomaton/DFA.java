package DeterministicFiniteAutomaton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import common.Automaton;
import common.InputNormalizer;
import common.State;
import common.Symbol;

/**
 * Represents a Deterministic Finite Automaton (DFA).
 * This class provides functionality to parse, validate, and execute DFAs,
 * as well as generate Graphviz DOT code for visualization.
 */
public class DFA extends Automaton {
  private Set<State> states;
  private Set<Symbol> alphabet;
  private Set<Transition> transitions;

  private State startState;
  private Set<State> finalStates;

  /**
   * Default constructor for DFA (used for parsing from text).
   */
  public DFA() {
    super(MachineType.DFA);
    this.states = new HashSet<>();
    this.alphabet = new HashSet<>();
    this.finalStates = new HashSet<>();
    this.transitions = new HashSet<>();
  }

  /**
   * Constructs a new DFA with the specified components.
   *
   * @param states The set of states in the DFA
   * @param alphabet The input alphabet symbols
   * @param finalStates The set of final/accepting states
   * @param startState The initial/start state
   * @param transitions The set of transitions between states
   */
  public DFA(Set<State> states,
            Set<Symbol> alphabet,
            Set<State> finalStates,
            State startState, 
            Set<Transition> transitions) {
    super(MachineType.DFA);
    this.states = states;
    this.alphabet = alphabet;
    this.finalStates = finalStates;
    this.startState = startState;
    this.transitions = transitions;
  }

  /**
   * Gets all states in the DFA.
   *
   * @return An unmodifiable set of all states
   */
  public Set<State> getStates() {
    return Collections.unmodifiableSet(states);
  }

  /**
   * Gets the input alphabet of the DFA.
   *
   * @return An unmodifiable set of input symbols
   */
  public Set<Symbol> getAlphabet() {
    return Collections.unmodifiableSet(alphabet);
  }

  /**
   * Gets all transitions in the DFA.
   *
   * @return An unmodifiable set of all transitions
   */
  public Set<Transition> getTransitions() {
    return Collections.unmodifiableSet(transitions);
  }

  /**
   * Gets the start state of the DFA.
   *
   * @return The start state, or null if not set
   */
  public State getStartState() {
    return startState;
  }

  /**
   * Gets all final (accepting) states in the DFA.
   *
   * @return An unmodifiable set of final states
   */
  public Set<State> getFinalStates() {
    return Collections.unmodifiableSet(finalStates);
  }

  /**
   * Parses a string representation of a DFA and initializes this instance.
   * The input string should contain sections for states, alphabet, start state,
   * final states, and transitions.
   *
   * @param inputText The string representation of the DFA
   * @return A ParseResult indicating success or failure, along with any messages
   */
  @Override
  public ParseResult parse(String inputText) {
    this.states = new HashSet<>();
    this.alphabet = new HashSet<>();
    this.finalStates = new HashSet<>();
    this.startState = null;
    this.transitions = new HashSet<>();

    Map<String, State> stateMap = new HashMap<>();
    
    // Use InputNormalizer for consistent parsing
    InputNormalizer.NormalizedInput normalizedInput = InputNormalizer.normalize(inputText, MachineType.DFA);
    List<ValidationMessage> messages = new ArrayList<>(normalizedInput.getMessages());
    Map<String, List<String>> sections = normalizedInput.getSections();
    Map<String, Integer> sectionLineNumbers = normalizedInput.getSectionLineNumbers();

    if (normalizedInput.hasErrors()) {
      return new ParseResult(false, messages, null);
    }

    if (!InputNormalizer.validateRequiredKeywords(sections, MachineType.DFA, messages)) {
      return new ParseResult(false, messages, null);
    }

    processStates(sections.get("states"), sectionLineNumbers.get("states"), this.states, stateMap, messages);
    processAlphabet(sections.get("alphabet"), sectionLineNumbers.get("alphabet"), this.alphabet, messages);

    this.startState = processStartState(sections.get("start"), sectionLineNumbers.get("start"), stateMap, messages);
    this.finalStates.addAll(processFinalStates(sections.get("finals"), sectionLineNumbers.get("finals"), stateMap, messages));

    this.transitions = processTransitions(sections.get("transitions"), sectionLineNumbers.get("transitions"), stateMap, messages);

    boolean isSuccess = messages.stream().noneMatch(m -> m.getType() == ValidationMessage.ValidationMessageType.ERROR);
    if (!isSuccess) {
      return new ParseResult(false, messages, this);
    }

    checkForUnreachableStates(this.states, this.startState, this.transitions, messages);
    checkForDeadEndStates(this.states, this.finalStates, this.transitions, messages);

    return new ParseResult(true, messages, this);
  }

  /**
   * Executes the DFA on a given input string.
   *
   * @param inputText The input string to process
   * @return An ExecutionResult containing the result of the execution
   */
  @Override
  public ExecutionResult execute(String inputText) {
    if (inputText == null) {
      throw new IllegalArgumentException("Input text cannot be null");
    }
    
    List<ValidationMessage> runtimeMessages = new ArrayList<>();
    StringBuilder trace = new StringBuilder();
    
    if (startState == null) {
      runtimeMessages.add(new ValidationMessage("No start state defined", 0, ValidationMessage.ValidationMessageType.ERROR));
      return new ExecutionResult(false, runtimeMessages, "DFA not properly initialized");
    }
    
    State currentState = startState;
    trace.append("Initial state: ").append(currentState.getName()).append("\n");
    
    for (int i = 0; i < inputText.length(); i++) {
      char inputChar = inputText.charAt(i);
      Symbol inputSymbol = new Symbol(inputChar);
      
      // Check if symbol is in alphabet
      if (!alphabet.contains(inputSymbol)) {
        runtimeMessages.add(new ValidationMessage("Symbol '" + inputChar + "' not in alphabet", i, ValidationMessage.ValidationMessageType.ERROR));
        return new ExecutionResult(false, runtimeMessages, trace.toString());
      }
      
      // Find transition
      Transition validTransition = null;
      for (Transition transition : transitions) {
        if (transition.getFrom().getName().equals(currentState.getName()) && 
            transition.getSymbol().equals(inputSymbol)) {
          validTransition = transition;
          break;
        }
      }
      
      if (validTransition == null) {
        trace.append("No transition from state ").append(currentState.getName())
              .append(" on symbol '").append(inputChar).append("'\n");
        runtimeMessages.add(new ValidationMessage("No transition defined", i, ValidationMessage.ValidationMessageType.ERROR));
        return new ExecutionResult(false, runtimeMessages, trace.toString());
      }
      
      currentState = validTransition.getTo();
      trace.append("Read '").append(inputChar).append("' -> state ").append(currentState.getName()).append("\n");
    }
    
    // Check if final state
    boolean accepted = false;
    for (State finalState : finalStates) {
      if (finalState.getName().equals(currentState.getName())) {
        accepted = true;
        break;
      }
    }
    
    trace.append("Final state: ").append(currentState.getName());
    trace.append(accepted ? " (ACCEPTED)" : " (REJECTED)").append("\n");
    
    return new ExecutionResult(accepted, runtimeMessages, trace.toString());
  }

  /**
   * Validates the DFA configuration.
   *
   * @return A list of validation messages, empty if no issues found
   */
  @Override
  public List<ValidationMessage> validate() {
    List<ValidationMessage> messages = new ArrayList<>();
    
    if (inputText == null || inputText.trim().isEmpty()) {
      messages.add(new ValidationMessage("No input text provided", 0, ValidationMessage.ValidationMessageType.WARNING));
      return messages;
    }
    
    try {
      ParseResult result = parse(inputText);
      messages.addAll(result.getValidationMessages());
      
      if (result.isSuccess()) {
        messages.add(new ValidationMessage("DFA is valid", 0, ValidationMessage.ValidationMessageType.INFO));
      }
    } catch (Exception e) {
      messages.add(new ValidationMessage("Validation error: " + e.getMessage(), 0, ValidationMessage.ValidationMessageType.ERROR));
    }
    
    return messages;
  }


  /**
   * Processes the states section of the DFA definition.
   *
   * @param stateLines The lines containing state definitions
   * @param lineNum The line number where the states section starts
   * @param states Set to store the parsed states
   * @param stateMap Map to store state names to State objects
   * @param messages List to collect any validation messages
   */
  private void processStates(List<String> stateLines, int lineNum, 
                           Set<State> states,
                           Map<String, State> stateMap,
                           List<ValidationMessage> messages) {
    if (stateLines == null || stateLines.isEmpty()) {
      messages.add(new ValidationMessage("The 'states:' block cannot be empty.", lineNum, ValidationMessage.ValidationMessageType.ERROR));
      return;
    }

    String[] stateNames = stateLines.get(0).split("\\s+");
    for (String name : stateNames) {
      State newState = new State(name);
      states.add(newState);
      stateMap.put(name, newState);
    }
  }

  /**
   * Processes the alphabet section of the DFA definition.
   *
   * @param alphabetLines The lines containing alphabet symbols
   * @param lineNum The line number where the alphabet section starts
   * @param alphabet Set to store the parsed alphabet symbols
   * @param messages List to collect any validation messages
   */
  private void processAlphabet(List<String> alphabetLines, int lineNum,
                             Set<Symbol> alphabet,
                             List<ValidationMessage> messages) {
    if (alphabetLines == null || alphabetLines.isEmpty()) {
      messages.add(new ValidationMessage("The 'alphabet:' block cannot be empty.", lineNum, ValidationMessage.ValidationMessageType.ERROR));
      return;
    }

    String[] symbolNames = alphabetLines.get(0).split("\\s+");
    for (String name : symbolNames) {
      alphabet.add(new Symbol(name.charAt(0)));
    }
  }

  /**
   * Processes the start state definition.
   *
   * @param lines The lines containing the start state definition
   * @param lineNum The line number where the start state is defined
   * @param stateMap Map of state names to State objects
   * @param messages List to collect any validation messages
   * @return The start State object, or null if invalid
   */
  private State processStartState(List<String> lines, int lineNum,
                                                             Map<String, State> stateMap,
                                                             List<ValidationMessage> messages) {
    if (lines == null || lines.isEmpty()) {
      messages.add(new ValidationMessage("Start state definition is missing.", lineNum, ValidationMessage.ValidationMessageType.ERROR));
      return null;
    }

    String startStateName = lines.get(0).trim();
    State startState = stateMap.get(startStateName);
    if (startState == null) {
      messages.add(new ValidationMessage("Start state '" + startStateName + "' is not defined in 'states'.", lineNum, ValidationMessage.ValidationMessageType.ERROR));
      return null;
    }

    return new State(startStateName, true, false);
  }

  /**
   * Processes the final states definition.
   *
   * @param lines The lines containing the final states definition
   * @param lineNum The line number where the final states are defined
   * @param stateMap Map of state names to State objects
   * @param messages List to collect any validation messages
   * @return A set of final State objects
   */
  private Set<State> processFinalStates(List<String> lines, int lineNum,
                                                                   Map<String, State> stateMap,
                                                                   List<ValidationMessage> messages) {
    Set<State> finalStates = new HashSet<>();

    if (lines == null || lines.isEmpty()) {
      messages.add(new ValidationMessage("Final states definition is missing.", lineNum, ValidationMessage.ValidationMessageType.ERROR));
      return finalStates;
    }

    String[] finalStateNames = lines.get(0).split("\\s+");
    for (String name : finalStateNames) {
      State finalState = stateMap.get(name);
      if (finalState == null) {
        messages.add(new ValidationMessage("Final state '" + name + "' is not defined in 'states'.", lineNum, ValidationMessage.ValidationMessageType.ERROR));
        continue;
      }

      finalStates.add(new State(finalState.getName(), false, true));
    }

    return finalStates;
  }

  /**
   * Processes the transitions section of the DFA definition.
   *
   * @param lines The lines containing transition definitions
   * @param startLine The line number where the transitions section starts
   * @param stateMap Map of state names to State objects
   * @param messages List to collect any validation messages
   * @return A set of Transition objects representing the DFA's transitions
   */
  private Set<Transition> processTransitions(List<String> lines, int startLine,
                                             Map<String, State> stateMap,
                                             List<ValidationMessage> messages) {
    Set<Transition> transitionSet = new HashSet<>();
    if (lines == null) return transitionSet;

    // Map to track transitions by fromState and symbol
    Map<State, Map<Symbol, State>> transitionMap = new HashMap<>();

    for (int i = 0; i < lines.size(); i++) {
      int currentLine = startLine + i + 1;
      String line = lines.get(i);

      String[] parts = line.split("->");
      if (parts.length != 2) {
        messages.add(new ValidationMessage("Invalid transition format. Rule must contain '->' separator.", currentLine, ValidationMessage.ValidationMessageType.ERROR));
        continue;
      }

      Pattern transitionPattern = Pattern.compile(
        "^\\s*(\\w+)\\s*->\\s*(\\w+)\\s*\\(\\s*([a-zA-Z0-9]+(?:\\s+[a-zA-Z0-9]+)*)\\s*\\)\\s*$"
      );

      Matcher matcher = transitionPattern.matcher(line);

      if (matcher.matches()) {
        String fromName = matcher.group(1);
        String toName = matcher.group(2);
        String[] symbols = matcher.group(3).trim().split("\\s+");

        State from = validateState(fromName, stateMap, currentLine, messages);
        State to = validateState(toName, stateMap, currentLine, messages);
        if (from == null || to == null) {
          continue; // Skip if state validation failed
        }

        for (String symStr : symbols) {
          Symbol sym = validateSymbol(new Symbol(symStr.charAt(0)), this.alphabet, currentLine, messages);
          if (sym == null) {
            continue; // Skip if symbol validation failed
          }

          // Check for duplicate transitions
          if (transitionMap.containsKey(from) && transitionMap.get(from).containsKey(sym)) {
            State existingToState = transitionMap.get(from).get(sym);
            messages.add(new ValidationMessage(
              String.format("Multiple transitions from state '%s' with symbol '%s' (to '%s' and '%s'). A DFA must have exactly one transition per symbol per state.",
                from.getName(), sym, existingToState.getName(), to.getName()),
              currentLine,
              ValidationMessage.ValidationMessageType.ERROR
            ));
            continue;
          }

          // Add to transition map for duplicate checking
          transitionMap.computeIfAbsent(from, k -> new HashMap<>()).put(sym, to);

          // Add to the final transition set
          transitionSet.add(new Transition(from, sym, to));
        }
      } else {
        messages.add(new ValidationMessage("Invalid transition format.", currentLine, ValidationMessage.ValidationMessageType.ERROR));
      }
    }

    return transitionSet;
  }


  /**
   * Validates that a state name is defined and not a reserved word.
   *
   * @param name The state name to validate
   * @param stateMap Map of valid state names to State objects
   * @param line The line number where the state is referenced
   * @param messages List to collect any validation messages
   * @return The State object if valid, null otherwise
   */
  private State validateState(String name,
                                                         Map<String, State> stateMap,
                                                         int line, List<ValidationMessage> messages) {
    State state = stateMap.get(name);

    if (state == null) {
      messages.add(new ValidationMessage("State '" + name + "' is not defined in 'states'.", line, ValidationMessage.ValidationMessageType.ERROR));
    }

    if (name.equalsIgnoreCase("eps")) {
      messages.add(new ValidationMessage("The state name 'eps' is reserved and cannot be used.", line, ValidationMessage.ValidationMessageType.ERROR));
    }

    return state;
  }

  /**
   * Validates that a symbol is defined in the alphabet.
   *
   * @param symbol The symbol to validate
   * @param alphabet The set of valid symbols
   * @param line The line number where the symbol is referenced
   * @param messages List to collect any validation messages
   * @return The validated Symbol object, or null if invalid
   */
  private Symbol validateSymbol(Symbol symbol, Set<Symbol> alphabet,
                                                           int line, List<ValidationMessage> messages) {
    if (!alphabet.contains(symbol)) {
      messages.add(new ValidationMessage("Input symbol '" + symbol + "' is not defined in 'alphabet'.", line, ValidationMessage.ValidationMessageType.ERROR));
      return null;
    }

    return symbol;
  }

  /**
   * Checks for states that are unreachable from the start state.
   * Adds a warning message for each unreachable state found.
   *
   * @param allStates All states in the DFA
   * @param startState The start state of the DFA
   * @param transitions Set of transitions in the DFA
   * @param messages List to collect validation messages
   */
  private void checkForUnreachableStates(Set<State> allStates, 
                                       State startState,
                                       Set<Transition> transitions,
                                       List<ValidationMessage> messages) {
    if (startState == null || allStates.isEmpty()) return;

    Set<State> reachableStates = new HashSet<>();
    Queue<State> queue = new LinkedList<>();

    reachableStates.add(startState);
    queue.add(startState);

    Map<State, Set<State>> transitionMap = new HashMap<>();
    for (Transition t : transitions) {
      transitionMap.computeIfAbsent(t.getFrom(), k -> new HashSet<>()).add(t.getTo());
    }

    while (!queue.isEmpty()) {
      State currentState = queue.poll();
      Set<State> nextStates = transitionMap.get(currentState);
      if (nextStates != null) {
        for (State nextState : nextStates) {
          if (nextState != null && reachableStates.add(nextState)) {
            queue.add(nextState);
          }
        }
      }
    }

    for (State state : allStates) {
      if (!reachableStates.contains(state)) {
        messages.add(new ValidationMessage("State '" + state.getName() + "' is unreachable from the start state.", 
                                          0, 
                                          ValidationMessage.ValidationMessageType.WARNING));
      }
    }
  }

  /**
   * Checks for dead-end states in the DFA.
   * A state is considered a dead-end if it's not a final state and has no outgoing transitions.
   *
   * @param allStates All states in the DFA
   * @param finalStates Set of final/accepting states
   * @param transitions Set of transitions in the DFA
   * @param messages List to collect validation messages
   */
  private void checkForDeadEndStates(Set<State> allStates,
                                   Set<State> finalStates,
                                   Set<Transition> transitions,
                                   List<ValidationMessage> messages) {

    Set<State> statesWithOutgoing = transitions.stream()
        .map(Transition::getFrom)
        .collect(Collectors.toSet());

    for (State state : allStates) {
      if (!finalStates.contains(state) && !statesWithOutgoing.contains(state)) {
        messages.add(new ValidationMessage("State '" + state.getName() + "' is a non-final state with no outgoing transitions (dead-end state).", 
                                          0, 
                                          ValidationMessage.ValidationMessageType.WARNING));
      }
    }
  }

  /**
   * Generates a Graphviz DOT representation of the DFA.
   *
   * @param inputText Unused parameter, maintained for interface compatibility
   * @return A string containing the DOT representation of the DFA
   */
  @Override
  public String toDotCode(String inputText) {
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

    Map<String, List<String>> grouped = new HashMap<>();

    for (Transition t : transitions) {
      String key = t.getFrom().getName() + "->" + t.getTo().getName();
      grouped.computeIfAbsent(key, k -> new ArrayList<>())
        .add(String.valueOf(t.getSymbol().getValue()));
    }

    for (Map.Entry<String, List<String>> entry : grouped.entrySet()) {
      String[] nodes = entry.getKey().split("->");
      String from = nodes[0];
      String to = nodes[1];
      String label = String.join(", ", entry.getValue());

      sb.append("    ")
        .append(from)
        .append(" -> ")
        .append(to)
        .append(" [label=\"")
        .append(label)
        .append("\"];\n");
    }

    sb.append("}");
    return sb.toString();
  }

  @Override
  public String getDefaultTemplate() {
    return "Start: q0\n" +
           "Finals: q0\n" +
           "Alphabet: a b\n" +
           "States: q0\n" +
           "\n" +
           "Transitions:\n" +
           "q0 -> q0 (a b)\n";
  }
}
