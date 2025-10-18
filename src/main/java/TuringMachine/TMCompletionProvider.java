package TuringMachine;

import common.AutoCompletionProviderRegistry;
import common.Automaton;
import org.fife.ui.autocomplete.*;
import javax.swing.text.JTextComponent;
import javax.swing.text.BadLocationException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Provides context-aware autocompletion for Turing Machine (.tm) definition files.
 * It suggests keywords, defined states, tape alphabet symbols, and move directions
 * based on the current text and cursor position, tailored to the .tm file format.
 */
public class TMCompletionProvider {

    public static class TMProviderRegistration {
        private static boolean registered = false;

        public static void register() {
            if (registered) return;

            AutoCompletionProviderRegistry.registerProvider(
                    Automaton.MachineType.TM,
                    TMCompletionProvider::create
            );

            registered = true;
        }
    }

    /**
     * Creates and returns a new dynamic completion provider for Turing Machines.
     *
     * @return A configured CompletionProvider instance.
     */
    public static CompletionProvider create() {
        return new DynamicTMCompletionProvider();
    }

    /**
     * The core implementation of the TM completion provider, extending DefaultCompletionProvider
     * to add dynamic, context-sensitive suggestions.
     */
    private static class DynamicTMCompletionProvider extends DefaultCompletionProvider {

        public DynamicTMCompletionProvider() {
            setAutoActivationRules(true, null);
        }

        @Override
        protected List<Completion> getCompletionsImpl(JTextComponent comp) {
            List<Completion> all = new ArrayList<>(super.getCompletionsImpl(comp));
            String text = comp.getText();
            TMContext ctx = parseTMContext(text);
            addDynamicCompletions(all, ctx, comp);

            String prefix = getAlreadyEnteredText(comp);
            if (prefix == null || prefix.isEmpty()) {
                return all;
            }

            List<Completion> filtered = new ArrayList<>();
            String lowerPrefix = prefix.toLowerCase();
            for (Completion c : all) {
                String s = c.getReplacementText();
                if (s != null && s.toLowerCase().startsWith(lowerPrefix)) {
                    filtered.add(c);
                }
            }
            return filtered;
        }

        @Override
        public String getAlreadyEnteredText(JTextComponent comp) {
            try {
                int pos = comp.getCaretPosition();
                if (pos == 0) return "";

                String text = comp.getText(0, pos);
                int start = pos;
                for (int i = pos - 1; i >= 0; i--) {
                    char c = text.charAt(i);
                    if (Character.isWhitespace(c) || c == '>') {
                        start = i + 1;
                        break;
                    }
                    if (i == 0) start = 0;
                }

                return text.substring(start, pos).trim();
            } catch (BadLocationException e) {
                return "";
            }
        }

        /**
         * Adds dynamic completions based on the TM transition structure.
         *
         * @param completions The list to add new completions to.
         * @param ctx         The parsed context of the entire TM file.
         * @param comp        The text component being edited.
         */
        private void addDynamicCompletions(List<Completion> completions,
                                           TMContext ctx,
                                           JTextComponent comp) {
            String text = comp.getText();
            int caret = comp.getCaretPosition();
            String line = getCurrentLine(text, caret);
            String beforeCaret = line.substring(0, Math.min(caret - getLineStartOffset(text, caret), line.length()));

            // Only provide transition suggestions on lines that are not section headers.
            if (line.trim().matches("^(states|input_alphabet|tape_alphabet|start|accept|reject|transitions):.*")) {
                return;
            }

            boolean hasArrow = beforeCaret.contains("->");

            if (!hasArrow) {
                // Before "->": <current_state> <read_symbol>
                String[] tokens = beforeCaret.trim().split("\\s+");
                int tokenCount = (beforeCaret.trim().isEmpty()) ? 0 : tokens.length;
                if (beforeCaret.endsWith(" ") && !beforeCaret.trim().isEmpty()) tokenCount++;

                if (tokenCount <= 1) { // Typing current_state
                    for (String s : ctx.states) completions.add(new BasicCompletion(this, s, "Defined state"));
                } else if (tokenCount == 2) { // Typing read_symbol
                    for (String s : ctx.tapeAlphabet) completions.add(new BasicCompletion(this, s, "Tape symbol"));
                }
            } else {
                // After "->": <next_state> <write_symbol> <direction>
                String afterArrowPart = beforeCaret.substring(beforeCaret.indexOf("->") + 2);
                String[] tokens = afterArrowPart.trim().split("\\s+");
                int tokenCount = (afterArrowPart.trim().isEmpty()) ? 0 : tokens.length;
                if (afterArrowPart.endsWith(" ") && !afterArrowPart.trim().isEmpty()) tokenCount++;

                if (tokenCount <= 1) { // Typing next_state
                    for (String s : ctx.states) completions.add(new BasicCompletion(this, s, "Defined state"));
                } else if (tokenCount == 2) { // Typing write_symbol
                    for (String s : ctx.tapeAlphabet) completions.add(new BasicCompletion(this, s, "Tape symbol"));
                } else if (tokenCount == 3) { // Typing direction
                    completions.add(new BasicCompletion(this, "L", "Move Left"));
                    completions.add(new BasicCompletion(this, "R", "Move Right"));
                }
            }
        }

        private String getCurrentLine(String text, int caret) {
            if (text == null || caret < 0 || caret > text.length()) return "";
            int start = text.lastIndexOf('\n', caret - 1) + 1;
            int end = text.indexOf('\n', caret);
            if (end == -1) end = text.length();
            return text.substring(start, end);
        }

        private int getLineStartOffset(String text, int caret) {
            int start = text.lastIndexOf('\n', caret - 1);
            return start == -1 ? 0 : start + 1;
        }

        /**
         * Parses the entire TM definition text to build a context of defined items.
         *
         * @param text The full content of the editor.
         * @return A TMContext object containing the parsed information.
         */
        private TMContext parseTMContext(String text) {
            TMContext ctx = new TMContext();
            if (text == null || text.trim().isEmpty()) return ctx;

            String[] lines = text.split("\n");
            String section = null;

            for (String line : lines) {
                String t = line.trim();
                if (t.isEmpty() || t.startsWith("#")) continue;

                String lower = t.toLowerCase();
                if (lower.startsWith("states:")) {
                    section = "states";
                    parseStates(t.substring(7).trim(), ctx);
                } else if (lower.startsWith("input_alphabet:")) {
                    section = "input_alphabet";
                    parseInputAlphabet(t.substring(15).trim(), ctx);
                } else if (lower.startsWith("tape_alphabet:")) {
                    section = "tape_alphabet";
                    parseTapeAlphabet(t.substring(14).trim(), ctx);
                } else if (lower.startsWith("transitions:")) {
                    section = "transitions";
                } else if (section != null) {
                    switch (section) {
                        case "states": parseStates(t, ctx); break;
                        case "input_alphabet": parseInputAlphabet(t, ctx); break;
                        case "tape_alphabet": parseTapeAlphabet(t, ctx); break;
                    }
                }
            }
            return ctx;
        }

        private void parseStates(String s, TMContext ctx) {
            for (String st : s.split("\\s+")) if (!st.isEmpty()) ctx.states.add(st);
        }

        private void parseInputAlphabet(String s, TMContext ctx) {
            for (String sym : s.split("\\s+")) if (!sym.isEmpty()) ctx.inputAlphabet.add(sym);
        }

        private void parseTapeAlphabet(String s, TMContext ctx) {
            for (String sym : s.split("\\s+")) if (!sym.isEmpty()) ctx.tapeAlphabet.add(sym);
        }
    }

    /**
     * A simple data structure to hold the parsed context of a TM definition.
     */
    private static class TMContext {
        Set<String> states = new LinkedHashSet<>();
        Set<String> inputAlphabet = new LinkedHashSet<>();
        Set<String> tapeAlphabet = new LinkedHashSet<>();
    }
}
