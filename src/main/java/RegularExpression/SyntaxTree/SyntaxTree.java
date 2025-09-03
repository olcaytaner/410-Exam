package RegularExpression.SyntaxTree;

import common.Automaton;
import common.InputNormalizer;

import java.util.*;

import static RegularExpression.SyntaxTree.RegexOperator.*;

public class SyntaxTree extends Automaton {
    private final Map<Character, Integer> precedence;
    
    {
        precedence = new HashMap<>();
        precedence.put(STAR, 3);
        precedence.put(CONCAT, 2);
        precedence.put(OR, 1);
    }

    public char[] alphabet;
    public SyntaxTreeNode root;

    public SyntaxTree() {
        super(MachineType.REGEX);
    }

    public SyntaxTree(String regex, char[] alphabet) {
        super(MachineType.REGEX);
        this.alphabet = alphabet;
        String sanitizedReg = sanitize(regex);
        //System.out.println("Sanitized: " + sanitizedReg);
        String postfix = shunting_yard(sanitizedReg);
        //System.out.println("Postfix: " + postfix);
        compile(postfix);
    }

    // check for malformations
    private String sanitize(String regex) {
        // TODO check for other malformations like ** or * at the start
        regex = regex.replaceAll("\\s+", ""); // delete whitespace from input
        StringBuilder sanitized = new StringBuilder();
        int parenthesisCount = 0;
        for (char c : regex.toCharArray()) {
            if ((c == '(' || alphabetHas(c)) && sanitized.length() > 0) {
                char prev = sanitized.charAt(sanitized.length() - 1);
                if (prev == ')' || alphabetHas(prev) || prev == STAR)
                    sanitized.append(CONCAT);
            }
            sanitized.append(c);

            if (c == '(') parenthesisCount++;
            else if (c == ')') parenthesisCount--;

            if (parenthesisCount < 0)
                throw new IllegalArgumentException("Unbalanced parenthesis");

            if (!alphabetHas(c) && !precedence.containsKey(c) && c != '(' && c != ')')
                throw new IllegalArgumentException("Invalid character in regex: " + c);

        }
        if (parenthesisCount != 0)
            throw new IllegalArgumentException("Unbalanced parenthesis");
        return new String(sanitized);
    }

    // shunting yard algorithm to convert to postfix
    private String shunting_yard(String regex) {
        StringBuilder postfix = new StringBuilder();
        Deque<Character> stk = new ArrayDeque<>();
        for (char c : regex.toCharArray()) {
            if (alphabetHas(c)) {
                postfix.append(c);
                continue;
            }
            if (c == '(') {
                stk.push(c);
                continue;
            }
            if (c == ')') {
                while (stk.peek() != '(')
                    postfix.append(stk.pop());
                stk.pop();
                continue;
            }
            while (!stk.isEmpty() && stk.peek() != '(' && precedence.get(stk.peek()) >= precedence.get(c))
                postfix.append(stk.pop());
            stk.push(c);
        }
        while (!stk.isEmpty())
            postfix.append(stk.pop());
        return new String(postfix);
    }

    @Override
    public ParseResult parse(String inputText) {
        if (inputText == null) {
            throw new NullPointerException("Input text cannot be null");
        }

        InputNormalizer.NormalizedInput normalizedInput = InputNormalizer.normalize(inputText, MachineType.REGEX);
        List<ValidationMessage> messages = new ArrayList<>(normalizedInput.getMessages());
        Map<String, List<String>> sections = normalizedInput.getSections();
        Map<String, Integer> sectionLineNumbers = normalizedInput.getSectionLineNumbers();

        if (normalizedInput.hasErrors()) {
            return new ParseResult(false, messages, null);
        }

        if (!InputNormalizer.validateRequiredKeywords(sections, MachineType.REGEX, messages)) {
            return new ParseResult(false, messages, null);
        }

        // Parse alphabet
        List<String> alphabetTokens = sections.get("alphabet");
        int alphabetLine = sectionLineNumbers.getOrDefault("alphabet", 0);
        Set<Character> alphabetSet = new LinkedHashSet<>();
        if (alphabetTokens == null || alphabetTokens.isEmpty()) {
            messages.add(new ValidationMessage("The 'alphabet' line cannot be empty.", alphabetLine, ValidationMessage.ValidationMessageType.ERROR));
        } else {
            for (String token : alphabetTokens) {
                if (token == null) continue;
                String t = token.trim();
                if (t.length() != 1) {
                    messages.add(new ValidationMessage("Invalid alphabet symbol: '" + t + "' (must be a single character)", alphabetLine, ValidationMessage.ValidationMessageType.ERROR));
                    continue;
                }
                char ch = t.charAt(0);
                if (!alphabetSet.add(ch)) {
                    messages.add(new ValidationMessage("Duplicate alphabet symbol: '" + ch + "'", alphabetLine, ValidationMessage.ValidationMessageType.WARNING));
                }
            }
        }


        boolean hasErrors = messages.stream().anyMatch(m -> m.getType() == ValidationMessage.ValidationMessageType.ERROR);
        if (hasErrors) {
            return new ParseResult(false, messages, null);
        }

        // Build alphabet array
        char[] parsedAlphabet = new char[alphabetSet.size()];
        int idx = 0;
        for (char c : alphabetSet) parsedAlphabet[idx++] = c;

        // parse regex and compile syntax tree on THIS instance
        String regex = sections.get("regex").get(0);
        int regexLine = sectionLineNumbers.getOrDefault("regex", 0);
        try {
            // mutate this instance
            this.alphabet = parsedAlphabet;
            String sanitized = sanitize(regex);
            String postfix = shunting_yard(sanitized);
            compile(postfix);
        } catch (IllegalArgumentException e) {
            messages.add(new ValidationMessage(e.getMessage(), regexLine, ValidationMessage.ValidationMessageType.ERROR));
        } catch (Exception e) {
            messages.add(new ValidationMessage("Failed to compile regex: " + e.getMessage(), regexLine, ValidationMessage.ValidationMessageType.ERROR));
        }

        boolean isSuccess = messages.stream().noneMatch(m -> m.getType() == ValidationMessage.ValidationMessageType.ERROR);

        if (isSuccess) {
            messages.addAll(validate());
            isSuccess = messages.stream().noneMatch(m -> m.getType() == ValidationMessage.ValidationMessageType.ERROR);
        }

        return new ParseResult(isSuccess, messages, isSuccess ? this : null);
    }

    @Override
    public ExecutionResult execute(String inputText) {
        List<ValidationMessage> runtimeMessages = new ArrayList<>();
        StringBuilder trace = new StringBuilder();

        if (root == null || alphabet == null) {
            runtimeMessages.add(new ValidationMessage("Regex is not parsed/compiled.", -1, ValidationMessage.ValidationMessageType.ERROR));
            return new ExecutionResult(false, runtimeMessages, trace.toString());
        }

        // check if input chars belong to the alphabet
        for (int i = 0; i < inputText.length(); i++) {
            char c = inputText.charAt(i);
            if (!alphabetHas(c)) {
                runtimeMessages.add(new ValidationMessage("Symbol not in alphabet: " + c, -1, ValidationMessage.ValidationMessageType.ERROR));
                return new ExecutionResult(false, runtimeMessages, trace.toString());
            }
        }

        Set<Integer> ends = root.match(inputText, 0);
        boolean accepted = ends.contains(inputText.length());
        trace.append("Ends: ").append(ends).append("\n");
        trace.append(accepted ? "ACCEPT" : "REJECT");

        return new ExecutionResult(accepted, runtimeMessages, trace.toString());
    }

    @Override
    public List<ValidationMessage> validate() {
        List<ValidationMessage> validationWarnings = new ArrayList<>();

        if (alphabet == null || alphabet.length == 0) {
            validationWarnings.add(new ValidationMessage("Alphabet is empty", -1, ValidationMessage.ValidationMessageType.ERROR));
        }

        if (root == null) {
            validationWarnings.add(new ValidationMessage("Regex syntax tree is not built", -1, ValidationMessage.ValidationMessageType.ERROR));
        }

        return validationWarnings;
    }

    @Override
    public String toDotCode(String inputText) {
        return "";
    }

    // build AST
    private void compile(String postfix) {
        Deque<SyntaxTreeNode> stk = new ArrayDeque<>();
        SyntaxTreeNode r, l;
        for (char c : postfix.toCharArray()) {
            if (alphabetHas(c)) {
                stk.push(new LeafNode(c));
            } else {
                switch (c) {
                    case STAR:
                        stk.push(new StarNode(stk.pop()));
                        break;
                    case CONCAT:
                        r = stk.pop();
                        l = stk.pop();
                        stk.push(new ConcatNode(l, r));
                        break;
                    case OR:
                        r = stk.pop();
                        l = stk.pop();
                        stk.push(new OrNode(l, r));
                        break;
                    default:
                        throw new IllegalStateException("Unexpected value: " + c);
                }
            }
        }
        if (stk.size() != 1) throw new IllegalArgumentException("Malformed postfix: " + postfix + ", \ncheck regex");
        root = stk.pop();
    }

    public boolean alphabetHas(char c) {
        for (char ch : alphabet)
            if (ch == c)
                return true;
        return false;
    }
}
