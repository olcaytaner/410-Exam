package ContextFreeGrammar;

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
 * Provides context-aware autocompletion for Context-Free Grammar (.cfg) definition files.
 * It suggests keywords, defined variables (non-terminals), and terminals based on the
 * current text and cursor position, tailored to the .cfg file format.
 */
public class CFGCompletionProvider {

    public static class CFGProviderRegistration {
        private static boolean registered = false;

        public static void register() {
            if (registered) return;

            AutoCompletionProviderRegistry.registerProvider(
                    Automaton.MachineType.CFG,
                    CFGCompletionProvider::create
            );

            registered = true;
        }
    }

    /**
     * Creates and returns a new dynamic completion provider for CFGs.
     *
     * @return A configured CompletionProvider instance.
     */
    public static CompletionProvider create() {
        return new DynamicCFGCompletionProvider();
    }

    /**
     * The core implementation of the CFG completion provider, extending DefaultCompletionProvider
     * to add dynamic, context-sensitive suggestions.
     */
    private static class DynamicCFGCompletionProvider extends DefaultCompletionProvider {

        public DynamicCFGCompletionProvider() {
            setAutoActivationRules(true, null);
        }

        @Override
        protected List<Completion> getCompletionsImpl(JTextComponent comp) {
            List<Completion> all = new ArrayList<>(super.getCompletionsImpl(comp));
            String text = comp.getText();
            CFGContext ctx = parseCFGContext(text);
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
                    // Delimiters are whitespace, '>', and '|'
                    if (Character.isWhitespace(c) || c == '>' || c == '|') {
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
         * Adds dynamic completions based on the CFG structure.
         *
         * @param completions The list to add new completions to.
         * @param ctx         The parsed context of the entire CFG file.
         * @param comp        The text component being edited.
         */
        private void addDynamicCompletions(List<Completion> completions,
                                           CFGContext ctx,
                                           JTextComponent comp) {
            String text = comp.getText();
            int caret = comp.getCaretPosition();
            String line = getCurrentLine(text, caret);
            String lowerLine = line.trim().toLowerCase();

            if (lowerLine.contains("->")) {
                // Context: Right-hand side of a production rule.
                // Suggest variables, terminals, epsilon, and alternative pipe.
                for (String v : ctx.variables) completions.add(new BasicCompletion(this, v, "Variable"));
                for (String t : ctx.terminals) completions.add(new BasicCompletion(this, t, "Terminal"));
                completions.add(new BasicCompletion(this, "_", "Epsilon (empty string)"));
                completions.add(new BasicCompletion(this, "|", "Alternative production"));
            } else if (lowerLine.startsWith("start =")) {
                // Context: Specifying the start variable.
                // Suggest from the list of defined variables.
                for (String v : ctx.variables) completions.add(new BasicCompletion(this, v, "Variable"));
            } else if (!lowerLine.startsWith("variables =") && !lowerLine.startsWith("terminals =")) {
                // Context: Not a keyword line and not a production RHS.
                // This is likely the start of a new production rule (LHS).
                // Suggest all defined variables.
                for (String v : ctx.variables) completions.add(new BasicCompletion(this, v, "Variable"));
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
         * Parses the entire CFG definition text to build a context of defined items.
         *
         * @param text The full content of the editor.
         * @return A CFGContext object containing the parsed information.
         */
        private CFGContext parseCFGContext(String text) {
            CFGContext ctx = new CFGContext();
            if (text == null || text.trim().isEmpty()) return ctx;

            String[] lines = text.split("\n");

            for (String line : lines) {
                String t = line.trim();
                if (t.isEmpty() || t.startsWith("#")) continue;

                if (t.toLowerCase().startsWith("variables =")) {
                    parseSymbols(t.substring(t.indexOf('=') + 1).trim(), ctx.variables);
                } else if (t.toLowerCase().startsWith("terminals =")) {
                    parseSymbols(t.substring(t.indexOf('=') + 1).trim(), ctx.terminals);
                } else if (t.toLowerCase().startsWith("start =")) {
                    ctx.startVariable = t.substring(t.indexOf('=') + 1).trim();
                }
            }
            return ctx;
        }

        private void parseSymbols(String s, Set<String> symbolSet) {
            for (String sym : s.split("\\s+")) {
                if (!sym.isEmpty()) {
                    symbolSet.add(sym);
                }
            }
        }
    }

    /**
     * A simple data structure to hold the parsed context of a CFG definition.
     */
    private static class CFGContext {
        Set<String> variables = new LinkedHashSet<>();
        Set<String> terminals = new LinkedHashSet<>();
        String startVariable = null;
    }
}