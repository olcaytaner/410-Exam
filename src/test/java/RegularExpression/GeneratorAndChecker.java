package RegularExpression;

import RegularExpression.Checker.Check;
import RegularExpression.SyntaxTree.RegularExpression;

/**
 * This is how you generate correct and incorrect cases to check in RegularExpression, in large sizes.
 */
public class GeneratorAndChecker {
    public static void main(String[] args) {
        String correct_reg = "((a u b)*) u ((a u b)* c (a u b)*) u ((a u b)* c (a u b)* c (a u b)*)";
        RegularExpression re = new RegularExpression(correct_reg, new char[]{'a', 'b', 'c'});

        re.generateCorrectCasesExhaustive(6, "src/test/manual-testing/examples/rex/rex.test");
        re.generateWrongCasesExhaustive(6, "src/test/manual-testing/examples/rex/rex.test");


        Check.check("src/test/manual-testing/examples/rex/regex-correct.rex",
                "src/test/manual-testing/examples/rex/rex.test");
    }
}
