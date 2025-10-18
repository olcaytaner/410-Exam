package DeterministicFiniteAutomaton;

import common.AutoCompletionProviderRegistry;
import common.Automaton;
import org.fife.ui.autocomplete.*;
import javax.swing.text.JTextComponent;
import javax.swing.text.BadLocationException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class DFACompletionProvider {

    public static class DFAProviderRegistration {

        private static boolean registered = false;

        public static void register() {
            if (registered) return;

            AutoCompletionProviderRegistry.registerProvider(
                    Automaton.MachineType.DFA,
                    DFACompletionProvider::create
            );

            registered = true;
        }
    }

    public static CompletionProvider create() {
        return new DynamicDFACompletionProvider();
    }

    private static class DynamicDFACompletionProvider extends DefaultCompletionProvider {

        public DynamicDFACompletionProvider() {
            setAutoActivationRules(true, null);
        }

        @Override
        protected List<Completion> getCompletionsImpl(JTextComponent comp) {
            List<Completion> all = new ArrayList<>(super.getCompletionsImpl(comp));
            String text = comp.getText();
            DFAContext ctx = parseDFAContext(text);
            addDynamicCompletions(all, ctx, comp);

            String prefix = getAlreadyEnteredText(comp);
            if (prefix == null || prefix.isEmpty()) return all;

            List<Completion> filtered = new ArrayList<>();
            String lowerPrefix = prefix.toLowerCase();
            for (Completion c : all) {
                String s = c.getReplacementText();
                if (s != null && s.toLowerCase().startsWith(lowerPrefix)) filtered.add(c);
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
                    if (Character.isWhitespace(c) || c == '>' || c == '(' || c == ')' || c == ',') {
                        start = i + 1;
                        break;
                    }
                    if (i == 0) start = 0;
                }

                String part = text.substring(start, pos).trim();
                return part.isEmpty() ? "" : part;
            } catch (BadLocationException e) {
                return "";
            }
        }

        private void addDynamicCompletions(List<Completion> completions,
                                           DFAContext ctx,
                                           JTextComponent comp) {
            String text = comp.getText();
            int caret = comp.getCaretPosition();
            String line = getCurrentLine(text, caret);
            String beforeCaret = line.substring(0, Math.min(caret - getLineStartOffset(text, caret), line.length())).toLowerCase();

            boolean afterArrow = beforeCaret.contains("->");
            boolean insideParens = beforeCaret.contains("(") && !beforeCaret.contains(")");

            // Determine suggestion mode
            if (afterArrow && !insideParens) {
                // After "->", but before "(" → suggest states only
                for (String s : ctx.states)
                    completions.add(new BasicCompletion(this, s, "Defined state"));
            } else if (insideParens) {
                // Inside "( ... )" → suggest alphabet symbols
                for (String a : ctx.alphabet)
                    completions.add(new BasicCompletion(this, a, "Defined symbol"));
            } else {
                // Default: suggest both
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

        private DFAContext parseDFAContext(String text) {
            DFAContext ctx = new DFAContext();
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

        private void parseStates(String s, DFAContext ctx) {
            for (String st : s.split("\\s+"))
                if (!st.isEmpty()) ctx.states.add(st);
        }

        private void parseAlphabet(String s, DFAContext ctx) {
            for (String sym : s.split("\\s+"))
                if (!sym.isEmpty()) ctx.alphabet.add(sym);
        }

        private void parseFinals(String s, DFAContext ctx) {
            for (String f : s.split("\\s+"))
                if (!f.isEmpty()) ctx.finalStates.add(f);
        }
    }

    private static class DFAContext {
        Set<String> states = new LinkedHashSet<>();
        Set<String> alphabet = new LinkedHashSet<>();
        Set<String> finalStates = new LinkedHashSet<>();
        String startState = null;
    }
}