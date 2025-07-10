package RegEx.Checker;

import SyntaxTree.RegularExpression;

import java.util.Arrays;
import java.util.List;

public class RegexSynthesizer {
    static final List<Character> chars = Arrays.asList('1', '2', '3', '+', '|', '*', '(', ')');
    static final char[] alphabet = new char[]{'1', '2', '3'};
    static final int maxLen = 8;

    static final List<String> cases = List.of("121212123", "212123", "11221212121122112211221212121212121212122211121212112213");
    //TODO add don't match cases

    // TODO prune the search space
    static void permute(int idx, StringBuilder regex) {
        if (idx == maxLen + 1) return;
        try {
            RegularExpression re = new RegularExpression(new String(regex), alphabet);
            boolean matchesAll = true;
            for (String case_ : cases)
                if (!re.match(case_)) {
                    matchesAll = false;
                    break;
                }
            if (matchesAll) System.out.println(regex);
        } catch (Exception _) {
        }
        for (char c : chars) {
            permute(idx + 1, regex.append(c));
            regex.deleteCharAt(regex.length() - 1);
        }
    }

    public static void main(String[] args) {
        // (1+2)*3
        permute(0, new StringBuilder());
    }
}
