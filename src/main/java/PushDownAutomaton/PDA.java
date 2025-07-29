package PushDownAutomaton;

import common.Automaton;
import static common.Automaton.ValidationMessage.ValidationMessageType;
import java.util.*;

/**
 * Represents a Push-down Automaton (PDA).
 * This class is responsible for parsing a textual description to create a PDA,
 * validating its structure, and providing methods for execution and visualization.
 */
public class PDA extends Automaton {

    private Set<State> states;
    private Set<String> inputAlphabet;
    private Set<String> stackAlphabet;
    private State startState;
    private Set<State> finalStates;
    private String stackStartSymbol;
    private Map<State, List<PDATransition>> transitionMap;
    private Stack stack;

    /**
     * Constructs a new, empty PDA and initializes its type.
     */
    public PDA() {
        super(MachineType.PDA);
        this.stack = new Stack();
    }

    /**
     * Parses a string containing the complete textual definition of the PDA.
     * It populates the automaton's fields (states, transitions, etc.) based on the input.
     *
     * @param inputText The string defining the PDA.
     * @return A {@link ParseResult} object containing the outcome, validation messages, and the automaton itself.
     */
    @Override
    public ParseResult parse(String inputText) {
        List<ValidationMessage> messages = new ArrayList<>();
        this.states = new HashSet<>();
        this.inputAlphabet = new HashSet<>();
        this.stackAlphabet = new HashSet<>();
        this.startState = null;
        this.finalStates = new HashSet<>();
        this.stackStartSymbol = null;
        this.transitionMap = new HashMap<>();

        Map<String, State> stateMap = new HashMap<>();
        String[] lines = inputText.split("\\R");
        Map<String, List<String>> sections = new HashMap<>();
        Map<String, Integer> sectionLineNumbers = new HashMap<>();

        parseSections(lines, sections, sectionLineNumbers, messages);

        if (!validateKeywords(sections, messages)) {
            return new ParseResult(false, messages, null);
        }

        processStates(sections.get("states"), sectionLineNumbers.get("states"), this.states, stateMap, messages);
        if (sections.get("alphabet") != null && !sections.get("alphabet").isEmpty()) {
            this.inputAlphabet.addAll(Arrays.asList(sections.get("alphabet").get(0).split("\\s+")));
        }
        if (sections.get("stack_alphabet") != null && !sections.get("stack_alphabet").isEmpty()) {
            this.stackAlphabet.addAll(Arrays.asList(sections.get("stack_alphabet").get(0).split("\\s+")));
        }

        this.startState = processStartState(sections.get("start"), sectionLineNumbers.get("start"), stateMap, messages);
        this.stackStartSymbol = processStackStartSymbol(sections.get("stack_start"), sectionLineNumbers.get("stack_start"), this.stackAlphabet, messages);
        this.finalStates.addAll(processFinalStates(sections.get("finals"), sectionLineNumbers.get("finals"), stateMap, messages));
        this.transitionMap.putAll(processTransitions(sections.get("transitions"), sectionLineNumbers.get("transitions"), stateMap, this.inputAlphabet, this.stackAlphabet, messages));

        boolean isSuccess = messages.stream().noneMatch(m -> m.getType() == ValidationMessageType.ERROR);
        if (!isSuccess) {
            return new ParseResult(false, messages, this);
        }

        checkForUnreachableStates(this.states, this.startState, this.transitionMap, messages);
        checkForDeadEndStates(this.states, this.finalStates, this.transitionMap, messages);

        return new ParseResult(true, messages, this);
    }

    /**
     * Generates a string in .dot format for visualizing the PDA using Graphviz.
     *
     * @param inputText This parameter is currently unused but required by the abstract parent method.
     * @return A string in .dot format representing the state diagram.
     */
    @Override
    public String toDotCode(String inputText) {
        if (this.states == null || this.states.isEmpty()) {
            return "digraph PDA {\n\tlabel=\"Automaton not parsed or is empty\";\n}";
        }

        StringBuilder dot = new StringBuilder("digraph PDA {\n");
        dot.append("  rankdir=LR;\n");
        dot.append("  node [shape = circle];\n");

        for (State state : finalStates) {
            dot.append(String.format("  \"%s\" [shape=doublecircle];\n", state.getName()));
        }

        if (startState != null) {
            dot.append("  \"\" [shape=point];\n");
            dot.append(String.format("  \"\" -> \"%s\";\n", startState.getName()));
        }

        for (List<PDATransition> transitions : transitionMap.values()) {
            for (PDATransition t : transitions) {
                String label = String.format("%s, %s / %s",
                        t.getInputSymbol(), t.getStackPop(), t.getStackPush());
                dot.append(String.format("  \"%s\" -> \"%s\" [label=\"%s\"];\n",
                        t.getFromState().getName(), t.getToState().getName(), label));
            }
        }

        dot.append("}");
        return dot.toString();
    }

    /**
     * Simulates the PDA on a given input string.
     *
     * @param inputText The input string to be processed by the PDA.
     * @return An {@link ExecutionResult} object containing the result of the simulation.
     */
    @Override
    public ExecutionResult execute(String inputText) {
        return new ExecutionResult(false, null, "Execution for PDA is not implemented yet.");
    }

    /**
     * Performs additional validation checks on the automaton after it has been parsed.
     *
     * @return A list of validation messages.
     */
    @Override
    public List<ValidationMessage> validate() {
        return new ArrayList<>();
    }

    private void parseSections(String[] lines, Map<String, List<String>> sections, Map<String, Integer> lineNumbers, List<ValidationMessage> messages) {
        String currentSection = null;
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty() || line.startsWith("#")) continue;

            int colonIndex = line.indexOf(":");
            if (colonIndex != -1) {
                currentSection = line.substring(0, colonIndex).trim().toLowerCase();
                String data = line.substring(colonIndex + 1).trim();

                if (sections.containsKey(currentSection)) {
                    messages.add(new ValidationMessage("Duplicate keyword '" + currentSection + "'. Only the first definition will be used.", i + 1, ValidationMessageType.WARNING));
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
                messages.add(new ValidationMessage("Undefined content. All content must be under a keyword (e.g., 'states:').", i + 1, ValidationMessageType.ERROR));
            }
        }
    }

    private boolean validateKeywords(Map<String, List<String>> sections, List<ValidationMessage> messages) {
        boolean allFound = true;
        String[] requiredKeys = {"states", "alphabet", "stack_alphabet", "start", "stack_start", "finals", "transitions"};
        for (String key : requiredKeys) {
            if (!sections.containsKey(key)) {
                messages.add(new ValidationMessage("Missing required keyword definition for '" + key + ":'.", 0, ValidationMessageType.ERROR));
                allFound = false;
            }
        }
        return allFound;
    }

    private void processStates(List<String> stateLines, int lineNum, Set<State> states, Map<String, State> stateMap, List<ValidationMessage> messages) {
        if (stateLines == null || stateLines.isEmpty()) {
            messages.add(new ValidationMessage("The 'states:' block cannot be empty.", lineNum, ValidationMessageType.ERROR));
            return;
        }
        String[] stateNames = stateLines.get(0).split("\\s+");
        for (String name : stateNames) {
            State newState = new State(name);
            states.add(newState);
            stateMap.put(name, newState);
        }
    }

    private State processStartState(List<String> lines, int lineNum, Map<String, State> stateMap, List<ValidationMessage> messages) {
        if (lines == null || lines.isEmpty()) {
            messages.add(new ValidationMessage("Start state definition is missing.", lineNum, ValidationMessageType.ERROR));
            return null;
        }
        String startStateName = lines.get(0).trim();
        State startState = stateMap.get(startStateName);
        if (startState == null) {
            messages.add(new ValidationMessage("Start state '" + startStateName + "' is not defined in 'states'.", lineNum, ValidationMessageType.ERROR));
            return null;
        }
        startState.setAsStart(true);
        return startState;
    }

    private String processStackStartSymbol(List<String> lines, int lineNum, Set<String> stackAlphabet, List<ValidationMessage> messages) {
        if (lines == null || lines.isEmpty()) {
            messages.add(new ValidationMessage("Stack start symbol definition is missing.", lineNum, ValidationMessageType.ERROR));
            return null;
        }
        String stackStartSymbol = lines.get(0).trim();
        if (!stackAlphabet.contains(stackStartSymbol)) {
            messages.add(new ValidationMessage("Stack start symbol '" + stackStartSymbol + "' is not defined in 'stack_alphabet'.", lineNum, ValidationMessageType.ERROR));
        }
        return stackStartSymbol;
    }

    private Set<State> processFinalStates(List<String> lines, int lineNum, Map<String, State> stateMap, List<ValidationMessage> messages) {
        Set<State> finalStates = new HashSet<>();
        if (lines == null || lines.isEmpty()) {
            messages.add(new ValidationMessage("Final states definition is missing.", lineNum, ValidationMessageType.ERROR));
            return finalStates;
        }
        String[] finalStateNames = lines.get(0).split("\\s+");
        for (String name : finalStateNames) {
            State finalState = stateMap.get(name);
            if (finalState == null) {
                messages.add(new ValidationMessage("Final state '" + name + "' is not defined in 'states'.", lineNum, ValidationMessageType.ERROR));
                continue;
            }
            finalState.setAsAccept(true);
            finalStates.add(finalState);
        }
        return finalStates;
    }

    private Map<State, List<PDATransition>> processTransitions(List<String> lines, int startLine, Map<String, State> stateMap, Set<String> inputAlphabet, Set<String> stackAlphabet, List<ValidationMessage> messages) {
        Map<State, List<PDATransition>> transitionMap = new HashMap<>();
        if (lines == null) return transitionMap;

        for (int i = 0; i < lines.size(); i++) {
            int currentLine = startLine + i + 1;
            String line = lines.get(i);

            String[] parts = line.split("->");
            if (parts.length != 2) {
                messages.add(new ValidationMessage("Invalid transition format. Rule must contain '->' separator.", currentLine, ValidationMessageType.ERROR));
                continue;
            }

            String[] left = parts[0].trim().split("\\s+");
            String[] right = parts[1].trim().split("\\s+");

            if (left.length != 3) {
                messages.add(new ValidationMessage("Left side of transition must have 3 components: state, input, stack_pop.", currentLine, ValidationMessageType.ERROR));
                continue;
            }
            if (right.length != 2) {
                messages.add(new ValidationMessage("Right side of transition must have 2 components: state, stack_push.", currentLine, ValidationMessageType.ERROR));
                continue;
            }

            State fromState = validateState(left[0], stateMap, currentLine, messages);
            String input = validateInputSymbol(left[1], inputAlphabet, currentLine, messages);
            String pop = validateStackSymbol(left[2], stackAlphabet, currentLine, messages);
            State toState = validateState(right[0], stateMap, currentLine, messages);

            if(!validatePushString(right[1], stackAlphabet, currentLine, messages)){
                continue;
            }

            if (fromState == null || input == null || pop == null || toState == null) {
                continue;
            }

            PDATransition transition = new PDATransition(fromState, input, pop, toState, right[1]);
            transitionMap.computeIfAbsent(fromState, k -> new ArrayList<>()).add(transition);
        }
        return transitionMap;
    }

    private State validateState(String name, Map<String, State> stateMap, int line, List<ValidationMessage> messages) {
        State state = stateMap.get(name);
        if (state == null) {
            messages.add(new ValidationMessage("State '" + name + "' is not defined in 'states'.", line, ValidationMessageType.ERROR));
        }
        return state;
    }

    private String validateInputSymbol(String symbol, Set<String> alphabet, int line, List<ValidationMessage> messages) {
        if (!symbol.equals("eps") && !alphabet.contains(symbol)) {
            messages.add(new ValidationMessage("Input symbol '" + symbol + "' is not defined in 'alphabet'.", line, ValidationMessageType.ERROR));
            return null;
        }
        return symbol;
    }

    private String validateStackSymbol(String symbol, Set<String> alphabet, int line, List<ValidationMessage> messages) {
        if (!symbol.equals("eps") && !alphabet.contains(symbol)) {
            messages.add(new ValidationMessage("Stack symbol '" + symbol + "' is not defined in 'stack_alphabet'.", line, ValidationMessageType.ERROR));
            return null;
        }
        return symbol;
    }

    private boolean validatePushString(String pushString, Set<String> alphabet, int line, List<ValidationMessage> messages) {
        if (!pushString.equals("eps")) {
            for (char c : pushString.toCharArray()) {
                if (!alphabet.contains(String.valueOf(c))) {
                    messages.add(new ValidationMessage("Stack symbol '" + c + "' (in push string '" + pushString + "') is not defined in 'stack_alphabet'.", line, ValidationMessageType.ERROR));
                    return false;
                }
            }
        }
        return true;
    }

    private void checkForUnreachableStates(Set<State> allStates, State startState, Map<State, List<PDATransition>> transitions, List<ValidationMessage> messages) {
        if (startState == null || allStates.isEmpty()) return;

        Set<State> reachableStates = new HashSet<>();
        Queue<State> queue = new LinkedList<>();

        reachableStates.add(startState);
        queue.add(startState);

        while (!queue.isEmpty()) {
            State currentState = queue.poll();
            if (transitions.containsKey(currentState)) {
                for (PDATransition t : transitions.get(currentState)) {
                    if (t.getToState() != null && reachableStates.add(t.getToState())) {
                        queue.add(t.getToState());
                    }
                }
            }
        }

        for (State state : allStates) {
            if (!reachableStates.contains(state)) {
                messages.add(new ValidationMessage("State '" + state.getName() + "' is unreachable from the start state.", 0, ValidationMessageType.WARNING));
            }
        }
    }

    private void checkForDeadEndStates(Set<State> allStates, Set<State> finalStates, Map<State, List<PDATransition>> transitions, List<ValidationMessage> messages) {
        for (State state : allStates) {
            if (!finalStates.contains(state) && transitions.getOrDefault(state, Collections.emptyList()).isEmpty()) {
                messages.add(new ValidationMessage("State '" + state.getName() + "' is a non-final state with no outgoing transitions (dead-end state).", 0, ValidationMessageType.WARNING));
            }
        }
    }
}
