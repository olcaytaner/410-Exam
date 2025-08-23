package TuringMachine;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TM2TestCaseGenerator {

    private static boolean isZeroNOneN(String s) {
        int n = s.length();
        if (n == 0) return true;
        if (n % 2 != 0) return false;

        int half = n / 2;
        String zeros = s.substring(0, half);
        String ones = s.substring(half);

        for(char c : zeros.toCharArray()) {
            if (c != '0') return false;
        }
        for(char c : ones.toCharArray()) {
            if (c != '1') return false;
        }

        return true;
    }

    public static List<String> generateTestCases() {
        List<String> testCases = new ArrayList<>();
        int maxLength = 15;

        for (int length = 0; length <= maxLength; length++) {
            int limit = (int) Math.pow(2, length);
            for (int i = 0; i < limit; i++) {
                String binaryStr = Integer.toBinaryString(i);
                if (binaryStr.length() < length) {
                    binaryStr = String.format("%" + length + "s", binaryStr).replace(' ', '0');
                }

                if (isZeroNOneN(binaryStr)) {
                    testCases.add(binaryStr + ",1");
                } else {
                    testCases.add(binaryStr + ",0");
                }
            }
        }
        return testCases;
    }

    public static void main(String[] args) {
        List<String> testCases = generateTestCases();

        // Write to file
        try (FileWriter writer = new FileWriter("src/test/manual-testing/exercises/week10/week10_tm2.test")) {
            for (String caseStr : testCases) {
                writer.write(caseStr + "\n");
            }
            System.out.println("Test cases written to 'week10_tm2.test'");
            System.out.println("Total test cases generated: " + testCases.size());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}