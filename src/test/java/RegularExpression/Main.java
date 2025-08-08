package RegularExpression;

import RegularExpression.SyntaxTree.RegularExpression;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {

        String regex = "0(01)*1"; // matches multiples of 3
        //String regex = "(1u0)*01";
        RegularExpression re = new RegularExpression(regex, new char[]{'0', '1'});

        System.out.println(re.match("00101011"));

        //System.out.println(re.generateOneCase(10));
        //System.out.println(re.generateCases(10, 3));
    }
}
