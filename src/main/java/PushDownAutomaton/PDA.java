package PushDownAutomaton;

import common.Automaton;
import common.Automaton.ValidationMessage;
import common.Automaton.ValidationMessage.ValidationMessageType;
import common.InputNormalizer;
import common.State;
import common.Symbol;

import java.util.*;

/**
 * Represents a Push-down Automaton (PDA).
 * Parsing ve DOT üretimi common yapıyla uyumlu; execution BFS + visited ile optimize edilmiştir.
 */
public class PDA extends Automaton {

    private Set<State> states;
    private Set<Symbol> inputAlphabet;
    private Set<Symbol> stackAlphabet;
    private State startState;
    private Set<State> finalStates;
    private Symbol stackStartSymbol;
    private Map<State, List<PDATransition>> transitionMap;

    public PDA() {
        super(MachineType.PDA);
    }

    @Override
    public ParseResult parse(String inputText) {
        if (inputText == null) {
            throw new NullPointerException("Input text cannot be null");
        }
        this.states = new HashSet<>();
        this.inputAlphabet = new HashSet<>();
        this.stackAlphabet = new HashSet<>();
        this.startState = null;
        this.finalStates = new HashSet<>();
        this.stackStartSymbol = null;
        this.transitionMap = new HashMap<>();
        Map<String, State> stateMap = new HashMap<>();

        InputNormalizer.NormalizedInput normalized =
                InputNormalizer.normalize(inputText, MachineType.PDA);

        List<ValidationMessage> messages = new ArrayList<>(normalized.getMessages());
        Map<String, List<String>> sections = normalized.getSections();
        Map<String, Integer> sectionLineNumbers = normalized.getSectionLineNumbers();

        if (normalized.hasErrors()
                || !InputNormalizer.validateRequiredKeywords(sections, MachineType.PDA, messages)) {
            return new ParseResult(false, messages, null);
        }

        processStates(sections.get("states"), sectionLineNumbers.get("states"), this.states, stateMap, messages);
        processAlphabet(sections.get("alphabet"), sectionLineNumbers.getOrDefault("alphabet", 0), this.inputAlphabet, messages);
        processAlphabet(sections.get("stack_alphabet"), sectionLineNumbers.getOrDefault("stack_alphabet", 0), this.stackAlphabet, messages);
        this.startState = processStartState(sections.get("start"), sectionLineNumbers.get("start"), stateMap, messages);
        this.stackStartSymbol = processStackStartSymbol(sections.get("stack_start"), sectionLineNumbers.get("stack_start"), this.stackAlphabet, messages);
        this.finalStates.addAll(processFinalStates(sections.get("finals"), sectionLineNumbers.get("finals"), stateMap, messages));
        this.transitionMap.putAll(processTransitions(sections.get("transitions"), sectionLineNumbers.get("transitions"), stateMap, this.inputAlphabet, this.stackAlphabet, messages));

        boolean isSuccess = messages.stream().noneMatch(m -> m.getType() == ValidationMessageType.ERROR);
        if (!isSuccess) {
            return new ParseResult(false, messages, this);
        }

        checkForUnreachableStates(this.states, this.startState, this.transitionMap, messages);
        checkForDeadEndStates(this.states, this.finalStates, this.transitionMap, messages);
        return new ParseResult(true, messages, this);
    }

    /**
     * Non-deterministic PDA execution with BFS over configurations (state, input position, stackString).
     * Stack top is kept at stackString.charAt(0) (leftmost).
     * Accept by final state (consistent with "finals" requirement in input spec).
     */
    @Override
    public ExecutionResult execute(String inputText) {
        List<ValidationMessage> logs = new ArrayList<>();

        if (this.startState == null) {
            logs.add(new ValidationMessage("Automaton not parsed.", 0, ValidationMessageType.ERROR));
            return new ExecutionResult(false, logs, "Automaton not parsed.");
        }

        final String input = (inputText == null) ? "" : inputText;
        final int n = input.length();

        // initial stack: start symbol if not epsilon
        final String initStack =
                (this.stackStartSymbol != null && !this.stackStartSymbol.isEpsilon())
                        ? Character.toString(this.stackStartSymbol.getValue())
                        : "";

        Deque<Conf> queue = new ArrayDeque<>();
        Set<Conf> visited = new HashSet<>();
        Map<Conf, Step> parent = new HashMap<>();

        Conf start = new Conf(this.startState, 0, initStack);
        queue.add(start);
        visited.add(start);

        int expansions = 0;
        final int MAX_EXPANSIONS = 1_000_000; // safety cap

        while (!queue.isEmpty()) {
            Conf cur = queue.poll();

            // Accept when input fully consumed & in a final state
            if (cur.pos == n && this.finalStates.contains(cur.state)) {
                String trace = reconstructTrace(parent, cur);
                logs.add(new ValidationMessage(
                        "Accepted at state '" + cur.state.getName() + "' with stack='" + cur.stack + "'.",
                        0, ValidationMessageType.INFO));
                return new ExecutionResult(true, logs, trace);
            }

            // Expand transitions from current state
            List<PDATransition> outgoing = this.transitionMap.getOrDefault(cur.state, Collections.emptyList());
            for (PDATransition t : outgoing) {
                // Stack pop condition
                boolean popEps = t.getStackPop().isEpsilon();
                char popCh = t.getStackPop().getValue();
                if (!popEps) {
                    if (cur.stack.isEmpty() || cur.stack.charAt(0) != popCh) continue;
                }

                // Input consume condition
                boolean inEps = t.getInputSymbol().isEpsilon();
                if (!inEps) {
                    if (cur.pos >= n || input.charAt(cur.pos) != t.getInputSymbol().getValue()) continue;
                }

                // Apply transition
                String newStack = cur.stack;
                if (!popEps) {
                    newStack = newStack.substring(1); // pop from left (top)
                }

                String push = t.getStackPush();
                if (!"eps".equals(push)) {
                    // push sequence to left (top at index 0)
                    newStack = push + newStack;
                }

                int newPos = inEps ? cur.pos : (cur.pos + 1);
                Conf nxt = new Conf(t.getToState(), newPos, newStack);

                if (visited.add(nxt)) {
                    parent.put(nxt, new Step(cur, t));
                    queue.add(nxt);
                }
            }

            if (++expansions > MAX_EXPANSIONS) {
                logs.add(new ValidationMessage(
                        "Search aborted after exploring " + expansions + " configurations (safety cap).",
                        0, ValidationMessageType.WARNING));
                break;
            }
        }

        // Not accepted: produce a best-effort trace from the farthest progressed configuration
        Conf farthest = parent.keySet().stream()
                .max(Comparator.comparingInt(c -> c.pos))
                .orElse(null);

        String trace = (farthest != null) ? reconstructTrace(parent, farthest) : "No steps taken.";
        logs.add(new ValidationMessage("No accepting configuration found.",
                0, ValidationMessageType.INFO));
        return new ExecutionResult(false, logs, trace);
    }

    /* ----------------- Helpers for BFS trace & conf identity ----------------- */

    private static final class Conf {
        final State state;
        final int pos;        // input index
        final String stack;   // top-of-stack = stack.charAt(0)

        Conf(State state, int pos, String stack) {
            this.state = state;
            this.pos = pos;
            this.stack = (stack == null) ? "" : stack;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Conf)) return false;
            Conf c = (Conf) o;
            return pos == c.pos
                    && Objects.equals(state.getName(), c.state.getName())
                    && Objects.equals(stack, c.stack);
        }

        @Override
        public int hashCode() {
            return Objects.hash(state.getName(), pos, stack);
        }
    }

    private static final class Step {
        final Conf prev;
        final PDATransition tr;

        Step(Conf prev, PDATransition tr) {
            this.prev = prev;
            this.tr = tr;
        }
    }

    private static String symToStr(Symbol s) {
        return (s == null || s.isEpsilon()) ? "eps" : Character.toString(s.getValue());
    }

    private String reconstructTrace(Map<Conf, Step> parent, Conf end) {
        List<String> lines = new ArrayList<>();
        Conf cur = end;

        while (parent.containsKey(cur)) {
            Step st = parent.get(cur);
            PDATransition t = st.tr;

            String in = symToStr(t.getInputSymbol());
            String pop = symToStr(t.getStackPop());
            String push = t.getStackPush();
            String from = st.prev.state.getName();
            String to = t.getToState().getName();

            lines.add(String.format("%s -- (%s, %s/%s) --> %s",
                    from, in, pop, push, to));

            cur = st.prev;
        }
        Collections.reverse(lines);
        return String.join(" | ", lines);
    }

    /* ----------------- DOT ve parse yardımcıları (senin kodunla aynı mantık) ----------------- */

    @Override
    public String toDotCode(String inputText) {
        if (this.states == null || this.states.isEmpty()) {
            return "digraph PDA {\n\tlabel=\"Automaton not parsed or is empty\";\n}";
        }
        StringBuilder dot = new StringBuilder("digraph PDA {\n");
        dot.append("  rankdir=LR;\n");
        dot.append("  node [shape = circle];\n");

        for (State state : finalStates) {
            dot.append(String.format("  \"%s\" [shape=doublecircle];\n", state.getName()));
        }

        if (startState != null) {
            dot.append("  \"\" [shape=point];\n");
            dot.append(String.format("  \"\" -> \"%s\";\n", startState.getName()));
        }

        Map<String, List<PDATransition>> groupedTransitions = new HashMap<>();
        for (List<PDATransition> transitions : transitionMap.values()) {
            for (PDATransition t : transitions) {
                String key = t.getFromState().getName() + " -> " + t.getToState().getName();
                groupedTransitions.computeIfAbsent(key, k -> new ArrayList<>()).add(t);
            }
        }

        String[] selfLoopPorts = {"n", "w", "s", "e", "nw", "ne", "sw", "se"};
        for (Map.Entry<String, List<PDATransition>> entry : groupedTransitions.entrySet()) {
            List<PDATransition> group = entry.getValue();
            State fromState = group.get(0).getFromState();
            State toState = group.get(0).getToState();
            boolean isSelfLoop = fromState.equals(toState);

            for (int i = 0; i < group.size(); i++) {
                PDATransition t = group.get(i);
                String input = t.getInputSymbol().isEpsilon() ? "eps" : t.getInputSymbol().toString();
                String pop = t.getStackPop().isEpsilon() ? "eps" : t.getStackPop().toString();
                String label = String.format("%s, %s / %s", input, pop, t.getStackPush());

                if (isSelfLoop) {
                    String port1 = selfLoopPorts[i * 2 % selfLoopPorts.length];
                    String port2 = selfLoopPorts[(i * 2 + 1) % selfLoopPorts.length];
                    dot.append(String.format("  \"%s\":%s -> \"%s\":%s [label=\"%s\"];\n",
                            fromState.getName(), port1, toState.getName(), port2, label));
                } else {
                    dot.append(String.format("  \"%s\" -> \"%s\" [label=\"%s\"];\n",
                            fromState.getName(), toState.getName(), label));
                }
            }
        }
        dot.append("}");
        return dot.toString();
    }

    @Override
    public List<ValidationMessage> validate() { return new ArrayList<>(); }

    private void processStates(List<String> stateLines, int lineNum, Set<State> states,
                               Map<String, State> stateMap, List<ValidationMessage> messages) {
        if (stateLines == null || stateLines.isEmpty()) {
            messages.add(new ValidationMessage("The 'states:' block cannot be empty.", lineNum, ValidationMessageType.ERROR));
            return;
        }
        for (String name : stateLines.get(0).split("\\s+")) {
            State newState = new State(name);
            states.add(newState);
            stateMap.put(name, newState);
        }
    }

    private void processAlphabet(List<String> lines, int lineNum, Set<Symbol> alphabet,
                                 List<ValidationMessage> messages) {
        if (lines == null || lines.isEmpty()) {
            messages.add(new ValidationMessage("Alphabet definition cannot be empty.", lineNum, ValidationMessageType.ERROR));
            return;
        }
        for (String s : lines.get(0).split("\\s+")) {
            if (s.length() != 1) {
                messages.add(new ValidationMessage("Alphabet symbol '" + s + "' must be a single character.", lineNum, ValidationMessageType.ERROR));
                continue;
            }
            alphabet.add(new Symbol(s.charAt(0)));
        }
    }

    private State processStartState(List<String> lines, int lineNum,
                                    Map<String, State> stateMap, List<ValidationMessage> messages) {
        if (lines == null || lines.isEmpty()) {
            messages.add(new ValidationMessage("Start state definition is missing.", lineNum, ValidationMessageType.ERROR));
            return null;
        }
        String startStateName = lines.get(0).trim();
        State startState = stateMap.get(startStateName);
        if (startState == null) {
            messages.add(new ValidationMessage("Start state '" + startStateName + "' is not defined in 'states'.", lineNum, ValidationMessageType.ERROR));
            return null;
        }
        startState.setStart(true);
        return startState;
    }

    private Symbol processStackStartSymbol(List<String> lines, int lineNum,
                                           Set<Symbol> stackAlphabet, List<ValidationMessage> messages) {
        if (lines == null || lines.isEmpty()) {
            messages.add(new ValidationMessage("Stack start symbol definition is missing.", lineNum, ValidationMessageType.ERROR));
            return null;
        }
        String stackStartStr = lines.get(0).trim();
        if (stackStartStr.length() != 1) {
            messages.add(new ValidationMessage("Stack start symbol must be a single character.", lineNum, ValidationMessageType.ERROR));
            return null;
        }
        Symbol stackStartSymbol = new Symbol(stackStartStr.charAt(0));
        if (!stackAlphabet.contains(stackStartSymbol)) {
            messages.add(new ValidationMessage("Stack start symbol '" + stackStartStr + "' is not defined in 'stack_alphabet'.", lineNum, ValidationMessageType.ERROR));
        }
        return stackStartSymbol;
    }

    private Set<State> processFinalStates(List<String> lines, int lineNum,
                                          Map<String, State> stateMap, List<ValidationMessage> messages) {
        Set<State> finalStates = new HashSet<>();
        if (lines == null || lines.isEmpty()) {
            messages.add(new ValidationMessage("Final states definition is missing.", lineNum, ValidationMessageType.ERROR));
            return finalStates;
        }
        String[] finalStateNames = lines.get(0).split("\\s+");
        for (String name : finalStateNames) {
            State finalState = stateMap.get(name);
            if (finalState == null) {
                messages.add(new ValidationMessage("Final state '" + name + "' is not defined in 'states'.", lineNum, ValidationMessageType.ERROR));
                continue;
            }
            finalState.setAccept(true);
            finalStates.add(finalState);
        }
        return finalStates;
    }

    private Map<State, List<PDATransition>> processTransitions(List<String> lines, int startLine,
                                                               Map<String, State> stateMap,
                                                               Set<Symbol> inputAlphabet, Set<Symbol> stackAlphabet,
                                                               List<ValidationMessage> messages) {
        Map<State, List<PDATransition>> transitionMap = new HashMap<>();
        if (lines == null) return transitionMap;

        for (int i = 0; i < lines.size(); i++) {
            int currentLine = startLine + i + 1;
            String line = lines.get(i);

            String[] parts = line.split("->");
            if (parts.length != 2) {
                messages.add(new ValidationMessage("Invalid transition format. Rule must contain '->' separator.", currentLine, ValidationMessageType.ERROR));
                continue;
            }

            String[] left = parts[0].trim().split("\\s+");
            String[] right = parts[1].trim().split("\\s+");

            if (left.length != 3) {
                messages.add(new ValidationMessage("Left side of transition must have 3 components: state, input, stack_pop.", currentLine, ValidationMessageType.ERROR));
                continue;
            }
            if (right.length != 2) {
                messages.add(new ValidationMessage("Right side of transition must have 2 components: state, stack_push.", currentLine, ValidationMessageType.ERROR));
                continue;
            }

            State fromState = validateState(left[0], stateMap, currentLine, messages);
            Symbol input = validateInputSymbol(left[1], inputAlphabet, currentLine, messages);
            Symbol pop = validateStackSymbol(left[2], stackAlphabet, currentLine, messages);
            State toState = validateState(right[0], stateMap, currentLine, messages);
            String push = right[1];

            if (!validatePushString(push, stackAlphabet, currentLine, messages)) {
                continue;
            }
            if (fromState == null || input == null || pop == null || toState == null) {
                continue;
            }

            PDATransition transition = new PDATransition(fromState, input, pop, toState, push);
            transitionMap.computeIfAbsent(fromState, k -> new ArrayList<>()).add(transition);
        }
        return transitionMap;
    }

    private State validateState(String name, Map<String, State> stateMap, int line, List<ValidationMessage> messages) {
        State state = stateMap.get(name);
        if (state == null) {
            messages.add(new ValidationMessage("State '" + name + "' is not defined in 'states'.", line, ValidationMessageType.ERROR));
        }
        return state;
    }

    private Symbol validateInputSymbol(String s, Set<Symbol> alphabet, int line, List<ValidationMessage> messages) {
        if (s.equals("eps")) return new Symbol('_');
        if (s.length() != 1) {
            messages.add(new ValidationMessage("Input symbol '" + s + "' must be a single character or 'eps'.", line, ValidationMessageType.ERROR));
            return null;
        }
        Symbol symbol = new Symbol(s.charAt(0));
        if (!alphabet.contains(symbol)) {
            messages.add(new ValidationMessage("Input symbol '" + s + "' is not defined in 'alphabet'.", line, ValidationMessageType.ERROR));
            return null;
        }
        return symbol;
    }

    private Symbol validateStackSymbol(String s, Set<Symbol> alphabet, int line, List<ValidationMessage> messages) {
        if (s.equals("eps")) return new Symbol('_');
        if (s.length() != 1) {
            messages.add(new ValidationMessage("Stack symbol '" + s + "' must be a single character or 'eps'.", line, ValidationMessageType.ERROR));
            return null;
        }
        Symbol symbol = new Symbol(s.charAt(0));
        if (!alphabet.contains(symbol)) {
            messages.add(new ValidationMessage("Stack symbol '" + s + "' is not defined in 'stack_alphabet'.", line, ValidationMessageType.ERROR));
            return null;
        }
        return symbol;
    }

    private boolean validatePushString(String pushString, Set<Symbol> alphabet, int line, List<ValidationMessage> messages) {
        if (!"eps".equals(pushString)) {
            for (char c : pushString.toCharArray()) {
                if (!alphabet.contains(new Symbol(c))) {
                    messages.add(new ValidationMessage(
                            "Stack symbol '" + c + "' (in push string '" + pushString + "') is not defined in 'stack_alphabet'.",
                            line, ValidationMessageType.ERROR));
                    return false;
                }
            }
        }
        return true;
    }

    private void checkForUnreachableStates(Set<State> allStates, State startState,
                                           Map<State, List<PDATransition>> transitions,
                                           List<ValidationMessage> messages) {
        if (startState == null || allStates.isEmpty()) return;

        Set<State> reachableStates = new HashSet<>();
        Queue<State> queue = new LinkedList<>();
        reachableStates.add(startState);
        queue.add(startState);

        while (!queue.isEmpty()) {
            State currentState = queue.poll();
            if (transitions.containsKey(currentState)) {
                for (PDATransition t : transitions.get(currentState)) {
                    if (t.getToState() != null && reachableStates.add(t.getToState())) {
                        queue.add(t.getToState());
                    }
                }
            }
        }

        for (State state : allStates) {
            if (!reachableStates.contains(state)) {
                messages.add(new ValidationMessage("State '" + state.getName() + "' is unreachable from the start state.", 0, ValidationMessageType.WARNING));
            }
        }
    }

    private void checkForDeadEndStates(Set<State> allStates, Set<State> finalStates,
                                       Map<State, List<PDATransition>> transitions,
                                       List<ValidationMessage> messages) {
        for (State state : allStates) {
            if (!finalStates.contains(state) && transitions.getOrDefault(state, Collections.emptyList()).isEmpty()) {
                messages.add(new ValidationMessage("State '" + state.getName() + "' is a non-final state with no outgoing transitions (dead-end state).", 0, ValidationMessageType.WARNING));
            }
        }
    }
}
