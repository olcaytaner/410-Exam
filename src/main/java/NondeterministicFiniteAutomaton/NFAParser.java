package NondeterministicFiniteAutomaton;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NFAParser {

    protected static final String transitionPattern = "(?:(q\\d+)|.) ?-> ?(?:(q\\d+)|\\S*)? (?:(\\((?:[a-zA-Z]|eps)(?:\\s(?:[a-zA-Z]|eps))*\\s?\\))|.+)?";
    protected static final String transitionSymbolPattern = "\\((?:[a-zA-Z]|eps)(?:\\s(?:[a-zA-Z]|eps))*\\s?\\)";
    protected static final String statePattern = "q\\d+";

    private final String filePath;

    private Map<String, State> states;
    private Set<Symbol> alphabet;
    private State startState;
    private Set<State> finalStates;
    private Map<State, List<Transition>> transitions;

    public NFAParser(String filePath) {
        this.filePath = filePath;
        states = new HashMap<>();
        alphabet = new HashSet<>();
        startState = null;
        finalStates = new HashSet<>();
        transitions = new HashMap<>();
    }

    public ParseResult parseNFA() {

        List<Warning> warnings = checkSyntax(filePath);

        int errorCount = 0;
        int warnCount = 0;

        for (Warning warning : warnings) {
            if (warning.getLevel() == Warning.ERROR){
                errorCount++;
            } else if (warning.getLevel() == Warning.WARN) {
                warnCount++;
            }
        }

        NFA nfa;

        if (errorCount > 0) {
            nfa = null;
            System.out.println("NFA parsing unsuccessful " + errorCount + " error(s) and " + warnCount + " warning(s).");
        }else {
            nfa = new NFA(this.states, this.alphabet, this.startState, this.finalStates, this.transitions);
            System.out.println("Successfully parsed NFA with " + errorCount + " error(s) and " +  warnCount + " warning(s)");
        }

        return new ParseResult(nfa, warnings);
    }

    private List<Warning> checkSyntax(String path) {

        boolean startLine = false;
        boolean finalsLine = false;
        boolean alphaLine = false;
        boolean transLine = false;
        boolean nextLineTransition = false;

        int startLineNo = -1;
        int finalsLineNo = -1;
        int alphaLineNo = -1;

        List<Warning> warnings = new ArrayList<>();

        try {
            BufferedReader reader = new BufferedReader(new FileReader(Paths.get(path).toFile()));
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
                        warnings.add(new Warning("There is already a start line", Warning.ERROR, lineNo, Warning.START_LINE));
                        continue;
                    }

                    if (arr.length < 2){
                        //no starts state
                        warnings.add(new Warning("No start state", Warning.ERROR, lineNo, Warning.START_LINE));
                        continue;
                    } else if (arr.length > 2) {
                        //more than 1 start state
                        warnings.add(new Warning("More than 1 start state", Warning.ERROR, lineNo, Warning.START_LINE));
                        continue;
                    } else {

                        String stateName = arr[1].trim();
                        if (stateName.matches(statePattern)){ //q then number
                            //correct
                            System.out.println("Start line is correct: " + line);

                            if (this.states.containsKey(stateName)){
                                this.states.get(stateName).setStartState(true);
                            }else {
                                this.states.put(stateName, new State(stateName, true, false));
                            }
                            this.startState = states.get(stateName);

                        }else {
                            //wrong state name
                            warnings.add(new Warning("Wrong start state name: " + stateName, Warning.ERROR, lineNo,  Warning.START_LINE));
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
                        warnings.add(new Warning("There is already a finals line", Warning.ERROR, lineNo, Warning.FINALS_LINE));
                        continue;
                    }

                    if (arr.length < 2){
                        //no state
                        warnings.add(new Warning("No final state", Warning.ERROR, lineNo,  Warning.FINALS_LINE));
                        continue;
                    }else {
                        boolean isCorrect = true;
                        for (int i = 1; i < arr.length; i++) {
                            if (!arr[i].matches(statePattern)){
                                //wrong state naming
                                isCorrect = false;
                                warnings.add(new Warning("Wrong final state name: " + arr[i], Warning.ERROR, lineNo,  Warning.FINALS_LINE));
                            }
                        }
                        if (isCorrect){
                            System.out.println("Final state is correct: " + line);
                            for (int i = 1; i < arr.length; i++) {

                                if (this.states.containsKey(arr[i])){
                                    this.states.get(arr[i]).setFinalState(true);
                                    this.finalStates.add(this.states.get(arr[i]));
                                }else {
                                    State finalState = new State(arr[i], false, true);
                                    this.states.put(arr[i], finalState);
                                    this.finalStates.add(finalState);
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
                        warnings.add(new Warning("There is already a alphabet line", Warning.ERROR, lineNo,  Warning.ALPHABET_LINE));
                        continue;
                    }

                    if (arr.length < 2){
                        //no letter
                        warnings.add(new Warning("No alphabet letter", Warning.ERROR, lineNo,   Warning.ALPHABET_LINE));
                    }else {
                        boolean isCorrect = true;
                        for (int i = 1; i < arr.length; i++) {
                            if (arr[i].equals("eps")){
                                //wrong letter naming
                                isCorrect = false;
                                warnings.add(new Warning("Alphabet letter cannot be eps", Warning.ERROR, lineNo, Warning.ALPHABET_LINE));
                            }

                            if (arr[i].length() != 1){
                                //not a character
                                isCorrect = false;
                                warnings.add(new Warning("Alphabet is not a character: " + arr[i], Warning.ERROR, lineNo,  Warning.ALPHABET_LINE));
                            }else {
                                if (!Character.isLetter(arr[i].charAt(0))) {
                                    //char is not a letter
                                    isCorrect = false;
                                    warnings.add(new Warning("Alphabet is not a letter: " + arr[i], Warning.ERROR, lineNo,  Warning.ALPHABET_LINE));
                                }else {
                                    //correct letter
                                    Symbol symbol = new Symbol(arr[i].charAt(0));
                                    if (this.alphabet.contains(symbol)) {
                                        //duplicate letter
                                        warnings.add(new Warning("Duplicate letter: " + arr[i], Warning.WARN, lineNo,  Warning.ALPHABET_LINE));
                                    }
                                    this.alphabet.add(symbol);
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
                        warnings.add(new Warning("There is already a \"transitions:\" line", Warning.ERROR, lineNo,  Warning.TRANS_LINE));
                        continue;
                    }

                    if (!line.trim().equals("transitions:")) {
                        warnings.add(new Warning("\"transitions:\" line should only contain \"transitions:\"", Warning.ERROR, lineNo, Warning.TRANS_LINE));
                    }

                } else
                if (line.contains("->")) {
                    if (!nextLineTransition) {
                        //no transitions: line
                        warnings.add(new Warning("No \"transitions:\" line for this transition", Warning.ERROR, lineNo, Warning.TRANS_LINE));
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
                        warnings.add(new Warning("Wrong transition syntax: " + message + " Wrong part: " + wrongPart,
                                Warning.ERROR, lineNo, Warning.TRANS_LINE));
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
                            warnings.add(new Warning("There is already this transition: " + firstStateName + " -> " + secondStateName,  Warning.ERROR, lineNo, Warning.TRANS_LINE));
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
                                    warnings.add(new Warning("Alphabet does not contain transition symbol: " + symbol, Warning.ERROR, lineNo, Warning.TRANS_LINE));
                                    continue;
                                }
                                Transition transition = new Transition(fromState,toState,symbolTemp);
                                if (!alreadyExists){
                                    if (transitionList.contains(transition)) {
                                        warnings.add(new Warning("Duplicate transition symbol: " + symbolTemp.getC(), Warning.ERROR, lineNo, Warning.TRANS_LINE));
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

                }else {
                    if (!line.isEmpty()) {
                        warnings.add(new Warning("Skipping Unknown Line: " + line, Warning.WARN, lineNo, Warning.OTHER));
                        nextLineTransition = false;
                    }
                }

            }

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
                    warnings.add(new Warning("Symbol not used: " + symbol.getC(), Warning.WARN, alphaLineNo, Warning.ALPHABET_LINE));
                }
            }

            if (!startLine){
                warnings.add(new Warning("There is no start line", Warning.ERROR, -1, Warning.START_LINE));
            }
            if (!finalsLine){
                warnings.add(new Warning("There is no final line", Warning.ERROR, -1, Warning.FINALS_LINE));
            }
            if (!alphaLine){
                warnings.add(new Warning("There is no alphabet line", Warning.ERROR, -1, Warning.ALPHABET_LINE));
            }
            if (!transLine){
                warnings.add(new Warning("There is no \"transitions\" line", Warning.WARN, -1, Warning.TRANS_LINE));
            }
            if (transitions.isEmpty()){
                warnings.add(new Warning("There are no correct transitions", Warning.WARN, -1, Warning.TRANS_LINE));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return warnings;
    }

    public Map<String, State> getStates() {
        return states;
    }
}
