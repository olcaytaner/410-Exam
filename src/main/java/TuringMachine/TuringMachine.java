package TuringMachine;

import java.io.*;
import java.util.*;

public class TuringMachine {
    private Set<State> states;
    private Alphabet inputAlphabet;
    private Alphabet tapeAlphabet;
    private Map<ConfigurationKey, Transition> transitionFunction;
    private State startState;
    private State acceptState;
    private State rejectState;
    private State currentState;
    private Tape tape;

    public TuringMachine() {
        states = new HashSet<>();
        inputAlphabet = new Alphabet();
        tapeAlphabet = new Alphabet();
        transitionFunction = new HashMap<>();
        tape = new Tape();
    }


    public void loadFromFile(String filepath) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filepath))) {
            String line;
            boolean readingTransitions = false;
            String acceptName = "", rejectName = "", startName = "";

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                if (line.equalsIgnoreCase("transitions:")) {
                    readingTransitions = true;
                    continue;
                }

                if (!readingTransitions) {
                    if (line.startsWith("states:")) {
                        String[] parts = line.substring(7).split(",");
                        for (String s : parts) getOrCreateState(s.trim());
                    } else if (line.startsWith("input_alphabet:")) {
                        for (String s : line.substring(15).split(",")) inputAlphabet.addSymbol(s.trim().charAt(0));
                    } else if (line.startsWith("tape_alphabet:")) {
                        for (String s : line.substring(14).split(",")) tapeAlphabet.addSymbol(s.trim().charAt(0));
                    } else if (line.startsWith("start:")) {
                        startName = line.substring(6).trim();
                    } else if (line.startsWith("accept:")) {
                        acceptName = line.substring(7).trim();
                    } else if (line.startsWith("reject:")) {
                        rejectName = line.substring(7).trim();
                    }
                } else {
                    String[] parts = line.split("->");
                    if (parts.length != 2) continue;
                    String[] lhs = parts[0].trim().split(",");
                    String[] rhs = parts[1].trim().split(",");

                    if (lhs.length != 2 || rhs.length != 3) continue;

                    String from = lhs[0].trim();
                    char read = lhs[1].trim().charAt(0);
                    String to = rhs[0].trim();
                    char write = rhs[1].trim().charAt(0);

                    Direction dir;
                    switch (rhs[2].trim()) {
                        case "L":
                            dir = Direction.LEFT;
                            break;
                        case "R":
                            dir = Direction.RIGHT;
                            break;
                        default:
                            throw new IllegalArgumentException("Invalid direction: " + rhs[2]);
                    }

                    addTransition(from, read, to, write, dir);
                }
            }
            final String finalAcceptName = acceptName;
            final String finalRejectName = rejectName;

            states.removeIf(s -> s.getName().equals(finalAcceptName));
            states.removeIf(s -> s.getName().equals(finalRejectName));


            // Set special states
            startState = getOrCreateState(startName);
            acceptState = new State(acceptName, true, false);
            rejectState = new State(rejectName, false, true);


            states.add(acceptState);
            states.add(rejectState);

            reset();
        } catch (IOException e) {
            System.err.println("Failed to load file: " + e.getMessage());
        }
    }

    public boolean simulate(String input) {
        reset();
        tape.initialize(input);
        currentState = startState;

        while (!currentState.isAccept() && !currentState.isReject()) {
            step();
        }
        return currentState.isAccept();
    }

    public void step() {
        char currentSymbol = tape.read();
        Transition transition = transitionFunction.get(new ConfigurationKey(currentState, currentSymbol));


        if (transition == null) {
            currentState = rejectState;
            return;
        }

        tape.write(transition.writeSymbol);
        tape.move(transition.moveDirection);
        currentState = transition.nextState;
    }

    public void reset() {
        tape.clear();
        currentState = startState;
    }

    public void printState() {
        tape.printTape();
    }

    public void addTransition(String from, char read, String to, char write, Direction dir) {
        State fromState = getOrCreateState(from);
        State toState = getOrCreateState(to);
        transitionFunction.put(new ConfigurationKey(fromState, read), new Transition(toState, write, dir));
    }

    private State getOrCreateState(String name) {
        for (State s : states) {
            if (s.getName().equals(name)) return s;
        }
        State newState = new State(name, false, false);
        states.add(newState);
        return newState;
    }

    public Set<State> getStates() {
        return states;
    }

    public Alphabet getInputAlphabet() {
        return inputAlphabet;
    }

    public State getStartState() {
        return startState;
    }
}
