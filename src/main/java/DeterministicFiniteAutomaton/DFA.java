package DeterministicFiniteAutomaton;

import common.Automaton;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Represents a Deterministic Finite Automaton (DFA).
 * This class provides functionality to parse, validate, and execute DFAs,
 * as well as generate Graphviz DOT code for visualization.
 */
public class DFA extends Automaton {
  private Set<DeterministicFiniteAutomaton.State> states;
  private Set<DeterministicFiniteAutomaton.Symbol> alphabet;
  private Set<DeterministicFiniteAutomaton.Transition> transitions;

  private DeterministicFiniteAutomaton.State startState;
  private Set<DeterministicFiniteAutomaton.State> finalStates;

  /**
   * Constructs a new DFA with the specified components.
   *
   * @param states The set of states in the DFA
   * @param alphabet The input alphabet symbols
   * @param finalStates The set of final/accepting states
   * @param startState The initial/start state
   * @param transitions The set of transitions between states
   */
  public DFA(Set<DeterministicFiniteAutomaton.State> states,
            Set<DeterministicFiniteAutomaton.Symbol> alphabet,
            Set<DeterministicFiniteAutomaton.State> finalStates,
            DeterministicFiniteAutomaton.State startState, 
            Set<DeterministicFiniteAutomaton.Transition> transitions) {
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
  public Set<DeterministicFiniteAutomaton.State> getStates() {
    return states;
  }

  /**
   * Gets the input alphabet of the DFA.
   *
   * @return An unmodifiable set of input symbols
   */
  public Set<DeterministicFiniteAutomaton.Symbol> getAlphabet() {
    return alphabet;
  }

  /**
   * Gets all transitions in the DFA.
   *
   * @return An unmodifiable set of all transitions
   */
  public Set<DeterministicFiniteAutomaton.Transition> getTransitions() {
    return transitions;
  }

  /**
   * Gets the start state of the DFA.
   *
   * @return The start state, or null if not set
   */
  public DeterministicFiniteAutomaton.State getStartState() {
    return startState;
  }

  /**
   * Gets all final (accepting) states in the DFA.
   *
   * @return An unmodifiable set of final states
   */
  public Set<DeterministicFiniteAutomaton.State> getFinalStates() {
    return finalStates;
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
    List<ValidationMessage> messages = new ArrayList<>();
    this.states = new HashSet<>();
    this.alphabet = new HashSet<>();
    this.finalStates = new HashSet<>();
    this.startState = null;
    this.transitions = new HashSet<>();

    Map<String, DeterministicFiniteAutomaton.State> stateMap = new HashMap<>();
    String[] lines = inputText.split("\\R");
    Map<String, List<String>> sections = new HashMap<>();
    Map<String, Integer> sectionLineNumbers = new HashMap<>();

    parseSections(lines, sections, sectionLineNumbers, messages);

    if (!validateKeywords(sections, messages)) {
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

  // TODO: Implement the execute method
  /**
   * Executes the DFA on a given input string.
   *
   * @param inputText The input string to process
   * @return An ExecutionResult containing the result of the execution
   */
  @Override
  public ExecutionResult execute(String inputText) {
    return null;
  }

  // TODO: Implement the validate method
  /**
   * Validates the DFA configuration.
   *
   * @return A list of validation messages, empty if no issues found
   */
  @Override
  public List<ValidationMessage> validate() {
    return null;
  }

  /**
   * Parses the input text into sections based on keywords.
   * Sections are separated by keywords followed by colons (e.g., "states:").
   *
   * @param lines The input lines to parse
   * @param sections Map to store the parsed sections
   * @param lineNumbers Map to store the line numbers of section starts
   * @param messages List to collect any validation messages
   */
  private void parseSections(String[] lines, Map<String, List<String>> sections, 
                           Map<String, Integer> lineNumbers, List<ValidationMessage> messages) {
    String currentSection = null;
    for (int i = 0; i < lines.length; i++) {
      String line = lines[i].trim();
      if (line.isEmpty() || line.startsWith("#")) continue;

      int colonIndex = line.indexOf(":");
      if (colonIndex != -1) {
        currentSection = line.substring(0, colonIndex).trim().toLowerCase();
        String data = line.substring(colonIndex + 1).trim();

        if (sections.containsKey(currentSection)) {
          messages.add(new ValidationMessage("Duplicate keyword '" + currentSection + "'. Only the first definition will be used.", i + 1, ValidationMessage.ValidationMessageType.WARNING));
          currentSection = null;
          continue;
        }
        sections.put(currentSection, new ArrayList<>());
        lineNumbers.put(currentSection, i + 1);

        if (!data.isEmpty()) {
          sections.get(currentSection).add(data);
        }
      } else if (currentSection != null) {
        sections.get(currentSection).add(line);
      } else {
        messages.add(new ValidationMessage("Undefined content. All content must be under a keyword (e.g., 'states:' or 'States:').", i + 1, ValidationMessage.ValidationMessageType.ERROR));
      }
    }
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
                           Set<DeterministicFiniteAutomaton.State> states,
                           Map<String, DeterministicFiniteAutomaton.State> stateMap,
                           List<ValidationMessage> messages) {
    if (stateLines == null || stateLines.isEmpty()) {
      messages.add(new ValidationMessage("The 'states:' block cannot be empty.", lineNum, ValidationMessage.ValidationMessageType.ERROR));
      return;
    }

    String[] stateNames = stateLines.get(0).split("\\s+");
    for (String name : stateNames) {
      DeterministicFiniteAutomaton.State newState = new DeterministicFiniteAutomaton.State(name);
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
                             Set<DeterministicFiniteAutomaton.Symbol> alphabet,
                             List<ValidationMessage> messages) {
    if (alphabetLines == null || alphabetLines.isEmpty()) {
      messages.add(new ValidationMessage("The 'alphabet:' block cannot be empty.", lineNum, ValidationMessage.ValidationMessageType.ERROR));
      return;
    }

    String[] symbolNames = alphabetLines.get(0).split("\\s+");
    for (String name : symbolNames) {
      alphabet.add(new DeterministicFiniteAutomaton.Symbol(name));
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
  private DeterministicFiniteAutomaton.State processStartState(List<String> lines, int lineNum,
                                                             Map<String, DeterministicFiniteAutomaton.State> stateMap,
                                                             List<ValidationMessage> messages) {
    if (lines == null || lines.isEmpty()) {
      messages.add(new ValidationMessage("Start state definition is missing.", lineNum, ValidationMessage.ValidationMessageType.ERROR));
      return null;
    }

    String startStateName = lines.get(0).trim();
    DeterministicFiniteAutomaton.State startState = stateMap.get(startStateName);
    if (startState == null) {
      messages.add(new ValidationMessage("Start state '" + startStateName + "' is not defined in 'states'.", lineNum, ValidationMessage.ValidationMessageType.ERROR));
      return null;
    }

    return new DeterministicFiniteAutomaton.State(startStateName, true, false);
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
  private Set<DeterministicFiniteAutomaton.State> processFinalStates(List<String> lines, int lineNum,
                                                                   Map<String, DeterministicFiniteAutomaton.State> stateMap,
                                                                   List<ValidationMessage> messages) {
    Set<DeterministicFiniteAutomaton.State> finalStates = new HashSet<>();

    if (lines == null || lines.isEmpty()) {
      messages.add(new ValidationMessage("Final states definition is missing.", lineNum, ValidationMessage.ValidationMessageType.ERROR));
      return finalStates;
    }

    String[] finalStateNames = lines.get(0).split("\\s+");
    for (String name : finalStateNames) {
      DeterministicFiniteAutomaton.State finalState = stateMap.get(name);
      if (finalState == null) {
        messages.add(new ValidationMessage("Final state '" + name + "' is not defined in 'states'.", lineNum, ValidationMessage.ValidationMessageType.ERROR));
        continue;
      }

      finalStates.add(new DeterministicFiniteAutomaton.State(finalState.getName(), false, true));
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
  private Set<DeterministicFiniteAutomaton.Transition> processTransitions(List<String> lines, int startLine,
                                                                       Map<String, DeterministicFiniteAutomaton.State> stateMap,
                                                                       List<ValidationMessage> messages) {
    Set<DeterministicFiniteAutomaton.Transition> transitionSet = new HashSet<>();
    if (lines == null) return transitionSet;

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

        DeterministicFiniteAutomaton.State from = validateState(fromName, stateMap, currentLine, messages);
        DeterministicFiniteAutomaton.State to = validateState(toName, stateMap, currentLine, messages);

        for (String symStr : symbols) {
          DeterministicFiniteAutomaton.Symbol sym = validateSymbol(new DeterministicFiniteAutomaton.Symbol(symStr), this.alphabet, currentLine, messages);

          transitionSet.add(new DeterministicFiniteAutomaton.Transition(from, sym, to));
        }

      } else {
        messages.add(new ValidationMessage("Invalid transition format.", currentLine, ValidationMessage.ValidationMessageType.ERROR));
      }
    }

    return transitionSet;
  }

  /**
   * Validates that all required keywords are present in the DFA definition.
   *
   * @param sections Map of section names to their content lines
   * @param messages List to collect any validation messages
   * @return true if all required keywords are present, false otherwise
   */
  private boolean validateKeywords(Map<String, List<String>> sections, List<ValidationMessage> messages) {
    boolean allFound = true;
    String[] requiredKeys = {"states", "alphabet", "start", "finals", "transitions"};

    for (String key : requiredKeys) {
      if (!sections.containsKey(key)) {
        messages.add(new ValidationMessage("Missing required keyword definition for '" + key + ":'.", 0, ValidationMessage.ValidationMessageType.ERROR));

        allFound = false;
      }
    }

    return allFound;
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
  private DeterministicFiniteAutomaton.State validateState(String name,
                                                         Map<String, DeterministicFiniteAutomaton.State> stateMap,
                                                         int line, List<ValidationMessage> messages) {
    DeterministicFiniteAutomaton.State state = stateMap.get(name);

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
  private DeterministicFiniteAutomaton.Symbol validateSymbol(Symbol symbol, Set<Symbol> alphabet,
                                                           int line, List<ValidationMessage> messages) {
    if (!symbol.getValue().equals("eps") && !alphabet.contains(symbol)) {
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
  private void checkForUnreachableStates(Set<DeterministicFiniteAutomaton.State> allStates, 
                                       DeterministicFiniteAutomaton.State startState,
                                       Set<DeterministicFiniteAutomaton.Transition> transitions,
                                       List<ValidationMessage> messages) {
    if (startState == null || allStates.isEmpty()) return;

    Set<DeterministicFiniteAutomaton.State> reachableStates = new HashSet<>();
    Queue<DeterministicFiniteAutomaton.State> queue = new LinkedList<>();

    reachableStates.add(startState);
    queue.add(startState);

    Map<DeterministicFiniteAutomaton.State, Set<DeterministicFiniteAutomaton.State>> transitionMap = new HashMap<>();
    for (DeterministicFiniteAutomaton.Transition t : transitions) {
      transitionMap.computeIfAbsent(t.getFrom(), k -> new HashSet<>()).add(t.getTo());
    }

    while (!queue.isEmpty()) {
      DeterministicFiniteAutomaton.State currentState = queue.poll();
      Set<DeterministicFiniteAutomaton.State> nextStates = transitionMap.get(currentState);
      if (nextStates != null) {
        for (DeterministicFiniteAutomaton.State nextState : nextStates) {
          if (nextState != null && reachableStates.add(nextState)) {
            queue.add(nextState);
          }
        }
      }
    }

    for (DeterministicFiniteAutomaton.State state : allStates) {
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
  private void checkForDeadEndStates(Set<DeterministicFiniteAutomaton.State> allStates,
                                   Set<DeterministicFiniteAutomaton.State> finalStates,
                                   Set<DeterministicFiniteAutomaton.Transition> transitions,
                                   List<ValidationMessage> messages) {

    Set<State> statesWithOutgoing = transitions.stream()
        .map(Transition::getFrom)
        .collect(Collectors.toSet());

    for (DeterministicFiniteAutomaton.State state : allStates) {
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
}
