package PushdownAutomaton;

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
 * Provides context-aware autocompletion for Push-down Automaton (PDA) definition files.
 * It suggests keywords, defined states, and alphabet symbols based on the current text and cursor position.
 * This provider is specifically tailored to the .pda file format, understanding its unique
 * five-part transition structure.
 */
public class PDACompletionProvider {

    public static class PDAProviderRegistration {
        private static boolean registered = false;

        public static void register() {
            if (registered) return;

            AutoCompletionProviderRegistry.registerProvider(
                    Automaton.MachineType.PDA,
                    PDACompletionProvider::create
            );

            registered = true;
        }
    }

    /**
     * Creates and returns a new dynamic completion provider for PDAs.
     *
     * @return A configured CompletionProvider instance.
     */
    public static CompletionProvider create() {
        return new DynamicPDACompletionProvider();
    }

    /**
     * The core implementation of the PDA completion provider, extending DefaultCompletionProvider
     * to add dynamic, context-sensitive suggestions.
     */
    private static class DynamicPDACompletionProvider extends DefaultCompletionProvider {

        public DynamicPDACompletionProvider() {
            setAutoActivationRules(true, null);
        }

        @Override
        protected List<Completion> getCompletionsImpl(JTextComponent comp) {
            List<Completion> all = new ArrayList<>(super.getCompletionsImpl(comp));
            String text = comp.getText();
            PDAContext ctx = parsePDAContext(text);
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
         * Adds dynamic completions based on the PDA transition structure.
         *
         * @param completions The list to add new completions to.
         * @param ctx         The parsed context of the entire PDA file.
         * @param comp        The text component being edited.
         */
        private void addDynamicCompletions(List<Completion> completions,
                                           PDAContext ctx,
                                           JTextComponent comp) {
            String text = comp.getText();
            int caret = comp.getCaretPosition();
            String line = getCurrentLine(text, caret);
            String beforeCaret = line.substring(0, Math.min(caret - getLineStartOffset(text, caret), line.length()));

            boolean hasArrow = beforeCaret.contains("->");

            if (!hasArrow) {
                // Handling the part before "->": <fromState> <input> <stackPop>
                String trimmedBeforeCaret = beforeCaret.trim();
                String[] tokens = trimmedBeforeCaret.isEmpty() ? new String[0] : trimmedBeforeCaret.split("\\s+");
                int tokenCount = tokens.length;

                if (beforeCaret.endsWith(" ") && !trimmedBeforeCaret.isEmpty()) {
                    tokenCount++;
                }

                switch (tokenCount) {
                    case 1: // Typing the fromState
                        for (String s : ctx.states) completions.add(new BasicCompletion(this, s, "Defined state"));
                        break;
                    case 2: // After fromState, typing the input symbol
                        for (String a : ctx.alphabet) completions.add(new BasicCompletion(this, a, "Input symbol"));
                        completions.add(new BasicCompletion(this, "eps", "Epsilon input"));
                        break;
                    case 3: // After input symbol, typing the stack pop symbol
                        for (String sa : ctx.stackAlphabet) completions.add(new BasicCompletion(this, sa, "Stack symbol"));
                        completions.add(new BasicCompletion(this, "eps", "Epsilon pop"));
                        break;
                }
            } else {
                // Handling the part after "->": <toState> <stackPush>
                String afterArrowPart = beforeCaret.substring(beforeCaret.indexOf("->") + 2).trim();
                String[] afterArrowTokens = afterArrowPart.isEmpty() ? new String[0] : afterArrowPart.split("\\s+");
                int afterArrowTokenCount = afterArrowTokens.length;

                if (beforeCaret.endsWith(" ") && !afterArrowPart.isEmpty()) {
                    afterArrowTokenCount++;
                }

                if (afterArrowTokenCount <= 1) { // After '->', typing the toState
                    for (String s : ctx.states) completions.add(new BasicCompletion(this, s, "Defined state"));
                } else { // After toState, typing the stack push string
                    for (String sa : ctx.stackAlphabet) completions.add(new BasicCompletion(this, sa, "Stack symbol"));
                    completions.add(new BasicCompletion(this, "eps", "Epsilon push"));
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
         * Parses the entire PDA definition text to build a context of defined items.
         *
         * @param text The full content of the editor.
         * @return A PDAContext object containing the parsed information.
         */
        private PDAContext parsePDAContext(String text) {
            PDAContext ctx = new PDAContext();
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
                } else if (lower.startsWith("alphabet:")) {
                    section = "alphabet";
                    parseAlphabet(t.substring(9).trim(), ctx);
                } else if (lower.startsWith("stack_alphabet:")) {
                    section = "stack_alphabet";
                    parseStackAlphabet(t.substring(15).trim(), ctx);
                } else if (lower.startsWith("start:")) {
                    section = "start";
                    ctx.startState = t.substring(6).trim();
                } else if (lower.startsWith("stack_start:")) {
                    section = "stack_start";
                    ctx.stackStartSymbol = t.substring(12).trim();
                } else if (lower.startsWith("finals:")) {
                    section = "finals";
                    parseFinals(t.substring(7).trim(), ctx);
                } else if (lower.startsWith("transitions:")) {
                    section = "transitions";
                } else if (section != null) {
                    switch (section) {
                        case "states": parseStates(t, ctx); break;
                        case "alphabet": parseAlphabet(t, ctx); break;
                        case "stack_alphabet": parseStackAlphabet(t, ctx); break;
                        case "finals": parseFinals(t, ctx); break;
                    }
                }
            }
            return ctx;
        }

        private void parseStates(String s, PDAContext ctx) {
            for (String st : s.split("\\s+")) if (!st.isEmpty()) ctx.states.add(st);
        }

        private void parseAlphabet(String s, PDAContext ctx) {
            for (String sym : s.split("\\s+")) if (!sym.isEmpty()) ctx.alphabet.add(sym);
        }

        private void parseStackAlphabet(String s, PDAContext ctx) {
            for (String sym : s.split("\\s+")) if (!sym.isEmpty()) ctx.stackAlphabet.add(sym);
        }

        private void parseFinals(String s, PDAContext ctx) {
            for (String f : s.split("\\s+")) if (!f.isEmpty()) ctx.finalStates.add(f);
        }
    }

    /**
     * A simple data structure to hold the parsed context of a PDA definition.
     */
    private static class PDAContext {
        Set<String> states = new LinkedHashSet<>();
        Set<String> alphabet = new LinkedHashSet<>();
        Set<String> stackAlphabet = new LinkedHashSet<>();
        Set<String> finalStates = new LinkedHashSet<>();
        String startState = null;
        String stackStartSymbol = null;
    }
}