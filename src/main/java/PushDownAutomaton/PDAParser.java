package PushDownAutomaton;

import PushDownAutomaton.Exceptions.*;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class PDAParser {

    private List<ParserWarning> warnings;

    public ParseResult parseFromFile(String filePath) throws ParserException, IOException {
        String content = new String(Files.readAllBytes(Paths.get(filePath)));
        return parse(content);
    }

    public ParseResult parse(String description) throws ParserException {
        this.warnings = new ArrayList<>();

        Set<State> states = new HashSet<>();
        Set<String> inputAlphabet = new HashSet<>();
        Set<String> stackAlphabet = new HashSet<>();
        State startState = null;
        String stackStartSymbol = null;
        Set<State> finalStates = new HashSet<>();
        Map<State, List<PDATransition>> transitionMap = new HashMap<>();

        Map<String, State> stateMap = new HashMap<>();

        String[] lines = description.split("\\R");
        Map<String, List<String>> sections = new HashMap<>();
        Map<String, Integer> sectionLineNumbers = new HashMap<>();

        parseSections(lines, sections, sectionLineNumbers);

        validateKeywords(sections);

        processStates(sections, sectionLineNumbers, states, stateMap);
        inputAlphabet.addAll(Arrays.asList(sections.get("alphabet").get(0).split("\\s+")));
        stackAlphabet.addAll(Arrays.asList(sections.get("stack_alphabet").get(0).split("\\s+")));

        startState = processStartState(sections, sectionLineNumbers, stateMap);
        stackStartSymbol = processStackStartSymbol(sections, sectionLineNumbers, stackAlphabet);
        finalStates.addAll(processFinalStates(sections, sectionLineNumbers, stateMap));

        transitionMap.putAll(processTransitions(sections, sectionLineNumbers, stateMap, inputAlphabet, stackAlphabet));

        checkForUnreachableStates(states, startState, transitionMap);
        checkForDeadEndStates(states, finalStates, transitionMap);

        PDA pda = new PDA(states, inputAlphabet, stackAlphabet, startState, finalStates, stackStartSymbol, transitionMap);
        return new ParseResult(pda, warnings);
    }

    private void parseSections(String[] lines, Map<String, List<String>> sections, Map<String, Integer> lineNumbers) throws SyntaxException {
        String currentSection = null;
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty() || line.startsWith("#")) continue;

            int colonIndex = line.indexOf(":");
            if (colonIndex != -1) {
                currentSection = line.substring(0, colonIndex).trim().toLowerCase();
                String data = line.substring(colonIndex + 1).trim();

                if (sections.containsKey(currentSection)) {
                    warnings.add(new ParserWarning("Duplicate keyword '" + currentSection + "'. Only the first definition will be used.", i + 1));
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
                throw new SyntaxException("Undefined content. All content must be under a keyword (e.g., 'states:').", i + 1);
            }
        }
    }

    private void validateKeywords(Map<String, List<String>> sections) throws SyntaxException {
        String[] requiredKeys = {"states", "alphabet", "stack_alphabet", "start", "stack_start", "finals", "transitions"};
        for (String key : requiredKeys) {
            if (!sections.containsKey(key)) {
                throw new SyntaxException("Missing required keyword definition for '" + key + ":'.", 0);
            }
        }
    }

    private void processStates(Map<String, List<String>> sections, Map<String, Integer> lineNumbers, Set<State> states, Map<String, State> stateMap) throws SyntaxException {
        List<String> stateLines = sections.get("states");
        if (stateLines.isEmpty()) {
            throw new SyntaxException("The 'states:' block cannot be empty.", lineNumbers.get("states"));
        }
        String[] stateNames = stateLines.get(0).split("\\s+");
        for (String name : stateNames) {
            State newState = new ConcreteState(name);
            states.add(newState);
            stateMap.put(name, newState);
        }
    }

    private State processStartState(Map<String, List<String>> sections, Map<String, Integer> lineNumbers, Map<String, State> stateMap) throws SemanticException {
        String startStateName = sections.get("start").get(0).trim();
        State startState = stateMap.get(startStateName);
        if (startState == null) {
            throw new SemanticException("Start state '" + startStateName + "' is not defined in the 'states' list.", lineNumbers.get("start"));
        }
        ((ConcreteState) startState).setAsStart(true);
        return startState;
    }

    private String processStackStartSymbol(Map<String, List<String>> sections, Map<String, Integer> lineNumbers, Set<String> stackAlphabet) throws SemanticException {
        String stackStartSymbol = sections.get("stack_start").get(0).trim();
        if (!stackAlphabet.contains(stackStartSymbol)) {
            throw new SemanticException("Stack start symbol '" + stackStartSymbol + "' is not defined in the 'stack_alphabet' list.", lineNumbers.get("stack_start"));
        }
        return stackStartSymbol;
    }

    private Set<State> processFinalStates(Map<String, List<String>> sections, Map<String, Integer> lineNumbers, Map<String, State> stateMap) throws SemanticException {
        Set<State> finalStates = new HashSet<>();
        String[] finalStateNames = sections.get("finals").get(0).split("\\s+");
        for (String name : finalStateNames) {
            State finalState = stateMap.get(name);
            if (finalState == null) {
                throw new SemanticException("Final state '" + name + "' is not defined in the 'states' list.", lineNumbers.get("finals"));
            }
            ((ConcreteState) finalState).setAsAccept(true);
            finalStates.add(finalState);
        }
        return finalStates;
    }

    private Map<State, List<PDATransition>> processTransitions(Map<String, List<String>> sections, Map<String, Integer> lineNumbers, Map<String, State> stateMap, Set<String> inputAlphabet, Set<String> stackAlphabet) throws ParserException {
        Map<State, List<PDATransition>> transitionMap = new HashMap<>();
        List<String> transitionLines = sections.get("transitions");
        int startLine = lineNumbers.get("transitions");

        for (int i = 0; i < transitionLines.size(); i++) {
            int currentLine = startLine + i + 1;
            String line = transitionLines.get(i);

            String[] parts = line.split("->");
            if (parts.length != 2 || !line.contains("->")) {
                throw new SyntaxException("Invalid transition format. Rule must contain '->' separator.", currentLine);
            }

            String[] left = parts[0].trim().split("\\s+");
            String[] right = parts[1].trim().split("\\s+");

            if (left.length != 3)
                throw new SyntaxException("Left side of transition must have 3 components: (state, input, stack_pop).", currentLine);
            if (right.length != 2)
                throw new SyntaxException("Right side of transition must have 2 components: (state, stack_push).", currentLine);

            State fromState = validateState(left[0], stateMap, currentLine);
            String input = validateInputSymbol(left[1], inputAlphabet, currentLine);
            String pop = validateStackSymbol(left[2], stackAlphabet, currentLine);
            State toState = validateState(right[0], stateMap, currentLine);
            validatePushString(right[1], stackAlphabet, currentLine);

            PDATransition transition = new PDATransition(fromState, input, pop, toState, right[1]);
            transitionMap.computeIfAbsent(fromState, k -> new ArrayList<>()).add(transition);
        }
        return transitionMap;
    }

    private State validateState(String name, Map<String, State> stateMap, int line) throws SemanticException {
        State state = stateMap.get(name);
        if (state == null)
            throw new SemanticException("State '" + name + "' is not defined in the 'states' list.", line);
        return state;
    }

    private String validateInputSymbol(String symbol, Set<String> alphabet, int line) throws SemanticException {
        if (!symbol.equals("eps") && !alphabet.contains(symbol)) {
            throw new SemanticException("Input symbol '" + symbol + "' is not defined in the 'alphabet' list.", line);
        }
        return symbol;
    }

    private String validateStackSymbol(String symbol, Set<String> alphabet, int line) throws SemanticException {
        if (!symbol.equals("eps") && !alphabet.contains(symbol)) {
            throw new SemanticException("Stack symbol '" + symbol + "' is not defined in the 'stack_alphabet' list.", line);
        }
        return symbol;
    }

    private void validatePushString(String pushString, Set<String> alphabet, int line) throws SemanticException {
        if (!pushString.equals("eps")) {
            for (char c : pushString.toCharArray()) {
                if (!alphabet.contains(String.valueOf(c))) {
                    throw new SemanticException("Stack symbol '" + c + "' (in push string '" + pushString + "') is not defined in 'stack_alphabet'.", line);
                }
            }
        }
    }

    private void checkForUnreachableStates(Set<State> allStates, State startState, Map<State, List<PDATransition>> transitions) {
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
                warnings.add(new ParserWarning("State '" + state.getName() + "' is defined but is unreachable from the start state.", 0));
            }
        }
    }

    private void checkForDeadEndStates(Set<State> allStates, Set<State> finalStates, Map<State, List<PDATransition>> transitions) {
        for (State state : allStates) {
            if (!finalStates.contains(state) && !transitions.containsKey(state)) {
                warnings.add(new ParserWarning("State '" + state.getName() + "' is a non-final state with no outgoing transitions (dead-end state).", 0));
            }
        }
    }
}