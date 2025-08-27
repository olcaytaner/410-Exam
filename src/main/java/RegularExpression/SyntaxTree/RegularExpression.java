package RegularExpression.SyntaxTree;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents a regular expression.
 * <p>
 * Internally, this class builds a syntax tree from the given regex and alphabet,
 * which is used for matching.
 * </p>
 *
 * <p><strong>Example usage:</strong></p>
 * <pre>{@code
 * // Regex matches strings like "0(01)*1" — multiples of 3 in this case
 * String regex = "0(01)*1";
 * char[] alphabet = {'0', '1'};
 * RegularExpression re = new RegularExpression(regex, alphabet);
 *
 * // Test matching
 * boolean matches = re.match("00101");
 * }</pre>
 */
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

    /**
     * Generate correct cases for the regex and return the number of cases generated.
     * @param maxLen Max length of the case
     * @param path Path where you wish to save the cases to
     */
    public int generateCorrectCasesExhaustive(int maxLen, String path) {
        Set<String> cases = st.root.generateCasesExhaustive(maxLen);
        //System.out.println(cases);
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(path, true))) {
            for (String case_ : cases) {
                bw.write(case_);
                bw.write(",1");
                bw.newLine();
            }
        } catch (IOException e) {
            System.err.println("Write unsuccessful");
            e.printStackTrace();
        }
        return cases.size();
    }

    /**
     * Generate wrong cases for the regex and return the number of cases generated.
     * @param maxLen Max length of the case
     * @param path Path where you wish to save the cases to
     */
    public int generateWrongCasesExhaustive(int maxLen, String path) {
        Set<String> cases = new HashSet<>();
        rec(maxLen, new StringBuilder(), cases);
        //System.out.println(cases);
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(path, true))) {
            for (String case_ : cases) {
                bw.write(case_);
                bw.write(",0");
                bw.newLine();
            }
        } catch (IOException e) {
            System.err.println("Write unsuccessful");
            e.printStackTrace();
        }
        return cases.size();
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
        if (curCase.length() > 0)
            curCase.deleteCharAt(curCase.length() - 1);
    }
}
