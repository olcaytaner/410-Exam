package TuringMachine;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.AbstractMap;


public class TMFileValidator {
    public enum Level {ERROR, WARNING}

    public static class Issue {
        public final int line;
        public final Level level;
        public final String code;
        public final String message;

        public Issue(int line, Level level, String code, String message) {
            this.line = line;
            this.level = level;
            this.code = code;
            this.message = message;
        }

        @Override
        public String toString() {
            return String.format("Line %d | %-7s | %-25s | %s",
                    line, level, code, message);
        }
    }

    private static final Pattern STATE_NAME = Pattern.compile("q\\d+|q_accept|q_reject", Pattern.CASE_INSENSITIVE);
    private static final Pattern TRANSITION_LINE = Pattern.compile("(\\S+)\\s+(\\S+)\\s*->\\s*(\\S+)\\s+(\\S+)\\s+([LR])");

    public static List<Issue> validate(String filepath) {
        List<Issue> issues = new ArrayList<>();
        Map<String, Integer> sectionLines = new HashMap<>();
        Map<String, String> sectionContent = new HashMap<>();
        List<Map.Entry<Integer, String>> transitionLines = new ArrayList<>();

        Set<String> states = new HashSet<>();
        Map<String, Integer> stateLineNumbers = new HashMap<>();
        Set<String> inputAlphabet = new HashSet<>();
        Set<String> tapeAlphabet = new HashSet<>();
        Set<String> usedStates = new HashSet<>();
        Set<String> transitionsSeen = new HashSet<>();

        String start = null, accept = null, reject = null;

        try (BufferedReader br = new BufferedReader(new FileReader(filepath))) {
            String line;
            int ln = 0;
            boolean inTransitions = false;

            while ((line = br.readLine()) != null) {
                ln++;
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#")) continue;

                String[] parts = trimmed.split(":", 2);
                if (parts.length == 2) {
                    String header = parts[0].trim().toLowerCase();
                    String content = parts[1].trim();

                    if (sectionLines.containsKey(header)) {
                        issues.add(new Issue(ln, Level.ERROR, "DUPLICATE_SECTION", "Section '" + header + ":' is repeated."));
                    }

                    sectionLines.put(header, ln);
                    sectionContent.put(header, content);

                    inTransitions = header.equals("transitions");
                    continue;
                }

                if (inTransitions) {
                    transitionLines.add(new AbstractMap.SimpleEntry<>(ln, trimmed));
                } else {
                    issues.add(new Issue(ln, Level.ERROR, "INVALID_LINE", "Unrecognized or misplaced line: " + trimmed));
                }

            }
        } catch (IOException e) {
            issues.add(new Issue(0, Level.ERROR, "IO_EXCEPTION", e.getMessage()));
            return issues;
        }

        String[] requiredSections = {
                "states", "input_alphabet", "tape_alphabet", "start", "accept", "reject", "transitions"
        };

        for (String section : requiredSections) {
            if (!sectionLines.containsKey(section)) {
                issues.add(new Issue(0, Level.ERROR, "MISSING_SECTION", "Missing section: '" + section + ":'"));
            }
        }

        if (sectionContent.containsKey("states")) {
            int line = sectionLines.get("states");
            String raw = sectionContent.get("states");
            if (raw.isEmpty()) {
                issues.add(new Issue(line, Level.ERROR, "MISSING_STATES", "No states defined after 'states:'"));
            } else {
                Set<String> seen = new HashSet<>();
                for (String s : raw.split("\\s+")) {
                    if (!STATE_NAME.matcher(s).matches()) {
                        issues.add(new Issue(line, Level.ERROR, "INVALID_STATE_NAME", "Invalid state: " + s));
                    } else {
                        if (!seen.add(s)) {
                            issues.add(new Issue(line, Level.ERROR, "DUPLICATE_STATE", "Duplicate state: " + s));
                        }
                        states.add(s);
                        stateLineNumbers.put(s, line);
                    }
                }
            }
        }

        if (sectionContent.containsKey("input_alphabet")) {
            int line = sectionLines.get("input_alphabet");
            String raw = sectionContent.get("input_alphabet");
            if (raw.isEmpty()) {
                issues.add(new Issue(line, Level.ERROR, "MISSING_INPUT_ALPHABET", "No input symbols defined."));
            } else {
                Set<String> seen = new HashSet<>();
                for (String s : raw.split("\\s+")) {
                    if (!Alphabet.isValidSymbol(s)) {
                        issues.add(new Issue(line, Level.ERROR, "INVALID_INPUT_SYMBOL", "Invalid input symbol: '" + s + "'"));
                    } else if (!seen.add(s)) {
                        issues.add(new Issue(line, Level.ERROR, "DUPLICATE_INPUT_SYMBOL", "Duplicate input symbol: " + s));
                    } else {
                        inputAlphabet.add(s);
                    }
                }
            }
        }

        if (sectionContent.containsKey("tape_alphabet")) {
            int line = sectionLines.get("tape_alphabet");
            String raw = sectionContent.get("tape_alphabet");
            if (raw.isEmpty()) {
                issues.add(new Issue(line, Level.ERROR, "MISSING_TAPE_ALPHABET", "No tape symbols defined."));
            } else {
                Set<String> seen = new HashSet<>();
                for (String s : raw.split("\\s+")) {
                    if (!Alphabet.isValidSymbol(s)) {
                        issues.add(new Issue(line, Level.ERROR, "INVALID_TAPE_SYMBOL", "Invalid tape symbol: '" + s + "'"));
                    } else if (!seen.add(s)) {
                        issues.add(new Issue(line, Level.ERROR, "DUPLICATE_TAPE_SYMBOL", "Duplicate tape symbol: " + s));
                    } else {
                        tapeAlphabet.add(s);
                    }
                }
            }
        }

        if (sectionContent.containsKey("start")) {
            start = sectionContent.get("start");
            int line = sectionLines.get("start");
            if (start.isEmpty()) {
                issues.add(new Issue(line, Level.ERROR, "MISSING_START_STATE", "Start state not specified."));
            } else if (!states.contains(start)) {
                issues.add(new Issue(line, Level.ERROR, "UNDEFINED_START", "Start state not in 'states': " + start));
            }
        }

        if (sectionContent.containsKey("accept")) {
            accept = sectionContent.get("accept");
            int line = sectionLines.get("accept");
            if (accept.isEmpty()) {
                issues.add(new Issue(line, Level.ERROR, "MISSING_ACCEPT_STATE", "Accept state not specified."));
            } else if (!states.contains(accept)) {
                issues.add(new Issue(line, Level.ERROR, "UNDEFINED_ACCEPT", "Accept state not in 'states': " + accept));
            }
        }

        if (sectionContent.containsKey("reject")) {
            reject = sectionContent.get("reject");
            int line = sectionLines.get("reject");
            if (reject.isEmpty()) {
                issues.add(new Issue(line, Level.ERROR, "MISSING_REJECT_STATE", "Reject state not specified."));
            } else if (!states.contains(reject)) {
                issues.add(new Issue(line, Level.ERROR, "UNDEFINED_REJECT", "Reject state not in 'states': " + reject));
            }
        }

        for (Map.Entry<Integer, String> entry : transitionLines) {
            int lineNumber = entry.getKey();
            String line = entry.getValue();

            if (!line.contains("->")) {
                issues.add(new Issue(lineNumber, Level.ERROR, "MISSING_ARROW", "Expected '->' in transition: '" + line + "'"));
                continue;
            }

            Matcher m = TRANSITION_LINE.matcher(line);

            if (!m.matches()) {
                if (line.matches(".*\\s+->\\s+.*\\s+[A-Za-z]$")) {
                    String[] parts = line.split("\\s+");
                    String last = parts[parts.length - 1];
                    if (!last.equals("L") && !last.equals("R")) {
                        issues.add(new Issue(lineNumber, Level.ERROR, "INVALID_DIRECTION", "Only directions L and R are allowed"));
                        continue;
                    }
                }

                issues.add(new Issue(lineNumber, Level.ERROR, "INVALID_TRANSITION_FORMAT",
                        "Invalid format. Must be: state symbol -> state symbol direction"));
                continue;
            }
            String from = m.group(1), read = m.group(2);
            String to = m.group(3), write = m.group(4), dir = m.group(5);
            String key = from + "," + read;

            if (!Alphabet.isValidSymbol(read))
                issues.add(new Issue(lineNumber, Level.ERROR, "INVALID_READ_SYMBOL", "Symbol '" + read + "' is not a valid alphabet symbol"));
            if (!Alphabet.isValidSymbol(write))
                issues.add(new Issue(lineNumber, Level.ERROR, "INVALID_WRITE_SYMBOL", "Symbol '" + write + "' is not a valid alphabet symbol"));

            if (!states.contains(from))
                issues.add(new Issue(lineNumber, Level.ERROR, "UNDEFINED_STATE", "From state '" + from + "' not in states"));
            if (!states.contains(to))
                issues.add(new Issue(lineNumber, Level.ERROR, "UNDEFINED_STATE", "To state '" + to + "' not in states"));
            if (!tapeAlphabet.contains(read))
                issues.add(new Issue(lineNumber, Level.ERROR, "TAPE_SYMBOL_NOT_DEFINED", "Read symbol '" + read + "' not in tape_alphabet"));
            if (!tapeAlphabet.contains(write))
                issues.add(new Issue(lineNumber, Level.ERROR, "TAPE_SYMBOL_NOT_DEFINED", "Write symbol '" + write + "' not in tape_alphabet"));
            if (!inputAlphabet.contains(read) && !read.equals("_"))
                issues.add(new Issue(lineNumber, Level.WARNING, "UNDECLARED_INPUT_SYMBOL", "Read symbol '" + read + "' not in input_alphabet"));

            if (!transitionsSeen.add(key)) {
                issues.add(new Issue(lineNumber, Level.WARNING, "DUPLICATE_TRANSITION", "Duplicate transition for: (" + key + ")"));
            }

            usedStates.add(from);
            usedStates.add(to);
        }

        for (String s : states) {
            if (!usedStates.contains(s) && !s.equals(start) && !s.equals(accept) && !s.equals(reject)) {
                int line = stateLineNumbers.getOrDefault(s, 0);
                issues.add(new Issue(line, Level.WARNING, "UNUSED_STATE", "State '" + s + "' is defined but never used"));
            }
        }

        return issues;
    }

    public static void main(String[] args) {
        String file = "src/test/java/TuringMachine/tm_sample.txt";
        List<Issue> issues = validate(file);
        issues.forEach(System.out::println);
    }
}
