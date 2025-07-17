package RegularExpression.SyntaxTree;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class LeafNode extends SyntaxTreeNode {
    public char sym;

    public LeafNode(char sym) {
        super(sym);
        this.sym = sym;
    }

    public Set<Integer> match(String s, int pos) {
        if (pos < s.length() && s.charAt(pos) == sym) {
            return Collections.singleton(pos + 1);
        } else {
            return new HashSet<>();
        }
    }

    public String generateOneCase() {
        return String.valueOf(sym);
    }

    public String generateOneCase(int maxStarRepeat) {
        return String.valueOf(sym);
    }

    public Set<String> generateCasesExhaustive(int maxLen) {
        if (maxLen > 1) {
            return Collections.singleton(String.valueOf(sym));
        } else {
            return Collections.emptySet();
        }
    }
}
