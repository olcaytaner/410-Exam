package Checker;

import SyntaxTree.RegularExpression;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Check {
    public static void check(String regexPath, String casesPath) {
        RegularExpression re;

        try (BufferedReader br = new BufferedReader(new FileReader(regexPath))) {
            char[] alphabet = br.readLine().toCharArray();
            String regex = br.readLine();
            re = new RegularExpression(regex, alphabet);
        } catch (IOException e) {
            System.err.println(regexPath + " read unsuccessful");
            return;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(casesPath))) {
            int correct = 0, wrong = 0;
            String ln;
            while ((ln = br.readLine()) != null) {
                if (re.match(ln)) correct++;
                else wrong++;
            }
            System.out.println("Correct: " + correct);
            System.out.println("Wrong: " + wrong);
            System.out.println("Accuracy: " + (double) correct / (correct + wrong));
        } catch (IOException e) {
            System.err.println(casesPath + " read unsuccessful");
        }
    }

    public static void main(String[] args) {
        check("src\\Checker\\input.txt", "src\\Checker\\cases.txt");
    }
}
