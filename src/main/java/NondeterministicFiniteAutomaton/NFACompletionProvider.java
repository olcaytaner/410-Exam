package NondeterministicFiniteAutomaton;

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
 * Provides context-aware autocompletion for NFA definition files.
 * It suggests keywords, defined states, and alphabet symbols based on the current text and cursor position.
 * This provider is specifically tailored to the .nfa file format, suggesting "eps" for epsilon transitions.
 */
public class NFACompletionProvider {

    public static class NFAProviderRegistration {

        private static boolean registered = false;

        public static void register() {
            if (registered) return;

            AutoCompletionProviderRegistry.registerProvider(
                    Automaton.MachineType.NFA,
                    NFACompletionProvider::create
            );

            registered = true;
        }
    }

    /**
     * Creates and returns a new dynamic completion provider for NFAs.
     *
     * @return A configured CompletionProvider instance.
     */
    public static CompletionProvider create() {
        return new DynamicNFACompletionProvider();
    }

    /**
     * The core implementation of the NFA completion provider, extending DefaultCompletionProvider
     * to add dynamic, context-sensitive suggestions.
     */
    private static class DynamicNFACompletionProvider extends DefaultCompletionProvider {

        public DynamicNFACompletionProvider() {
            // Auto-activate completions without a specific trigger character.
            setAutoActivationRules(true, null);
        }

        @Override
        protected List<Completion> getCompletionsImpl(JTextComponent comp) {
            List<Completion> all = new ArrayList<>(super.getCompletionsImpl(comp));
            String text = comp.getText();
            NFAContext ctx = parseNFAContext(text);
            addDynamicCompletions(all, ctx, comp);

            // Filter the suggestions based on what the user has already typed.
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
                // Find the start of the current word/token.
                for (int i = pos - 1; i >= 0; i--) {
                    char c = text.charAt(i);
                    if (Character.isWhitespace(c) || c == '>' || c == '(' || c == ')' || c == ',') {
                        start = i + 1;
                        break;
                    }
                    if (i == 0) start = 0;
                }

                String part = text.substring(start, pos).trim();
                return part.isEmpty() ? "" : part;
            } catch (BadLocationException e) {
                return ""; // Should not happen
            }
        }

        /**
         * Adds dynamic completions based on the current context (e.g., inside a transition).
         *
         * @param completions The list to add new completions to.
         * @param ctx         The parsed context of the entire NFA file.
         * @param comp        The text component being edited.
         */
        private void addDynamicCompletions(List<Completion> completions,
                                           NFAContext ctx,
                                           JTextComponent comp) {
            String text = comp.getText();
            int caret = comp.getCaretPosition();
            String line = getCurrentLine(text, caret);
            String beforeCaret = line.substring(0, Math.min(caret - getLineStartOffset(text, caret), line.length())).toLowerCase();

            boolean afterArrow = beforeCaret.contains("->");
            boolean insideParens = beforeCaret.contains("(") && !beforeCaret.contains(")");

            // Determine suggestion mode based on context
            if (afterArrow && !insideParens) {
                // Context: After "->", but before "(". Suggest destination states.
                // Example: q0 -> |
                for (String s : ctx.states)
                    completions.add(new BasicCompletion(this, s, "Defined state"));
            } else if (insideParens) {
                // Context: Inside "(...)". Suggest alphabet symbols and "eps".
                // Example: q0 -> q1 (|
                for (String a : ctx.alphabet)
                    completions.add(new BasicCompletion(this, a, "Defined symbol"));
                // NFA-specific: Add "eps" for epsilon transitions.
                completions.add(new BasicCompletion(this, "eps", "Epsilon transition"));
            } else {
                // Default context: Suggest states (for starting a transition) and alphabet symbols.
                for (String s : ctx.states)
                    completions.add(new BasicCompletion(this, s, "Defined state"));
                for (String a : ctx.alphabet)
                    completions.add(new BasicCompletion(this, a, "Defined symbol"));
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
         * Parses the entire NFA definition text to build a context of defined states, alphabet, etc.
         *
         * @param text The full content of the editor.
         * @return An NFAContext object containing the parsed information.
         */
        private NFAContext parseNFAContext(String text) {
            NFAContext ctx = new NFAContext();
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
                } else if (lower.startsWith("start:")) {
                    section = "start";
                    ctx.startState = t.substring(6).trim();
                } else if (lower.startsWith("finals:")) {
                    section = "finals";
                    parseFinals(t.substring(7).trim(), ctx);
                } else if (lower.startsWith("transitions:")) {
                    section = "transitions";
                } else if (section != null) {
                    // Handle multi-line definitions for sections
                    switch (section) {
                        case "states":
                            parseStates(t, ctx);
                            break;
                        case "alphabet":
                            parseAlphabet(t, ctx);
                            break;
                        case "finals":
                            parseFinals(t, ctx);
                            break;
                        default:
                            break;
                    }
                }
            }
            return ctx;
        }

        private void parseStates(String s, NFAContext ctx) {
            for (String st : s.split("\\s+"))
                if (!st.isEmpty()) ctx.states.add(st);
        }

        private void parseAlphabet(String s, NFAContext ctx) {
            for (String sym : s.split("\\s+"))
                if (!sym.isEmpty()) ctx.alphabet.add(sym);
        }

        private void parseFinals(String s, NFAContext ctx) {
            for (String f : s.split("\\s+"))
                if (!f.isEmpty()) ctx.finalStates.add(f);
        }
    }

    /**
     * A simple data structure to hold the parsed context of an NFA definition.
     */
    private static class NFAContext {
        Set<String> states = new LinkedHashSet<>();
        Set<String> alphabet = new LinkedHashSet<>();
        Set<String> finalStates = new LinkedHashSet<>();
        String startState = null;
    }
}