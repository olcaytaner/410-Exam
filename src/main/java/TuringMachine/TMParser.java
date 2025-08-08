package TuringMachine;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses a Turing Machine definition from a string.
 */
public class TMParser {

    private static final Pattern TRANSITION_LINE = Pattern.compile("(\\S+)\\s+(\\S+)\\s*->\\s*(\\S+)\\s+(\\S+)\\s+([LR])");

    /**
     * Parses a Turing Machine definition from a string.
     * @param content The string content to parse.
     * @return A new TM object.
     */
    public static TM parse(String content) {
        ParseContext context = new ParseContext();
        parseContent(content, context);

        createStates(context);
        createAlphabets(context);
        createTransitions(context);

        return new TM(
                context.states,
                context.inputAlphabet,
                context.tapeAlphabet,
                context.transitionFunction,
                context.startState,
                context.acceptState,
                context.rejectState
        );
    }

    private static void parseContent(String content, ParseContext context) {
        String[] lines = content.split("\\R");
        boolean inTransitions = false;

        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("#")) continue;

            String[] parts = trimmed.split(":", 2);
            if (parts.length == 2) {
                String header = parts[0].trim().toLowerCase();
                String sectionContent = parts[1].trim();
                context.sectionContent.put(header, sectionContent);
                inTransitions = header.equals("transitions");
            } else if (inTransitions) {
                context.transitionLines.add(trimmed);
            }
        }
    }

    private static void createStates(ParseContext context) {
        String statesRaw = context.sectionContent.get("states");
        for (String s : statesRaw.split("\\s+")) {
            State newState = new State(s, s.equalsIgnoreCase("q_accept"), s.equalsIgnoreCase("q_reject"));
            context.stateMap.put(s, newState);
            context.states.add(newState);
        }

        String startStateName = context.sectionContent.get("start");
        String acceptStateName = context.sectionContent.get("accept");
        String rejectStateName = context.sectionContent.get("reject");

        context.startState = context.stateMap.get(startStateName);
        context.acceptState = context.stateMap.get(acceptStateName);
        context.rejectState = context.stateMap.get(rejectStateName);
    }

    private static void createAlphabets(ParseContext context) {
        String inputAlphabetRaw = context.sectionContent.get("input_alphabet");
        for (String s : inputAlphabetRaw.split("\\s+")) {
            context.inputAlphabet.addSymbol(s.charAt(0));
        }

        String tapeAlphabetRaw = context.sectionContent.get("tape_alphabet");
        for (String s : tapeAlphabetRaw.split("\\s+")) {
            context.tapeAlphabet.addSymbol(s.charAt(0));
        }
    }

    private static void createTransitions(ParseContext context) {
        for (String line : context.transitionLines) {
            Matcher m = TRANSITION_LINE.matcher(line);
            if (m.matches()) {
                String fromStateName = m.group(1);
                char symbolToRead = m.group(2).charAt(0);
                String toStateName = m.group(3);
                char symbolToWrite = m.group(4).charAt(0);
                Direction direction = m.group(5).equalsIgnoreCase("R") ? Direction.RIGHT : Direction.LEFT;

                State fromState = context.stateMap.get(fromStateName);
                State toState = context.stateMap.get(toStateName);

                ConfigurationKey key = new ConfigurationKey(fromState, symbolToRead);
                Transition transition = new Transition(toState, symbolToWrite, direction);

                context.transitionFunction.put(key, transition);
            }
        }
    }

    private static class ParseContext {
        final Map<String, String> sectionContent = new HashMap<>();
        final List<String> transitionLines = new ArrayList<>();
        final Map<String, State> stateMap = new HashMap<>();
        final Set<State> states = new HashSet<>();
        final Alphabet inputAlphabet = new Alphabet();
        final Alphabet tapeAlphabet = new Alphabet();
        final Map<ConfigurationKey, Transition> transitionFunction = new HashMap<>();
        State startState, acceptState, rejectState;
    }
}