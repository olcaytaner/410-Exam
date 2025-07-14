package RegularExpression.SyntaxTree;

import java.util.HashSet;
import java.util.Set;

public class LeafNode extends SyntaxTreeNode {
    public char sym;

    public LeafNode(char sym) {
        super(sym);
        this.sym = sym;
    }

    public Set<Integer> match(String s, int pos) {
        return (pos < s.length() && s.charAt(pos) == sym) ? Set.of(pos + 1) : new HashSet<>();
    }

    public String generateOneCase() {
        return String.valueOf(sym);
    }

    public String generateOneCase(int maxStarRepeat) {
        return String.valueOf(sym);
    }

    public Set<String> generateCasesExhaustive(int maxLen) {
        return maxLen > 1 ? Set.of(String.valueOf(sym)) : Set.of();
    }
}
