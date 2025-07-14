package RegularExpression.SyntaxTree;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class OrNode extends BinaryNode {

    public OrNode(SyntaxTreeNode l, SyntaxTreeNode r) {
        super(l, r, '+');
    }

    public Set<Integer> match(String s, int pos) {
        HashSet<Integer> res = new HashSet<>();
        res.addAll(leftChild.match(s, pos));
        res.addAll(rightChild.match(s, pos));
        return res;
    }

    public String generateOneCase() {
        Random rand = new Random();
        boolean l = rand.nextBoolean();
        if (l) return leftChild.generateOneCase();
        return rightChild.generateOneCase();
    }

    public String generateOneCase(int maxStarRepeat) {
        Random rand = new Random();
        boolean l = rand.nextBoolean();
        if (l) return leftChild.generateOneCase(maxStarRepeat);
        return rightChild.generateOneCase(maxStarRepeat);
    }

    public Set<String> generateCasesExhaustive(int maxLen) {
        Set<String> res = new HashSet<>();
        res.addAll(leftChild.generateCasesExhaustive(maxLen));
        res.addAll(rightChild.generateCasesExhaustive(maxLen));
        return res;
    }
}
