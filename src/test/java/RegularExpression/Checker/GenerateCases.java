package RegularExpression.Checker;

import RegularExpression.SyntaxTree.RegularExpression;

public class GenerateCases {
    public static void main(String[] args) {
        String regex = "(0+1(01*0)*1)*";
        RegularExpression re = new RegularExpression(regex, new char[]{'0', '1'});
        //re.generateCorrectCasesExhaustive(5, "src/test/java/RegEx/Checker/cases_match.txt");
        re.generateWrongCasesExhaustive(5, "src/test/java/RegularExpression/Checker/cases_not_match.txt");

    }
}
