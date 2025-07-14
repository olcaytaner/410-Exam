package RegularExpression.Checker;

import RegularExpression.SyntaxTree.RegularExpression;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Check {
    public static void check(String regexPath, String matchCasesPath, String nonMatchCasesPath) {
        RegularExpression re;

        try (BufferedReader br = new BufferedReader(new FileReader(regexPath))) {
            String line = br.readLine();
            String[] tokens = line.substring(line.indexOf(":") + 1).trim().split("\\s+");
            char[] alphabet = new char[tokens.length];
            for (int i = 0; i < tokens.length; ++i)
                alphabet[i] = tokens[i].charAt(0);

            String regex = br.readLine();

            re = new RegularExpression(regex, alphabet);
        } catch (IOException e) {
            System.err.println(regexPath + " read unsuccessful");
            return;
        }

        int correct = 0, wrong = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(matchCasesPath))) {
            String ln;
            while ((ln = br.readLine()) != null) {
                if (re.match(ln)) correct++;
                else wrong++;
            }
        } catch (IOException e) {
            System.err.println("Path: " + matchCasesPath);
            System.err.println("Match cases read unsuccessful");
            e.printStackTrace();
        }

        try (BufferedReader br = new BufferedReader(new FileReader(nonMatchCasesPath))) {
            String ln;
            while ((ln = br.readLine()) != null) {
                if (!re.match(ln)) correct++;
                else wrong++;
            }
        } catch (IOException e) {
            System.err.println("Path: " + nonMatchCasesPath);
            System.err.println("Non-match cases read unsuccessful");
            e.printStackTrace();
        }

        System.out.println("Correct: " + correct);
        System.out.println("Wrong: " + wrong);
        System.out.println("Accuracy: " + (double) correct / (correct + wrong));
    }

    public static void main(String[] args) {
        check("src/test/java/RegularExpression/Checker/input.txt",
                "src/test/java/RegularExpression/Checker/cases_match.txt",
                "src/test/java/RegularExpression/Checker/cases_not_match.txt");
    }
}
