package RegularExpression.SyntaxTree;

import java.util.Set;

public abstract class SyntaxTreeNode {
    public char sym;

    public SyntaxTreeNode(char sym) {
        this.sym = sym;
    }

    public abstract Set<Integer> match(String s, int pos);

    public abstract String generateOneCase();

    public abstract String generateOneCase(int maxStarRepeat);

    public abstract Set<String> generateCasesExhaustive(int maxLen);
}
