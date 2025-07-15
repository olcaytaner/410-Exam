package NondeterministicFiniteAutomaton;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NFA extends AbstractAutomaton {

    public NFA(Map<String, State> states, Set<Symbol> alphabet, State startState, Set<State> finalStates){

        super(states, alphabet, startState, finalStates);

    }

    public NFA(String path){

        super(new HashMap<>(), new HashSet<>(), null, new HashSet<>());

        try {
            BufferedReader reader = new BufferedReader(new FileReader(Path.of(path).toFile()));
            String line;
            while ((line = reader.readLine()) != null){

                if (line.startsWith("Start:")){
                    String startStateName = line.substring(6).trim();
                    startState = states.getOrDefault(startStateName, new State(startStateName));
                    startState.setStartState(true);
                    states.put(startStateName, startState);


                } else if (line.startsWith("Finals:")) {
                    String[] finals = line.substring(7).trim().split(", ");

                    for (int i = 0; i < finals.length; i++) {
                        State finalState = states.getOrDefault(finals[i], new State(finals[i]));
                        finalState.setFinalState(true);
                        states.put(finals[i], finalState);
                        finalStates.add(finalState);
                    }

                } else if (line.contains("->")) {

                    String[] parts = line.split("->");
                    String fromStateName = parts[0].trim();

                    String[] restParts = parts[1].trim().split(" ", 2);
                    String toStateName = restParts[0].trim();

                    String symbols = restParts[1].trim();
                    symbols = symbols.replace("(", "").replace(")", "").trim();
                    String[] symbolsArray = symbols.split(",");

                    State fromState = states.getOrDefault(fromStateName, new State(fromStateName));
                    State toState = states.getOrDefault(toStateName, new State(toStateName));

                    states.put(fromStateName, fromState);
                    states.put(toStateName, toState);

                    for (int i = 0; i < symbolsArray.length; i++) {

                        Symbol symbol = new Symbol(symbolsArray[i].trim().charAt(0));
                        alphabet.add(symbol);
                        addTransition(fromState, toState, symbol);

                    }



                }


            }


        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void addTransition(State from, State to, Symbol symbol){

        transitions.putIfAbsent(from, new ArrayList<>());
        transitions.get(from).add(new Transition(from, to, symbol));

    }

    @Override
    public boolean accepts(String s) {
        return false;
    }


    public String toGraphViz(){
        return null;
    }

    public static List<Warning> validate(String path) {

        boolean startLine = false;
        boolean finalsLine = false;
        boolean alphaLine = false;
        boolean transLine = false;
        boolean nextLineTransition = false;
        List<Transition> transitions = new ArrayList<>();


        List<Warning> warnings = new ArrayList<>();

        try {
            BufferedReader reader = new BufferedReader(new FileReader(Path.of(path).toFile()));
            String line;
            int lineNo = 0;
            while ((line = reader.readLine()) != null){
                line = line.trim();
                lineNo++;

                if (line.toLowerCase().startsWith("start:")){
                    nextLineTransition = false;
                    String[] arr = line.split(" ");

                    if (!startLine){
                        startLine = true;
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
                        
                        String state = arr[1].trim();
                        if (state.matches("q\\d+")){ //q then number
                            //correct
                            System.out.println("Start line is correct");
                        }else {
                            //wrong state name
                            warnings.add(new Warning("Wrong start state name: " + state, Warning.ERROR, lineNo,  Warning.START_LINE));
                            continue;
                        }

                    }

                } else
                    if (line.toLowerCase().startsWith("finals:")) {
                    nextLineTransition = false;
                    String[] arr = line.split(" ");

                    if (!finalsLine){
                        finalsLine = true;
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
                            if (!arr[i].matches("q\\d+")){
                                //wrong state naming
                                isCorrect = false;
                                warnings.add(new Warning("Wrong final state name: " + arr[i], Warning.ERROR, lineNo,  Warning.FINALS_LINE));
                            }
                        }
                        if (isCorrect){
                            System.out.println("Final state is correct");
                        }

                    }
                    
                } else
                    if (line.toLowerCase().startsWith("alphabet:")) {
                    nextLineTransition = false;
                    String[] arr = line.split(" ");

                    if (!alphaLine){
                        alphaLine = true;
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
                                }
                            }

                        }
                        if (isCorrect){
                            System.out.println("Alphabet is correct");
                        }

                    }
                } else
                    if (line.toLowerCase().startsWith("transitions:")) { // q\d+ -> q\d+ \(([^()] ?)+\)
                    nextLineTransition = true;
                    if (!transLine){
                        transLine = true;
                    }else {
                        //there is already a transitions line
                        warnings.add(new Warning("There is already a \"transitions:\" line", Warning.ERROR, lineNo,  Warning.TRANS_LINE));
                        continue;
                    }

                    if (!line.trim().equals("transitions:")) {
                        warnings.add(new Warning("Something wrong with the \"transitions:\" line", Warning.WARN, lineNo, Warning.TRANS_LINE));
                    }

                } else
                    if (line.contains("->")) { // (q\d+) -> (q\d+) \(([a-zA-Z]{1}(\s[a-zA-Z]{1})*\s?)\)
                    if (!nextLineTransition) {
                        //no transitions: line
                        warnings.add(new Warning("No \"transitions:\" line for this transition", Warning.ERROR, lineNo, Warning.TRANS_LINE));
                    }

                    Pattern pattern = Pattern.compile("(?:(q\\d+)|.) ?-> ?(?:(q\\d+)|\\S*)? (?:(\\((?:[a-zA-Z]|eps)(?:\\s(?:[a-zA-Z]|eps))*\\s?\\))|.+)?");
                    Matcher matcher = pattern.matcher(line);
                    System.out.println("group count: " + matcher.groupCount());
                    String wrongPart = line;
                    String message = "";
                    State firstState = null;
                    State secondState = null;
                    if (matcher.find()) {
                        wrongPart = wrongPart.replace("->", "");
                        if (matcher.group(1) != null && matcher.group(1).matches("q\\d+")) {
                            System.out.println("first state correct");
                            System.out.println(matcher.group(1));
                            firstState = new State(matcher.group(1));
                            wrongPart = wrongPart.replace(matcher.group(1),"");
                        }else {
                            message += "First state incorrect \n";
                        }

                        if (matcher.group(2) != null && matcher.group(2).matches("q\\d+")) {
                            System.out.println("second state correct");
                            System.out.println(matcher.group(2));
                            secondState = new State(matcher.group(2));
                            wrongPart = wrongPart.replace(matcher.group(2),"");
                        }else {
                            message += "Second state incorrect \n";
                        }

                        if (matcher.group(3) != null && matcher.group(3).matches("\\((?:[a-zA-Z]|eps)(?:\\s(?:[a-zA-Z]|eps))*\\s?\\)")) {
                            System.out.println("transition letters correct");
                            System.out.println(matcher.group(3));
                            wrongPart = wrongPart.replace(matcher.group(3),"");
                        }else {
                            message += "Transition letters incorrect \n";
                        }
                        wrongPart = wrongPart.trim();

                    }else {
                        message = "Whole line is wrong ";
                    }

                    if (!message.isEmpty() && !wrongPart.isEmpty()) {
                        warnings.add(new Warning("Wrong transition syntax: " + message + " Wrong part: " + wrongPart,
                                Warning.ERROR, lineNo, Warning.TRANS_LINE));
                    }else {
                        System.out.println("Transition syntax correct");

                        boolean alreadyExists = false;

                        for (int i = 0; i < transitions.size(); i++) {
                            if (transitions.get(i).getFrom().getName().equals(firstState.getName()) && transitions.get(i).getTo().getName().equals(secondState.getName())) {
                                alreadyExists = true;
                                break;
                            }
                        }

                        if (alreadyExists) {
                            warnings.add(new Warning("There is already this transition: " + line,  Warning.WARN, lineNo, Warning.TRANS_LINE));
                            System.out.println("Transition already exists");
                        }

                        if (!alreadyExists && firstState != null && secondState != null) {
                            transitions.add(new Transition(firstState, secondState, new Symbol('x')));
                            //correct
                        }
                    }

                }else {
                    if (!line.isEmpty()) {
                        warnings.add(new Warning("Unknown Line: " + line, Warning.WARN, lineNo, Warning.OTHER));
                        nextLineTransition = false;
                    }
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
                warnings.add(new Warning("There are no correct transition", Warning.WARN, -1, Warning.TRANS_LINE));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return warnings;
    }

    @Override
    public void loadInput(String s) {

    }

    @Override
    public String toString() {
        return super.toString();
    }
}
