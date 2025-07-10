package SyntaxTree;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class RegularExpression {
    public SyntaxTree.SyntaxTree st;
    String regex;

    public RegularExpression(String regex, char[] alphabet) {
        this.regex = regex;
        st = new SyntaxTree.SyntaxTree(regex, alphabet);
    }

    public boolean match(String s) {
        return st.root.match(s, 0).contains(s.length());
    }

    String generateOneCase() {
        return st.root.generateOneCase();
    }

    String generateOneCase(int maxStarRepeat) {
        return st.root.generateOneCase(maxStarRepeat);
    }

    Set<String> generateCases(int cnt) {
        Set<String> cases = new HashSet<>();
        while (cases.size() != cnt)
            cases.add(generateOneCase());
        return cases;
    }

    Set<String> generateCases(int cnt, int maxStarRepeat) {
        Set<String> cases = new HashSet<>();
        while (cases.size() != cnt)
            cases.add(generateOneCase(maxStarRepeat));
        return cases;
    }

    Set<String> generateCasesExhaustive(int maxLen) {
        return st.root.generateCasesExhaustive(maxLen);
    }

    public void generateCasesExhaustive(int maxLen, String path) {
        Set<String> cases = st.root.generateCasesExhaustive(maxLen);
        System.out.println(cases);
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(path))) {
            for (String case_ : cases) {
                bw.write(case_);
                bw.newLine();
            }
        } catch (IOException _) {
            System.out.println("Write unsuccessful");
        }
    }
}
