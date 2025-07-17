package RegularExpression.SyntaxTree;

import java.util.*;

public class StarNode extends UnaryNode {

    public StarNode(SyntaxTreeNode child) {
        super(child);
    }

    public Set<Integer> match(String s, int pos) {
        Set<Integer> res = new HashSet<>();
        Deque<Integer> stack = new ArrayDeque<>(); // bfs will also work

        res.add(pos); // because * allows 0 repetitions
        stack.push(pos);

        while (!stack.isEmpty()) {
            int p = stack.pop();
            Set<Integer> nextEnds = child.match(s, p);
            for (int nxt : nextEnds) {
                if (!res.contains(nxt)) {
                    res.add(nxt);
                    stack.push(nxt);
                }
            }
        }
        return res;
    }

    public String generateOneCase() {
        Random rand = new Random();
        int repeat = rand.nextInt(4);
        String base = child.generateOneCase();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < repeat; i++) {
            sb.append(base);
        }
        return sb.toString();
    }

    public String generateOneCase(int maxStarRepeat) {
        Random rand = new Random();
        int repeat = rand.nextInt(maxStarRepeat + 1);
        String base = child.generateOneCase(maxStarRepeat);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < repeat; i++) {
            sb.append(base);
        }
        return sb.toString();
    }

    public Set<String> generateCasesExhaustive(int maxLen) {
        Set<String> base = child.generateCasesExhaustive(maxLen);
        Set<String> res = new HashSet<>();

        Deque<String> q = new ArrayDeque<>();
        q.add("");

        while (!q.isEmpty()) {
            String prefix = q.poll();
            res.add(prefix);
            for (String s : base)
                if (prefix.length() + s.length() <= maxLen && !res.contains(prefix + s))
                    q.add(prefix + s);
        }

        return res;
    }
}
