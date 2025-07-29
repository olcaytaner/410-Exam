package NondeterministicFiniteAutomaton;

import common.Automaton;
import common.Automaton.ValidationMessage.ValidationMessageType;
import java.io.BufferedReader;
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

        List<ValidationMessage> warnings = checkSyntax(inputText);

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
     * (Not implemented yet)
     *
     * @param inputText input string to execute on the NFA
     * @return {@link ExecutionResult} (currently returns null)
     */
    @Override
    public ExecutionResult execute(String inputText) { //todo to be implemented
        return null;
    }

    /**
     * Validates the internal consistency of the NFA structure.
     *
     * @return list of {@link ValidationMessage} objects indicating errors or warnings
     */
    @Override
    public List<ValidationMessage> validate(){
        List<ValidationMessage> validationWarnings = new ArrayList<>();

        Map<String, State> states = this.states;
        State startState = this.startState;

        if (startState == null){
            validationWarnings.add(new ValidationMessage("No start state in NFA object", -1, ValidationMessageType.ERROR));
        }else{
            if (!startState.isStartState()){
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

        Set<State> finalStates = this.finalStates;
        if (finalStates == null || finalStates.isEmpty()){
            validationWarnings.add(new ValidationMessage("No final state in NFA object", -1, ValidationMessageType.ERROR));
        }else {
            for (State finalState : finalStates){
                if (!finalState.isFinalState()){
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

        Set<Symbol> alphabet = this.alphabet;
        if (alphabet == null || alphabet.isEmpty()){
            validationWarnings.add(new ValidationMessage("Alphabet is empty", -1, ValidationMessageType.ERROR));
        }else {
            for (Symbol symbol : alphabet){
                validationWarnings.addAll(symbol.validate());
            }
        }

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
                    List<ValidationMessage> transitionSymbolWarnings = t.getSymbol().validate();
                    validationWarnings.addAll(transitionSymbolWarnings);

                    if (alphabet != null && !t.getSymbol().isEpsilon() && !alphabet.contains(t.getSymbol())){
                        validationWarnings.add(new ValidationMessage("Alphabet does not contain transition symbol: " + t.getSymbol().getC(), -1, ValidationMessageType.ERROR));
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

        int startCount = 0;
        int finalCount = 0;
        for (State state : states.values()){
            if (!state.getName().matches(NFA.statePattern)){
                validationWarnings.add(new ValidationMessage("State name: " + state.getName() + " does not match valid state name", -1, ValidationMessageType.ERROR));
            }
            if (state.isStartState()){
                startCount++;
            }
            if (state.isFinalState()){
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

        for (ValidationMessage warning : validationWarnings){
            System.out.println(warning);
        }

        return validationWarnings;
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

        StringBuilder dot = new StringBuilder("digraph PDA {\n");
        dot.append("  rankdir=LR;\n");
        dot.append("  node [shape = circle];\n");

        for (State state : finalStates) {
            dot.append(String.format("  \"%s\" [shape=doublecircle];\n", state.getName()));
        }

        if (startState != null) {
            dot.append("  \"start\" [shape=diamond];\n");
            dot.append(String.format("  \"start\" -> \"%s\";\n", startState.getName()));
        }

        for (List<Transition> transitions : transitions.values()) {
            for (Transition t : transitions) {
                String symbol;
                if (t.getSymbol().isEpsilon()) {
                    symbol = "ε";
                    dot.append(String.format("  \"%s\" -> \"%s\" [label=\"%s\", style=\"dashed\"];\n",
                            t.getFrom().getName(), t.getTo().getName(), symbol));
                }else {
                    symbol = String.valueOf(t.getSymbol().getC());
                    dot.append(String.format("  \"%s\" -> \"%s\" [label=\"%s\"];\n",
                            t.getFrom().getName(), t.getTo().getName(), symbol));
                }

            }
        }

        dot.append("}");
        return dot.toString();
    }

    /**
     * Analyzes the input text line by line to detect and record syntax errors and warnings
     * during the parsing process. This includes validation of the start state,
     * final states, alphabet definition, and transitions.
     *
     * @param inputText the raw textual definition of the NFA
     * @return list of {@link ValidationMessage} objects indicating issues in the input
     */
    private List<ValidationMessage> checkSyntax(String inputText) {

        boolean startLine = false;
        boolean finalsLine = false;
        boolean alphaLine = false;
        boolean transLine = false;
        boolean nextLineTransition = false;

        Map<String, State> tempStates = new HashMap<>();
        Set<Symbol> tempAlphabet =  new HashSet<>();
        State tempStartState = null;
        Set<State> tempFinalStates =  new HashSet<>();
        Map<State, List<Transition>> tempTransitions =  new HashMap<>();


        int startLineNo = -1;
        int finalsLineNo = -1;
        int alphaLineNo = -1;

        List<ValidationMessage> warnings = new ArrayList<>();

        try {
            BufferedReader reader = new BufferedReader(new StringReader(inputText));
            String line;
            int lineNo = 0;
            while ((line = reader.readLine()) != null){
                line = line.trim();
                lineNo++;

                if (line.startsWith("#")) {
                    //comment line
                    continue;
                }

                if (line.toLowerCase().startsWith("start:")){
                    nextLineTransition = false;
                    String[] arr = line.split(" ");

                    if (!startLine){
                        startLine = true;
                        startLineNo = lineNo;
                    }else {
                        //there is already a start line
                        warnings.add(new ValidationMessage("There is already a start line", lineNo, ValidationMessageType.ERROR));
                        continue;
                    }

                    if (arr.length < 2){
                        //no starts state
                        warnings.add(new ValidationMessage("No start state", lineNo, ValidationMessageType.ERROR));
                        continue;
                    } else if (arr.length > 2) {
                        //more than 1 start state
                        warnings.add(new ValidationMessage("More than 1 start state", lineNo, ValidationMessageType.ERROR));
                        continue;
                    } else {

                        String stateName = arr[1].trim();
                        if (stateName.matches(statePattern)){ //q then number
                            //correct
                            System.out.println("Start line is correct: " + line);

                            if (tempStates.containsKey(stateName)){
                                tempStates.get(stateName).setStartState(true);
                            }else {
                                tempStates.put(stateName, new State(stateName, true, false));
                            }
                            tempStartState = tempStates.get(stateName);

                        }else {
                            //wrong state name
                            warnings.add(new ValidationMessage("Wrong start state name: " + stateName, lineNo,  ValidationMessageType.ERROR));
                            continue;
                        }

                    }

                } else
                if (line.toLowerCase().startsWith("finals:")) {
                    nextLineTransition = false;
                    String[] arr = line.split(" ");

                    if (!finalsLine){
                        finalsLine = true;
                        finalsLineNo = lineNo;
                    } else {
                        //there is already a finals line
                        warnings.add(new ValidationMessage("There is already a finals line", lineNo, ValidationMessageType.ERROR));
                        continue;
                    }

                    if (arr.length < 2){
                        //no state
                        warnings.add(new ValidationMessage("No final state", lineNo,  ValidationMessageType.ERROR));
                        continue;
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

                                if (tempStates.containsKey(arr[i])){
                                    tempStates.get(arr[i]).setFinalState(true);
                                    tempFinalStates.add(tempStates.get(arr[i]));
                                }else {
                                    State finalState = new State(arr[i], false, true);
                                    tempStates.put(arr[i], finalState);
                                    tempFinalStates.add(finalState);
                                }

                            }

                        }

                    }

                } else
                if (line.toLowerCase().startsWith("alphabet:")) {
                    nextLineTransition = false;
                    String[] arr = line.split(" ");

                    if (!alphaLine){
                        alphaLine = true;
                        alphaLineNo = lineNo;
                    }else {
                        //there is already an alphabet line
                        warnings.add(new ValidationMessage("There is already a alphabet line", lineNo,  ValidationMessageType.ERROR));
                        continue;
                    }

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
                                    if (tempAlphabet.contains(symbol)) {
                                        //duplicate letter
                                        warnings.add(new ValidationMessage("Duplicate letter: " + arr[i], lineNo,  ValidationMessageType.WARNING));
                                    }
                                    tempAlphabet.add(symbol);
                                }
                            }

                        }
                        if (isCorrect){
                            System.out.println("Alphabet syntax is correct: " + line);
                        }

                    }
                } else
                if (line.toLowerCase().startsWith("transitions:")) {
                    nextLineTransition = true;
                    if (!transLine){
                        transLine = true;
                    }else {
                        //there is already a transitions line
                        warnings.add(new ValidationMessage("There is already a \"transitions:\" line", lineNo,  ValidationMessageType.ERROR));
                        continue;
                    }

                    if (!line.trim().equals("transitions:")) {
                        warnings.add(new ValidationMessage("\"transitions:\" line should only contain \"transitions:\"", lineNo, ValidationMessageType.ERROR));
                    }

                } else
                if (line.contains("->")) {
                    if (!nextLineTransition) {
                        //no transitions: line
                        warnings.add(new ValidationMessage("No \"transitions:\" line for this transition", lineNo, ValidationMessageType.ERROR));
                    }

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
                        if (firstStateName != null && tempStates.get(firstStateName) != null) {
                            State fromState = tempStates.get(firstStateName);
                            if (tempTransitions.get(fromState) != null) {
                                for (Transition t : tempTransitions.get(fromState)) {
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

                            tempStates.putIfAbsent(firstStateName, new State(firstStateName));
                            tempStates.putIfAbsent(secondStateName, new State(secondStateName));

                            State fromState = tempStates.get(firstStateName);
                            State toState = tempStates.get(secondStateName);

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
                                if (!tempAlphabet.contains(symbolTemp) && !symbol.equals("eps")) {
                                    //alphabet does not contain transition symbol
                                    warnings.add(new ValidationMessage("Alphabet does not contain transition symbol: " + symbol,
                                            lineNo, ValidationMessageType.ERROR));
                                    continue;
                                }
                                Transition transition = new Transition(fromState,toState,symbolTemp);
                                if (!alreadyExists){
                                    if (transitionList.contains(transition)) {
                                        warnings.add(new ValidationMessage("Duplicate transition symbol: " + symbolTemp.getC(), lineNo, ValidationMessageType.ERROR));
                                    }else {
                                        //unique transition and unique symbol
                                        transitionList.add(transition);
                                    }
                                }
                            }

                            if (!transitionList.isEmpty()) {
                                if (tempTransitions.containsKey(fromState)){
                                    tempTransitions.get(fromState).addAll(transitionList);
                                }else {
                                    tempTransitions.put(fromState,transitionList);
                                }
                                System.out.println("Transition correct: " + line);
                                //correct
                            }
                        }
                    }

                }else {
                    if (!line.isEmpty()) {
                        warnings.add(new ValidationMessage("Skipping Unknown Line: " + line, lineNo, ValidationMessageType.WARNING));
                        nextLineTransition = false;
                    }
                }

            }

            for (Symbol symbol : tempAlphabet) {
                boolean found = false;
                for (List<Transition> transitions : tempTransitions.values()) {
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
                    warnings.add(new ValidationMessage("Symbol not used: " + symbol.getC(), alphaLineNo, ValidationMessageType.WARNING));
                }
            }

            if (!startLine){
                warnings.add(new ValidationMessage("There is no start line", -1, ValidationMessageType.ERROR));
            }
            if (!finalsLine){
                warnings.add(new ValidationMessage("There is no final line", -1, ValidationMessageType.ERROR));
            }
            if (!alphaLine){
                warnings.add(new ValidationMessage("There is no alphabet line", -1, ValidationMessageType.ERROR));
            }
            if (!transLine){
                warnings.add(new ValidationMessage("There is no \"transitions\" line", -1, ValidationMessageType.WARNING));
            }
            if (tempTransitions.isEmpty()){
                warnings.add(new ValidationMessage("There are no correct transitions", -1, ValidationMessageType.WARNING));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        this.states = tempStates;
        this.alphabet = tempAlphabet;
        this.startState = tempStartState;
        this.finalStates = tempFinalStates;
        this.transitions = tempTransitions;

        return warnings;
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

    public void setStartState(State startState) { //todo delete
        this.startState = startState;
    }
}
