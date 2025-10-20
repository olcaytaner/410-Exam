package RegularExpression.SyntaxTree;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents a leaf node in a regular expression syntax tree.
 * <p>
 *     A {@code LeafNode} corresponds to a single symbol from the
 *     regular expression's alphabet (for example, <code>'a'</code>
 *     or <code>'1'</code>). It has no children and matches exactly
 *     one occurrence of its symbol in the input string.
 * </p>
 */
public class LeafNode extends SyntaxTreeNode {
    public char sym;

    public LeafNode(char sym) {
        super(sym);
        this.sym = sym;
    }

    public Set<Integer> match(String s, int pos) {
        if (sym == 'ε')
            return Collections.singleton(pos);
        if (pos < s.length() && s.charAt(pos) == sym)
            return Collections.singleton(pos + 1);
        return new HashSet<>();
    }

    public String generateOneCase() {
        return String.valueOf(sym);
    }

    public String generateOneCase(int maxStarRepeat) {
        return String.valueOf(sym);
    }

    public Set<String> generateCasesExhaustive(int maxLen) {
        if (sym == 'ε')
            return Collections.emptySet();
        if (maxLen > 1)
            return Collections.singleton(String.valueOf(sym));
        return Collections.emptySet();
    }
}
