package SyntaxTree;

import java.util.HashSet;
import java.util.Set;

public class ConcatNode extends BinaryNode {

    public ConcatNode(SyntaxTreeNode l, SyntaxTreeNode r) {
        super(l, r, '.');
    }

    public Set<Integer> match(String s, int pos) {
        HashSet<Integer> res = new HashSet<>();
        Set<Integer> mids = leftChild.match(s, pos);
        for (int mid : mids)
            res.addAll(rightChild.match(s, mid));
        return res;
    }

    public String generateOneCase() {
        return leftChild.generateOneCase() + rightChild.generateOneCase();
    }

    public String generateOneCase(int maxStarRepeat) {
        return leftChild.generateOneCase(maxStarRepeat) + rightChild.generateOneCase(maxStarRepeat);
    }

    public Set<String> generateCasesExhaustive(int maxLen) {
        Set<String> left = leftChild.generateCasesExhaustive(maxLen);
        Set<String> right = rightChild.generateCasesExhaustive(maxLen);
        Set<String> res = new HashSet<>();
        for (String s1 : left)
            for (String s2 : right)
                if (s1.length() + s2.length() <= maxLen)
                    res.add(s1 + s2);
        return res;
    }
}
