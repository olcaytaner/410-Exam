package RegularExpression.SyntaxTree;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;

import static RegularExpression.SyntaxTree.RegexOperator.*;

public class SyntaxTree {
    private final Map<Character, Integer> precedence = Map.of(STAR, 3, CONCAT, 2, OR, 1);
    public char[] alphabet;
    public SyntaxTreeNode root;

    public SyntaxTree(String regex, char[] alphabet) {
        this.alphabet = alphabet;
        String sanitizedReg = sanitize(regex);
        //System.out.println("Sanitized: " + sanitizedReg);
        String postfix = parse(sanitizedReg);
        //System.out.println("Postfix: " + postfix);
        compile(postfix);
    }


    // check for malformations
    private String sanitize(String regex) {
        // TODO check for other malformations like ** or * at the start
        StringBuilder sanitized = new StringBuilder();
        int parenthesisCount = 0;
        for (char c : regex.toCharArray()) {
            if ((c == '(' || alphabetHas(c)) && !sanitized.isEmpty()) {
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
    private String parse(String regex) {
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

    // build AST
    private void compile(String postfix) {
        Deque<SyntaxTreeNode> stk = new ArrayDeque<>();
        for (char c : postfix.toCharArray()) {
            if (alphabetHas(c)) {
                stk.push(new LeafNode(c));
            } else {
                switch (c) {
                    case STAR -> stk.push(new StarNode(stk.pop()));
                    case CONCAT -> {
                        SyntaxTreeNode r = stk.pop(), l = stk.pop();
                        stk.push(new ConcatNode(l, r));
                    }
                    case OR -> {
                        SyntaxTreeNode r = stk.pop(), l = stk.pop();
                        stk.push(new OrNode(l, r));
                    }
                    default -> throw new IllegalStateException("Unexpected value: " + c);
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
