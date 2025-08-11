package NondeterministicFiniteAutomaton;

import common.Automaton;
import common.Automaton.ValidationMessage.ValidationMessageType;
import common.State;
import common.Symbol;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents a Non-deterministic Finite Automaton (NFA).
 * <p>
 * This class provides functionality for parsing a textual representation of an NFA,
 * validating its structure, and producing its DOT format for visualization.
 * It supports states, alphabet, transitions, and distinguishes between start and final states.
 * </p>
 * <p>
 * The expected input format includes:
 * <ul>
 *   <li>A "start:" line with exactly one start state.</li>
 *   <li>A "finals:" line with one or more final states.</li>
 *   <li>An "alphabet:" line with valid characters.</li>
 *   <li>A "transitions:" section with valid state-to-state transitions using symbols from the alphabet.</li>
 * </ul>
 * </p>
 */
public class NFA extends Automaton {

    private static final String transitionPattern = "(?:(q\\d+)|.) ?-> ?(?:(q\\d+)|\\S*)? (?:(\\((?:[a-zA-Z]|eps)(?:\\s(?:[a-zA-Z]|eps))*\\s?\\))|.+)?";
    private static final String transitionSymbolPattern = "\\((?:[a-zA-Z]|eps)(?:\\s(?:[a-zA-Z]|eps))*\\s?\\)";
    private static final String statePattern = "q\\d+";

    private Map<String, State> states;
    private Set<Symbol> alphabet;
    private State startState;
    private Set<State> finalStates;
    private Map<State, List<Transition>> transitions;

    /**
     * Constructs an NFA with specified states, alphabet, start state, final states, and transitions.
     *
     * @param states       map of state names to State objects
     * @param alphabet     set of symbols in the alphabet
     * @param startState   the starting state
     * @param finalStates  set of final states
     * @param transitions  map of states to their outgoing transitions
     */
    public NFA(Map<String, State> states, Set<Symbol> alphabet, State startState, Set<State> finalStates, Map<State, List<Transition>> transitions) {
        super(MachineType.NFA);
        this.states = states;
        this.alphabet = alphabet;
        this.startState = startState;
        this.finalStates = finalStates;
        this.transitions = transitions;
    }

    /**
     * Constructs an empty NFA with initialized data structures.
     */
    public NFA(){
        super(MachineType.NFA);
        this.states = new HashMap<>();
        this.alphabet = new HashSet<>();
        this.startState = null;
        this.finalStates = new HashSet<>();
        this.transitions = new HashMap<>();
    }

    /**
     * Parses the input text to create the NFA structure.
     *
     * @param inputText textual representation of the NFA
     * @return {@link ParseResult} indicating success/failure and any validation messages
     */
    @Override
    public ParseResult parse(String inputText) {

        List<ValidationMessage> warnings = new ArrayList<>();
        try {
            warnings = handleLines(inputText);
        } catch (IOException e) {
            warnings.add(new ValidationMessage("Error reading input", -1, ValidationMessageType.ERROR));
            System.out.println("Error reading input: " + e.getMessage());
            return new ParseResult(false, warnings, null);
        }

        int errorCount = 0;
        int warnCount = 0;
        int infoCount = 0;

        for (ValidationMessage warning : warnings) {
            if (warning.getType() == ValidationMessageType.ERROR) {
                errorCount++;
            } else if (warning.getType() == ValidationMessageType.WARNING) {
                warnCount++;
            } else if (warning.getType() == ValidationMessageType.INFO) {
                infoCount++;
            }
        }

        ParseResult parseResult;

        if (errorCount > 0) {
            parseResult = new ParseResult(false, warnings, null);
            System.out.println("\n NFA parsing unsuccessful " + errorCount + " error(s), " + warnCount + " warning(s) and " + infoCount + " info(s).\n");
        }else {
            parseResult = new ParseResult(true, warnings, this);
            System.out.println("\n Successfully parsed NFA with " + errorCount + " error(s) and " +  warnCount + " warning(s) " + infoCount + " info(s).\n");
        }

        for (ValidationMessage warning : warnings) {
            System.out.println(warning);
        }


        return parseResult;
    }

    /**
     * Executes the NFA on the given input text.
     *
     * @param inputText input string to execute on the NFA
     * @return {@link ExecutionResult}
     */
    public ExecutionResult execute(String inputText) {
        List<ValidationMessage> runtimeMessages = new ArrayList<>();
        StringBuilder trace = new StringBuilder();

        Set<State> currentStates = new LinkedHashSet<>();
        Set<State> nextStates = new LinkedHashSet<>();

        if (this.startState == null) {
            runtimeMessages.add(new ValidationMessage("Start state is not defined", -1, ValidationMessageType.ERROR));
            return new ExecutionResult(false, runtimeMessages, "");
        }

        currentStates.add(this.startState);
        trace.append("Start state: ").append(this.startState.getName()).append("\n");


        for (State s : getEpsilonClosure(currentStates)) {
            trace.append("Epsilon-closure includes: ").append(s.getName()).append("\n");
            currentStates.add(s);
        }

        for (char c : inputText.toCharArray()) {
            Symbol inputSymbol = new Symbol(c);

            if (!this.alphabet.contains(inputSymbol)) {
                runtimeMessages.add(new ValidationMessage("Symbol not in alphabet: " + c, -1, ValidationMessageType.ERROR));
                return new ExecutionResult(false, runtimeMessages, trace.toString());
            }

            for (State state : currentStates) {

                for (Transition t : this.transitions.getOrDefault(state, Collections.emptyList())) {
                    if (t.getSymbol().equals(inputSymbol)) {
                        nextStates.add(t.getTo());
                        trace.append("Transition: ").append(state.getName())
                                .append(" --").append(c).append("--> ")
                                .append(t.getTo().getName()).append("\n");
                    }
                }

            }

            for (State s : getEpsilonClosure(currentStates)) {
                trace.append("Epsilon-closure includes: ").append(s.getName()).append("\n");
                nextStates.add(s);
            }

            currentStates = new LinkedHashSet<>(nextStates);
            nextStates.clear();
        }

        currentStates.addAll(getEpsilonClosure(currentStates));

        for (State state : currentStates) {
            if (state.isAccept()) {
                return new ExecutionResult(true, runtimeMessages, trace.toString());
            }
        }

        return new ExecutionResult(false, runtimeMessages, trace.toString());
    }

    /**
     * Computes the epsilon-closure of a set of states.
     * The epsilon-closure is the set of states reachable from the given states via epsilon transitions.
     *
     * @param states the set of states from which to compute the epsilon-closure
     * @return the set of states reachable via epsilon transitions
     */
    private Set<State> getEpsilonClosure(Set<State> states) {
        Set<State> queue = new LinkedHashSet<>(states);
        Set<State> visited = new HashSet<>();
        Set<State> returnQueue = new LinkedHashSet<>();

        for (State state : queue){
            for (Transition t : this.transitions.getOrDefault(state, Collections.emptyList())) {
                if (t.getSymbol().isEpsilon() && !visited.contains(t.getTo())) {
                    visited.add(t.getTo());
                    queue.add(t.getTo());
                    returnQueue.add(t.getTo());
                }
            }

        }

        return returnQueue;
    }

    /**
     * Computes the epsilon-closure of a single state.
     *
     * @param state the state from which to compute the epsilon-closure
     * @return the set of states reachable via epsilon transitions
     */
    private Set<State> getEpsilonClosure(State state){
        Set<State> set = new LinkedHashSet<>();
        set.add(state);
        return getEpsilonClosure(set);
    }


    /**
     * Parses each line of the input text and delegates validation based on section headers (start, finals, alphabet, transitions).
     * Keeps track of line numbers and section presence for post-parse validation.
     * Collects warnings or errors encountered during the parsing of individual lines or sections.
     *
     * @param inputText full multiline string representing the NFA
     * @return list of ValidationMessages indicating any issues found during parsing
     * @throws IOException if an error occurs while reading the input
     */
    private List<ValidationMessage> handleLines(String inputText) throws IOException {

        this.states = new HashMap<>();
        this.alphabet =  new HashSet<>();
        this.startState = null;
        this.finalStates =  new HashSet<>();
        this.transitions =  new HashMap<>();

        Map<String, Boolean> lineBoolMap = new HashMap<>();
        lineBoolMap.put("startLine", false);
        lineBoolMap.put("finalsLine", false);
        lineBoolMap.put("alphaLine", false);
        lineBoolMap.put("transitionsLine", false);
        lineBoolMap.put("isNextLineTransition", false);

        //startLineNo, finalsLineNo, alphaLineNo
        Map<String, Integer> lineNumbers = new HashMap<>();
        lineNumbers.put("currentLineNo", 0);
        lineNumbers.put("startLineNo", -1);
        lineNumbers.put("finalsLineNo", -1);
        lineNumbers.put("alphaLineNo", -1);

        List<ValidationMessage> warnings = new ArrayList<>();

        BufferedReader reader = new BufferedReader(new StringReader(inputText));
        String line;
        while ((line = reader.readLine()) != null){
            line = line.trim();
            int count = lineNumbers.getOrDefault("currentLineNo",0);
            lineNumbers.put("currentLineNo", count + 1);

            if (line.startsWith("#")) {
                //comment line
                continue;
            }

            if (line.toLowerCase().startsWith("start:")){
                lineBoolMap.put("isNextLineTransition", false);

                if (!lineBoolMap.get("startLine")){
                    lineBoolMap.put("startLine", true);
                    lineNumbers.put("startLineNo", lineNumbers.get("currentLineNo"));

                    warnings.addAll(handleStartStateLine(line, lineNumbers.get("currentLineNo")));
                }else {
                    //there is already a start line
                    warnings.add(new ValidationMessage("There is already a start line", lineNumbers.get("currentLineNo"), ValidationMessageType.ERROR));
                    continue;
                }

            } else
            if (line.toLowerCase().startsWith("finals:")) {
                lineBoolMap.put("isNextLineTransition", false);

                if (!lineBoolMap.get("finalsLine")){
                    lineBoolMap.put("finalsLine", true);
                    lineNumbers.put("finalsLineNo", lineNumbers.get("currentLineNo"));

                    warnings.addAll(handleFinalStatesLine(line, lineNumbers.get("currentLineNo")));
                } else {
                    //there is already a finals line
                    warnings.add(new ValidationMessage("There is already a finals line", lineNumbers.get("currentLineNo"), ValidationMessageType.ERROR));
                    continue;
                }

            } else
            if (line.toLowerCase().startsWith("alphabet:")) {
                lineBoolMap.put("isNextLineTransition", false);

                if (!lineBoolMap.get("alphaLine")){
                    lineBoolMap.put("alphaLine", true);
                    lineNumbers.put("alphaLineNo", lineNumbers.get("currentLineNo"));

                    warnings.addAll(handleAlphabetLine(line, lineNumbers.get("currentLineNo")));
                }else {
                    //there is already an alphabet line
                    warnings.add(new ValidationMessage("There is already a alphabet line", lineNumbers.get("currentLineNo"),  ValidationMessageType.ERROR));
                    continue;
                }

            } else
            if (line.toLowerCase().startsWith("transitions:")) {
                lineBoolMap.put("isNextLineTransition", true);
                if (!lineBoolMap.get("transitionsLine")){
                    lineBoolMap.put("transitionsLine", true);
                }else {
                    //there is already a transitions line
                    warnings.add(new ValidationMessage("There is already a \"transitions:\" line", lineNumbers.get("currentLineNo"),  ValidationMessageType.ERROR));
                    continue;
                }

                if (!line.trim().equals("transitions:")) {
                    warnings.add(new ValidationMessage("\"transitions:\" line should only contain \"transitions:\"", lineNumbers.get("currentLineNo"), ValidationMessageType.ERROR));
                }

            } else
            if (line.contains("->")) {

                if (!lineBoolMap.get("isNextLineTransition")) {
                    //no "transitions:" line
                    warnings.add(new ValidationMessage("No \"transitions:\" line for this transition", lineNumbers.get("currentLineNo"), ValidationMessageType.ERROR));
                }else {
                    warnings.addAll(handleTransitionLines(line, lineNumbers.get("currentLineNo")));
                }

            } else
            if (!line.isEmpty()) {

                warnings.add(new ValidationMessage("Skipping Unknown Line: " + line, lineNumbers.get("currentLineNo"), ValidationMessageType.WARNING));
                lineBoolMap.put("isNextLineTransition", false);

            }

        }

        warnings.addAll(runPostParseChecks(lineNumbers, lineBoolMap));

        return warnings;
    }

    /**
     * Parses and validates the "start:" line from the NFA definition.
     * Ensures exactly one valid start state is provided and marks it in the states map.
     *
     * @param line   the input line containing the start state
     * @param lineNo the line number for error reporting
     * @return a list of ValidationMessages indicating syntax or semantic issues
     */
    private List<ValidationMessage> handleStartStateLine(String line, int lineNo){

        List<ValidationMessage> warnings = new ArrayList<>();

        String[] arr = line.split(" ");

        if (arr.length < 2){
            //no starts state
            warnings.add(new ValidationMessage("No start state", lineNo, ValidationMessageType.ERROR));
            return warnings;
        } else if (arr.length > 2) {
            //more than 1 start state
            warnings.add(new ValidationMessage("More than 1 start state", lineNo, ValidationMessageType.ERROR));
            return warnings;
        } else {

            String stateName = arr[1].trim();
            if (stateName.matches(statePattern)){ //q then number
                //correct
                System.out.println("Start line is correct: " + line);

                if (this.states.containsKey(stateName)){
                    this.states.get(stateName).setStart(true);
                }else {
                    this.states.put(stateName, new State(stateName, true, false));
                }
                this.startState = this.states.get(stateName);

            }else {
                //wrong state name
                warnings.add(new ValidationMessage("Wrong start state name: " + stateName, lineNo,  ValidationMessageType.ERROR));
                return warnings;
            }

        }

        return warnings;
    }

    /**
     * Parses and validates the "finals:" line from the NFA definition.
     * Ensures that all listed states follow the correct naming convention and are marked as final states.
     * Adds new states to the internal state map if they do not already exist.
     *
     * @param line the input line containing final states
     * @param lineNo the line number for error reporting
     * @return list of validation messages indicating any syntax errors
     */
    private List<ValidationMessage> handleFinalStatesLine(String line, int lineNo){
        List<ValidationMessage> warnings = new ArrayList<>();

        String[] arr = line.split(" ");

        if (arr.length < 2){
            //no state
            warnings.add(new ValidationMessage("No final state", lineNo,  ValidationMessageType.ERROR));
            return warnings;
        }else {
            boolean isCorrect = true;
            for (int i = 1; i < arr.length; i++) {
                if (!arr[i].matches(statePattern)){
                    //wrong state naming
                    isCorrect = false;
                    warnings.add(new ValidationMessage("Wrong final state name: " + arr[i], lineNo,  ValidationMessageType.ERROR));
                }
            }
            if (isCorrect){
                System.out.println("Final state is correct: " + line);
                for (int i = 1; i < arr.length; i++) {

                    if (this.states.containsKey(arr[i])){
                        this.states.get(arr[i]).setAccept(true);
                        this.finalStates.add(this.states.get(arr[i]));
                    }else {
                        State finalState = new State(arr[i], false, true);
                        this.states.put(arr[i], finalState);
                        this.finalStates.add(finalState);
                    }

                }

            }

        }

        return warnings;
    }

    /**
     * Parses and validates the "alphabet:" line from the NFA definition.
     * Checks each symbol for validity (must be a single letter and not 'eps') and detects duplicates.
     *
     * @param line   the input line containing alphabet symbols
     * @param lineNo the line number for error reporting
     * @return a list of ValidationMessages describing any format or semantic errors
     */
    private List<ValidationMessage> handleAlphabetLine(String line, int lineNo){
        List<ValidationMessage> warnings = new ArrayList<>();

        String[] arr = line.split(" ");

        if (arr.length < 2){
            //no letter
            warnings.add(new ValidationMessage("No alphabet letter", lineNo,   ValidationMessageType.ERROR));
        }else {
            boolean isCorrect = true;
            for (int i = 1; i < arr.length; i++) {
                if (arr[i].equals("eps")){
                    //wrong letter naming
                    isCorrect = false;
                    warnings.add(new ValidationMessage("Alphabet letter cannot be eps", lineNo, ValidationMessageType.ERROR));
                }

                if (arr[i].length() != 1){
                    //not a character
                    isCorrect = false;
                    warnings.add(new ValidationMessage("Alphabet is not a character: " + arr[i], lineNo,  ValidationMessageType.ERROR));
                }else {
                    if (!Character.isLetter(arr[i].charAt(0))) {
                        //char is not a letter
                        isCorrect = false;
                        warnings.add(new ValidationMessage("Alphabet is not a letter: " + arr[i], lineNo,  ValidationMessageType.ERROR));
                    }else {
                        //correct letter
                        Symbol symbol = new Symbol(arr[i].charAt(0));
                        if (this.alphabet.contains(symbol)) {
                            //duplicate letter
                            warnings.add(new ValidationMessage("Duplicate letter: " + arr[i], lineNo,  ValidationMessageType.WARNING));
                        }
                        this.alphabet.add(symbol);
                    }
                }

            }
            if (isCorrect){
                System.out.println("Alphabet syntax is correct: " + line);
            }

        }

        return warnings;
    }

    /**
     * Parses and validates a single transition line.
     * Ensures correct syntax for state names and symbols, verifies alphabet inclusion, and adds transitions.
     *
     * @param line   the input line representing a transition
     * @param lineNo the line number for error reporting
     * @return a list of ValidationMessages representing any issues found in the transition
     */
    private List<ValidationMessage> handleTransitionLines(String line, int lineNo){
        List<ValidationMessage> warnings = new ArrayList<>();

        Pattern pattern = Pattern.compile(transitionPattern);
        Matcher matcher = pattern.matcher(line);
        //System.out.println("group count: " + matcher.groupCount());
        String wrongPart = line;
        String message = "";
        String firstStateName = null;
        String secondStateName = null;
        if (matcher.find()) {
            wrongPart = wrongPart.replace("->", "");
            if (matcher.group(1) != null && matcher.group(1).matches(statePattern)) {
                //System.out.println("first state correct");
                //System.out.println(matcher.group(1));
                firstStateName = matcher.group(1);
                wrongPart = wrongPart.replace(matcher.group(1),"");
            }else {
                message += "First state incorrect \n";
            }

            if (matcher.group(2) != null && matcher.group(2).matches(statePattern)) {
                //System.out.println("second state correct");
                //System.out.println(matcher.group(2));
                secondStateName = matcher.group(2);
                wrongPart = wrongPart.replace(matcher.group(2),"");
            }else {
                message += "Second state incorrect \n";
            }

            if (matcher.group(3) != null && matcher.group(3).matches(transitionSymbolPattern)) {
                //System.out.println("transition letters correct");
                //System.out.println(matcher.group(3));
                wrongPart = wrongPart.replace(matcher.group(3),"");
            }else {
                message += "Transition letters incorrect \n";
            }
            wrongPart = wrongPart.trim();

        }else {
            message = "Whole line is wrong ";
        }

        if (!message.isEmpty() || !wrongPart.isEmpty()) {
            warnings.add(new ValidationMessage("Wrong transition syntax: " + message + " Wrong part: " + "\"" + wrongPart + "\"",
                    lineNo, ValidationMessageType.ERROR));
        }else {
            //syntax correct, check for duplicate
            boolean alreadyExists = false;
            if (firstStateName != null && this.states.get(firstStateName) != null) {
                State fromState = this.states.get(firstStateName);
                if (this.transitions.get(fromState) != null) {
                    for (Transition t : this.transitions.get(fromState)) {
                        if (t.getTo().getName().equals(secondStateName)) {
                            alreadyExists = true;
                            break;
                        }
                    }
                }
            }

            if (alreadyExists) {
                warnings.add(new ValidationMessage("There is already this transition: " + firstStateName + " -> " + secondStateName,
                        lineNo, ValidationMessageType.ERROR));
                //System.out.println("Transition already exists");
            }

            if (firstStateName != null && secondStateName != null) {

                this.states.putIfAbsent(firstStateName, new State(firstStateName));
                this.states.putIfAbsent(secondStateName, new State(secondStateName));

                State fromState = this.states.get(firstStateName);
                State toState = this.states.get(secondStateName);

                String transitionName = matcher.group(3);
                String[] symbols = transitionName.substring(1, transitionName.length()-1).split("\\s");


                List<Transition> transitionList = new ArrayList<>();
                for (String symbol : symbols) {
                    Symbol symbolTemp;
                    if (symbol.equals("eps")) {
                        symbolTemp = new Symbol('_');
                    }else{
                        symbolTemp = new Symbol(symbol.charAt(0));
                    }
                    if (!this.alphabet.contains(symbolTemp) && !symbol.equals("eps")) {
                        //alphabet does not contain transition symbol
                        warnings.add(new ValidationMessage("Alphabet does not contain transition symbol: " + symbol,
                                lineNo, ValidationMessageType.ERROR));
                        continue;
                    }
                    Transition transition = new Transition(fromState,toState,symbolTemp);
                    if (!alreadyExists){
                        if (transitionList.contains(transition)) {
                            warnings.add(new ValidationMessage("Duplicate transition symbol: " + symbolTemp.getValue(), lineNo, ValidationMessageType.ERROR));
                        }else {
                            //unique transition and unique symbol
                            transitionList.add(transition);
                        }
                    }
                }

                if (!transitionList.isEmpty()) {
                    if (this.transitions.containsKey(fromState)){
                        this.transitions.get(fromState).addAll(transitionList);
                    }else {
                        this.transitions.put(fromState,transitionList);
                    }
                    System.out.println("Transition correct: " + line);
                    //correct
                }
            }
        }

        return warnings;
    }

    /**
     * Runs post-parsing checks to ensure completeness of NFA specification.
     * Validates presence of start line, final line, alphabet line, transitions, and symbol usage.
     *
     * @param lineNumbers    map containing line numbers for reference
     * @param lineBoolMap    map tracking presence of key lines in the input
     * @return a list of ValidationMessages representing any missing or unused components
     */
    private List<ValidationMessage> runPostParseChecks(Map<String, Integer> lineNumbers, Map<String, Boolean> lineBoolMap){
        List<ValidationMessage> warnings = new ArrayList<>();

        for (Symbol symbol : this.alphabet) {
            boolean found = false;
            for (List<Transition> transitions : this.transitions.values()) {
                for (Transition transition : transitions) {
                    if (transition.getSymbol().equals(symbol)) {
                        found = true;
                        break;
                    }
                }
                if (found) {
                    break;
                }
            }
            if (!found) {
                warnings.add(new ValidationMessage("Symbol not used: " + symbol.getValue(), lineNumbers.get("alphaLineNo"), ValidationMessageType.WARNING));
            }
        }

        if (!lineBoolMap.get("startLine")){
            warnings.add(new ValidationMessage("There is no start line", -1, ValidationMessageType.ERROR));
        }
        if (!lineBoolMap.get("finalsLine")){
            warnings.add(new ValidationMessage("There is no final line", -1, ValidationMessageType.ERROR));
        }
        if (!lineBoolMap.get("alphaLine")){
            warnings.add(new ValidationMessage("There is no alphabet line", -1, ValidationMessageType.ERROR));
        }
        if (!lineBoolMap.get("isNextLineTransition")){
            warnings.add(new ValidationMessage("There is no \"transitions\" line", -1, ValidationMessageType.WARNING));
        }
        if (this.transitions.isEmpty()){
            warnings.add(new ValidationMessage("There are no correct transitions", -1, ValidationMessageType.WARNING));
        }
        return warnings;
    }

    /**
     * Validates the internal consistency of the NFA structure.
     *
     * @return list of {@link ValidationMessage} objects indicating errors or warnings
     */
    @Override
    public List<ValidationMessage> validate(){
        List<ValidationMessage> validationWarnings = new ArrayList<>();

        validationWarnings.addAll(validateStartState());

        validationWarnings.addAll(validateFinalStates());

        validationWarnings.addAll(validateAlphabet());

        validationWarnings.addAll(validateTransitions());

        validationWarnings.addAll(validateStates());

        for (ValidationMessage warning : validationWarnings){
            System.out.println(warning);
        }

        return validationWarnings;
    }

    /**
     * Validates the start state of the NFA.
     * Checks whether it is properly defined and consistent with the states map.
     *
     * @return list of {@link ValidationMessage} objects indicating any issues with the start state
     */
    private List<ValidationMessage> validateStartState(){
        List<ValidationMessage> validationWarnings = new ArrayList<>();

        Map<String, State> states = this.states;
        State startState = this.startState;

        if (startState == null){
            validationWarnings.add(new ValidationMessage("No start state in NFA object", -1, ValidationMessageType.ERROR));
        }else{
            if (!startState.isStart()){
                validationWarnings.add(new ValidationMessage("Start state is not a start state", -1, ValidationMessageType.ERROR));
            }
            if (!startState.getName().matches(NFA.statePattern)){
                validationWarnings.add(new ValidationMessage("Start state does not match valid state name", -1, ValidationMessageType.ERROR));
            }
            if (!states.containsKey(startState.getName())){
                validationWarnings.add(new ValidationMessage("States map does not contain the start state", -1, ValidationMessageType.ERROR));
            }else {
                if (!states.get(startState.getName()).equals(startState)){
                    validationWarnings.add(new ValidationMessage("Start state in states map is not equal to the start state", -1, ValidationMessageType.ERROR));
                }
            }
        }

        return validationWarnings;
    }

    /**
     * Validates the final states of the NFA.
     * Ensures they are marked correctly and present in the states map.
     *
     * @return list of {@link ValidationMessage} objects indicating any issues with final states
     */
    private List<ValidationMessage> validateFinalStates(){
        List<ValidationMessage> validationWarnings = new ArrayList<>();

        Set<State> finalStates = this.finalStates;
        if (finalStates == null || finalStates.isEmpty()){
            validationWarnings.add(new ValidationMessage("No final state in NFA object", -1, ValidationMessageType.ERROR));
        }else {
            for (State finalState : finalStates){
                if (!finalState.isAccept()){
                    validationWarnings.add(new ValidationMessage("Final state: " + finalState.getName() + " is not a final state", -1, ValidationMessageType.ERROR));
                }
                if (!finalState.getName().matches(NFA.statePattern)){
                    validationWarnings.add(new ValidationMessage("Final state: " + finalState.getName() + " does not match valid state name", -1, ValidationMessageType.ERROR));
                }
                if (!states.containsKey(finalState.getName())){
                    validationWarnings.add(new ValidationMessage("States map does not contain the final state: " + finalState.getName(), -1, ValidationMessageType.ERROR));
                }else {
                    if (!states.get(finalState.getName()).equals(finalState)){
                        validationWarnings.add(new ValidationMessage("Final state in states map is not equal to the final state: " + finalState.getName(), -1, ValidationMessageType.ERROR));
                    }
                }
            }
        }

        return validationWarnings;
    }

    /**
     * Validates the alphabet symbols of the NFA.
     * Ensures each symbol is valid and properly formatted.
     *
     * @return list of {@link ValidationMessage} objects indicating issues with alphabet symbols
     */
    private List<ValidationMessage> validateAlphabet(){
        List<ValidationMessage> validationWarnings = new ArrayList<>();

        Set<Symbol> alphabet = this.alphabet;
        if (alphabet == null || alphabet.isEmpty()){
            validationWarnings.add(new ValidationMessage("Alphabet is empty", -1, ValidationMessageType.ERROR));
        }else {
            for (Symbol symbol : alphabet){
                validationWarnings.addAll(validateSymbol(symbol));
            }
        }

        return validationWarnings;
    }

    /**
     * Validates the transitions of the NFA.
     * Checks for valid source and target states and verifies that symbols exist in the alphabet.
     *
     * @return list of {@link ValidationMessage} objects indicating any transition-related issues
     */
    private List<ValidationMessage> validateTransitions(){
        List<ValidationMessage> validationWarnings = new ArrayList<>();

        Map<State, List<Transition>> transitions = this.transitions;
        for (State fromState : transitions.keySet()){
            if (!states.containsKey(fromState.getName())){
                validationWarnings.add(new ValidationMessage("States map does not contain the transition fromState: " + fromState.getName(), -1, ValidationMessageType.ERROR));
            }
            if (!fromState.getName().matches(NFA.statePattern)){
                validationWarnings.add(new ValidationMessage("Transition fromState: " + fromState.getName() + " does not match valid state name", -1, ValidationMessageType.ERROR));
            }

            if (transitions.get(fromState) != null) {
                for (Transition t : transitions.get(fromState)){
                    List<ValidationMessage> transitionSymbolWarnings = validateSymbol(t.getSymbol());
                    validationWarnings.addAll(transitionSymbolWarnings);

                    if (alphabet != null && !t.getSymbol().isEpsilon() && !alphabet.contains(t.getSymbol())){
                        validationWarnings.add(new ValidationMessage("Alphabet does not contain transition symbol: " + t.getSymbol().getValue(), -1, ValidationMessageType.ERROR));
                    }

                    State toState = t.getTo();
                    if (!states.containsKey(toState.getName())){
                        validationWarnings.add(new ValidationMessage("States map does not contain the transition toState: " + toState.getName(), -1, ValidationMessageType.ERROR));
                    }
                    if (!toState.getName().matches(NFA.statePattern)){
                        validationWarnings.add(new ValidationMessage("Transition toState: " + toState.getName() + " does not match valid state name", -1, ValidationMessageType.ERROR));
                    }
                }
            }
        }
        return validationWarnings;
    }

    /**
     * Validates the consistency and correctness of state definitions.
     * Checks for proper naming, counts of start/final states, and reports errors accordingly.
     *
     * @return list of {@link ValidationMessage} objects indicating any issues found
     */
    private List<ValidationMessage> validateStates(){
        List<ValidationMessage> validationWarnings = new ArrayList<>();

        int startCount = 0;
        int finalCount = 0;
        for (State state : states.values()){
            if (!state.getName().matches(NFA.statePattern)){
                validationWarnings.add(new ValidationMessage("State name: " + state.getName() + " does not match valid state name", -1, ValidationMessageType.ERROR));
            }
            if (state.isStart()){
                startCount++;
            }
            if (state.isAccept()){
                finalCount++;
            }
        }

        if (startCount == 0){
            validationWarnings.add(new ValidationMessage("No start state in states map", -1, ValidationMessageType.ERROR));
        }else if (startCount > 1){
            validationWarnings.add(new ValidationMessage("More than one start state in states map", -1, ValidationMessageType.ERROR));
        }

        if (finalCount == 0){
            validationWarnings.add(new ValidationMessage("No final state in states map", -1, ValidationMessageType.ERROR));
        }

        return validationWarnings;
    }


    /**
     * Validates a given symbol to ensure it is either a letter or an epsilon symbol.
     * Symbols must be alphabetic characters or represent an epsilon transition.
     *
     * @param symbol the symbol to validate
     * @return a list of ValidationMessages indicating any validation errors
     */
    private List<ValidationMessage> validateSymbol(Symbol symbol){
        List<ValidationMessage> warnings = new ArrayList<>();

        if (!(symbol.isEpsilon() || Character.isLetter(symbol.getValue()))) {
            warnings.add(new ValidationMessage("Symbol is not valid: " + symbol.getValue(), -1, ValidationMessage.ValidationMessageType.ERROR));
        }
        return warnings;
    }

    /**
     * Generates a DOT language representation of the NFA for visualization.
     *
     * @param inputText the original input (not used in DOT generation)
     * @return DOT format string
     */
    @Override
    public String toDotCode(String inputText) {
        if (this.states == null || this.states.isEmpty()) {
            return "digraph NFA {\n\tlabel=\"Automaton not parsed or is empty\";\n}";
        }

        StringBuilder dot = new StringBuilder("digraph NFA {\n");
        dot.append("  rankdir=LR;\n");
        dot.append("  node [shape = circle];\n");

        for (State state : finalStates) {
            dot.append(String.format("  \"%s\" [shape=doublecircle];\n", state.getName()));
        }

        if (startState != null) {
            dot.append("  \"start\" [shape=point];\n");
            dot.append(String.format("  \"start\" -> \"%s\";\n", startState.getName()));
        }

        List<Transition> addedTransitions = new ArrayList<>();

        for (List<Transition> transitions : transitions.values()) {
            for (Transition t : transitions) {
                if (!addedTransitions.contains(t)) {
                    addedTransitions.add(t);

                    List<Transition> sameTransitionsFromState = getSameTransitionsFromState(t.getFrom(), t.getTo());

                    List<String> symbols = new ArrayList<>();
                    boolean epsilonExists = false;

                    for (Transition sameTransition : sameTransitionsFromState) {
                            addedTransitions.add(sameTransition);

                            if (sameTransition.getSymbol().isEpsilon()){
                                epsilonExists = true;
                            }else {
                                symbols.add(String.valueOf(sameTransition.getSymbol().getValue()));
                            }
                    }

                    String label = String.join(", ", symbols);

                    dot.append(String.format("  \"%s\" -> \"%s\" [label=\"%s\"];\n",
                            t.getFrom().getName(), t.getTo().getName(), label));

                    if (epsilonExists){
                        dot.append(String.format("  \"%s\" -> \"%s\" [label=\"%s\", style=\"dashed\"];\n",
                                t.getFrom().getName(), t.getTo().getName(), "ε"));
                    }

                }

            }
        }

        dot.append("}");
        return dot.toString();
    }

    /**
     * Returns a list of transitions from a specific source state to a target state.
     *
     * @param fromState the source state
     * @param toState the destination state
     * @return list of transitions between the given states
     */
    private List<Transition> getSameTransitionsFromState(State fromState, State toState){

        List<Transition> transitions = new ArrayList<>();

        for (Transition t : this.transitions.get(fromState)){
            if (t.getTo().equals(toState)){
                transitions.add(t);
            }
        }

        return transitions;
    }

    /**
     * Returns a human-readable string representation of the NFA's components.
     *
     * @return formatted string representing the NFA
     */
    public String prettyPrint(){
        StringBuilder sb = new StringBuilder();

        sb.append("NFA\n");
        sb.append("States:\n");
        for (State state : this.states.values()){
            sb.append(state.prettyPrint()).append("\n");
        }

        sb.append("Alphabet:\n");
        for (Symbol symbol : this.alphabet){
            sb.append(symbol.prettyPrint()).append("\n");
        }

        sb.append("StartState:\n");
        sb.append(this.startState.prettyPrint()).append("\n");

        sb.append("FinalStates:\n");
        for (State state : this.finalStates){
            sb.append(state.prettyPrint()).append("\n");
        }

        sb.append("Transitions:\n");
        for (List<Transition> transitions : this.transitions.values()){
            for (Transition transition : transitions){
                sb.append(transition.prettyPrint()).append("\n");
            }
        }

        return sb.toString();
    }

    @Override
    public String toString() {
        return super.toString();
    }

    public Map<String, State> getStates() {
        return states;
    }

    public Set<Symbol> getAlphabet() {
        return alphabet;
    }

    public State getStartState() {
        return startState;
    }

    public Set<State> getFinalStates() {
        return finalStates;
    }

    public Map<State, List<Transition>> getTransitions() {
        return transitions;
    }
}
