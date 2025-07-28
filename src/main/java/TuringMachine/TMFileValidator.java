package TuringMachine;

import common.Automaton.ValidationMessage;
import common.Automaton.ValidationMessage.ValidationMessageType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TMFileValidator {

    private static final Pattern STATE_NAME = Pattern.compile("q\\d+|q_accept|q_reject", Pattern.CASE_INSENSITIVE);
    private static final Pattern TRANSITION_LINE = Pattern.compile("(\\S+)\\s+(\\S+)\\s*->\\s*(\\S+)\\s+(\\S+)\\s+([LR])");

    public static List<ValidationMessage> validate(String filepath) throws IOException {
        String content = new String(Files.readAllBytes(Paths.get(filepath)));
        return validateFromString(content);
    }

    public static List<ValidationMessage> validateFromString(String content) {
        ValidationContext context = new ValidationContext();
        parseContent(content, context);

        validateSections(context);
        validateStates(context);
        validateAlphabets(context);
        validateSpecialStates(context);
        validateTransitions(context);
        checkForUnusedStates(context);

        return context.validationMessages;
    }

    private static void parseContent(String content, ValidationContext context) {
        String[] lines = content.split("\\R");
        boolean inTransitions = false;

        for (int i = 0; i < lines.length; i++) {
            int ln = i + 1;
            String line = lines[i];
            String trimmed = line.trim();

            if (trimmed.isEmpty() || trimmed.startsWith("#")) continue;

            String[] parts = trimmed.split(":", 2);
            if (parts.length == 2) {
                String header = parts[0].trim().toLowerCase();
                String sectionContent = parts[1].trim();

                if (context.sectionLines.containsKey(header)) {
                    context.addMessage(ln, ValidationMessageType.ERROR, "DUPLICATE_SECTION", "Section '" + header + ":' is repeated.");
                }

                context.sectionLines.put(header, ln);
                context.sectionContent.put(header, sectionContent);

                inTransitions = header.equals("transitions");
                continue;
            }

            if (inTransitions) {
                context.transitionLines.add(new AbstractMap.SimpleEntry<>(ln, trimmed));
            } else {
                context.addMessage(ln, ValidationMessageType.ERROR, "INVALID_LINE", "Unrecognized or misplaced line: " + trimmed);
            }
        }
    }

    private static void validateSections(ValidationContext context) {
        String[] requiredSections = {"states", "input_alphabet", "tape_alphabet", "start", "accept", "reject", "transitions"};
        for (String section : requiredSections) {
            if (!context.sectionLines.containsKey(section)) {
                context.addMessage(0, ValidationMessageType.ERROR, "MISSING_SECTION", "Missing section: '" + section + ":'");
            }
        }
    }

    private static void validateStates(ValidationContext context) {
        if (!context.sectionContent.containsKey("states")) return;

        int line = context.sectionLines.get("states");
        String raw = context.sectionContent.get("states");

        if (raw.isEmpty()) {
            context.addMessage(line, ValidationMessageType.ERROR, "MISSING_STATES", "No states defined after 'states:'");
        } else {
            Set<String> seen = new HashSet<>();
            for (String s : raw.split("\\s+")) {
                if (!STATE_NAME.matcher(s).matches()) {
                    context.addMessage(line, ValidationMessageType.ERROR, "INVALID_STATE_NAME", "Invalid state: " + s);
                } else {
                    if (!seen.add(s)) {
                        context.addMessage(line, ValidationMessageType.ERROR, "DUPLICATE_STATE", "Duplicate state: " + s);
                    }
                    context.states.add(s);
                    context.stateLineNumbers.put(s, line);
                }
            }
        }
    }

    private static void validateAlphabets(ValidationContext context) {
        if (context.sectionContent.containsKey("input_alphabet")) {
            int line = context.sectionLines.get("input_alphabet");
            String raw = context.sectionContent.get("input_alphabet");
            if (raw.isEmpty()) {
                context.addMessage(line, ValidationMessageType.ERROR, "MISSING_INPUT_ALPHABET", "No input symbols defined.");
            } else {
                Set<String> seen = new HashSet<>();
                for (String s : raw.split("\\s+")) {
                    if (!Alphabet.isValidSymbol(s)) {
                        context.addMessage(line, ValidationMessageType.ERROR, "INVALID_INPUT_SYMBOL", "Invalid input symbol: '" + s + "'");
                    } else if (!seen.add(s)) {
                        context.addMessage(line, ValidationMessageType.ERROR, "DUPLICATE_INPUT_SYMBOL", "Duplicate input symbol: " + s);
                    } else {
                        context.inputAlphabet.add(s);
                    }
                }
            }
        }

        if (context.sectionContent.containsKey("tape_alphabet")) {
            int line = context.sectionLines.get("tape_alphabet");
            String raw = context.sectionContent.get("tape_alphabet");
            if (raw.isEmpty()) {
                context.addMessage(line, ValidationMessageType.ERROR, "MISSING_TAPE_ALPHABET", "No tape symbols defined.");
            } else {
                Set<String> seen = new HashSet<>();
                for (String s : raw.split("\\s+")) {
                    if (!Alphabet.isValidSymbol(s)) {
                        context.addMessage(line, ValidationMessageType.ERROR, "INVALID_TAPE_SYMBOL", "Invalid tape symbol: '" + s + "'");
                    } else if (!seen.add(s)) {
                        context.addMessage(line, ValidationMessageType.ERROR, "DUPLICATE_TAPE_SYMBOL", "Duplicate tape symbol: " + s);
                    }
                    else {
                        context.tapeAlphabet.add(s);
                    }
                }
            }
        }
    }

    private static void validateSpecialStates(ValidationContext context) {
        context.start = validateSpecialState(context, "start", "MISSING_START_STATE", "UNDEFINED_START");
        context.accept = validateSpecialState(context, "accept", "MISSING_ACCEPT_STATE", "UNDEFINED_ACCEPT");
        context.reject = validateSpecialState(context, "reject", "MISSING_REJECT_STATE", "UNDEFINED_REJECT");
    }

    private static String validateSpecialState(ValidationContext context, String stateType, String missingCode, String undefinedCode) {
        if (!context.sectionContent.containsKey(stateType)) return null;

        String state = context.sectionContent.get(stateType);
        int line = context.sectionLines.get(stateType);

        if (state.isEmpty()) {
            context.addMessage(line, ValidationMessageType.ERROR, missingCode, stateType.substring(0, 1).toUpperCase() + stateType.substring(1) + " state not specified.");
        } else if (!context.states.contains(state)) {
            context.addMessage(line, ValidationMessageType.ERROR, undefinedCode, stateType.substring(0, 1).toUpperCase() + stateType.substring(1) + " state not in 'states': " + state);
        }
        return state;
    }

    private static void validateTransitions(ValidationContext context) {
        for (Map.Entry<Integer, String> entry : context.transitionLines) {
            int lineNumber = entry.getKey();
            String line = entry.getValue();

            if (!line.contains("->")) {
                context.addMessage(lineNumber, ValidationMessageType.ERROR, "MISSING_ARROW", "Expected '->' in transition: '" + line + "'");
                continue;
            }

            Matcher m = TRANSITION_LINE.matcher(line);

            if (!m.matches()) {
                if (line.matches(".*\\s+->\\s+.*\\s+[A-Za-z]$")) {
                    String[] parts = line.split("\\s+");
                    String last = parts[parts.length - 1];
                    if (!last.equals("L") && !last.equals("R")) {
                        context.addMessage(lineNumber, ValidationMessageType.ERROR, "INVALID_DIRECTION", "Only directions L and R are allowed");
                    }
                } else {
                    context.addMessage(lineNumber, ValidationMessageType.ERROR, "INVALID_TRANSITION_FORMAT", "Invalid format. Must be: state symbol -> state symbol direction");
                }
                continue;
            }

            String from = m.group(1), read = m.group(2);
            String to = m.group(3), write = m.group(4);
            String key = from + "," + read;

            if (!Alphabet.isValidSymbol(read))
                context.addMessage(lineNumber, ValidationMessageType.ERROR, "INVALID_READ_SYMBOL", "Symbol '" + read + "' is not a valid alphabet symbol");
            if (!Alphabet.isValidSymbol(write))
                context.addMessage(lineNumber, ValidationMessageType.ERROR, "INVALID_WRITE_SYMBOL", "Symbol '" + write + "' is not a valid alphabet symbol");

            if (!context.states.contains(from))
                context.addMessage(lineNumber, ValidationMessageType.ERROR, "UNDEFINED_STATE", "From state '" + from + "' not in states");
            if (!context.states.contains(to))
                context.addMessage(lineNumber, ValidationMessageType.ERROR, "UNDEFINED_STATE", "To state '" + to + "' not in states");
            if (!context.tapeAlphabet.contains(read))
                context.addMessage(lineNumber, ValidationMessageType.ERROR, "TAPE_SYMBOL_NOT_DEFINED", "Read symbol '" + read + "' not in tape_alphabet");
            if (!context.tapeAlphabet.contains(write))
                context.addMessage(lineNumber, ValidationMessageType.ERROR, "TAPE_SYMBOL_NOT_DEFINED", "Write symbol '" + write + "' not in tape_alphabet");
            if (!context.inputAlphabet.contains(read) && !read.equals("_"))
                context.addMessage(lineNumber, ValidationMessageType.WARNING, "UNDECLARED_INPUT_SYMBOL", "Read symbol '" + read + "' not in input_alphabet");

            if (!context.transitionsSeen.add(key)) {
                context.addMessage(lineNumber, ValidationMessageType.WARNING, "DUPLICATE_TRANSITION", "Duplicate transition for: (" + key + ")");
            }

            context.usedStates.add(from);
            context.usedStates.add(to);
        }
    }

    private static void checkForUnusedStates(ValidationContext context) {
        for (String s : context.states) {
            if (!context.usedStates.contains(s) && !s.equals(context.start) && !s.equals(context.accept) && !s.equals(context.reject)) {
                int line = context.stateLineNumbers.getOrDefault(s, 0);
                context.addMessage(line, ValidationMessageType.WARNING, "UNUSED_STATE", "State '" + s + "' is defined but never used");
            }
        }
    }

    private static class ValidationContext {
        final List<ValidationMessage> validationMessages = new ArrayList<>();
        final Map<String, Integer> sectionLines = new HashMap<>();
        final Map<String, String> sectionContent = new HashMap<>();
        final List<Map.Entry<Integer, String>> transitionLines = new ArrayList<>();
        final Set<String> states = new HashSet<>();
        final Map<String, Integer> stateLineNumbers = new HashMap<>();
        final Set<String> inputAlphabet = new HashSet<>();
        final Set<String> tapeAlphabet = new HashSet<>();
        final Set<String> usedStates = new HashSet<>();
        final Set<String> transitionsSeen = new HashSet<>();
        String start, accept, reject;

        void addMessage(int line, ValidationMessageType level, String code, String message) {
            validationMessages.add(new ValidationMessage(code + ": " + message, line, level));
        }
    }

    public static void main(String[] args) {
        String file = "src/test/java/TuringMachine/tm_sample.txt";
        try {
            List<ValidationMessage> issues = validate(file);
            issues.forEach(System.out::println);
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
    }
}
