package Checker;

import SyntaxTree.RegularExpression;

public class GenerateCases {
    public static void main(String[] args) {
        String regex = "(0+1(01*0)*1)*";
        RegularExpression re = new RegularExpression(regex, new char[]{'0', '1'});
        re.generateCasesExhaustive(5, "src\\cases.txt");
    }
}
