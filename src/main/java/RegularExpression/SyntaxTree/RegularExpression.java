package RegularExpression.SyntaxTree;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class RegularExpression {
    public SyntaxTree st;
    String regex;

    public RegularExpression(String regex, char[] alphabet) {
        this.regex = regex;
        st = new SyntaxTree(regex, alphabet);
    }

    public boolean match(String s) {
        return st.root.match(s, 0).contains(s.length());
    }


                        /* ******************************************* */
                        /*  ↓↓↓↓↓↓↓↓↓↓ FOR CASE GENERATION ↓↓↓↓↓↓↓↓↓↓  */
                        /* ******************************************* */

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

    Set<String> generateCorrectCasesExhaustive(int maxLen) {
        return st.root.generateCasesExhaustive(maxLen);
    }

    public void generateCorrectCasesExhaustive(int maxLen, String path) {
        Set<String> cases = st.root.generateCasesExhaustive(maxLen);
        System.out.println(cases);
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(path))) {
            for (String case_ : cases) {
                bw.write(case_);
                bw.newLine();
            }
        } catch (IOException e) {
            System.err.println("Write unsuccessful");
            e.printStackTrace();
        }
    }

    public void generateWrongCasesExhaustive(int maxLen, String path) {
        Set<String> cases = new HashSet<>();
        rec(maxLen, new StringBuilder(), cases);
        System.out.println(cases);
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(path))) {
            for (String case_ : cases) {
                bw.write(case_);
                bw.newLine();
            }
        } catch (IOException e) {
            System.err.println("Write unsuccessful");
            e.printStackTrace();
        }
    }

    private void rec(int maxLen, StringBuilder curCase, Set<String> cases) {
        String s = new String(curCase);
        if (!match(s))
            cases.add(s);
        if (curCase.length() == maxLen) {
            curCase.deleteCharAt(curCase.length() - 1);
            return;
        }
        for (char c : st.alphabet) {
            curCase.append(c);
            rec(maxLen, curCase, cases);
        }
        if (!curCase.isEmpty())
            curCase.deleteCharAt(curCase.length() - 1);
    }
}
