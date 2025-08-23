package TuringMachine;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TM1TestCaseGenerator {
    static int maxLength=18;
    // Generate test cases for binary strings of length <= maxLength
    public static List<String> generateTestCases() {
        List<String> testCases = new ArrayList<>();


        for (int length = 0; length <= maxLength; length++) {
            int limit = (int) Math.pow(2, length);
            for (int i = 0; i < limit; i++) {
                // Convert to binary string with leading zeros
                String binaryStr = Integer.toBinaryString(i);
                if (binaryStr.length() < length) {
                    binaryStr = String.format("%" + length + "s", binaryStr).replace(' ', '0');
                }

                // Count number of 1's
                long onesCount = binaryStr.chars().filter(ch -> ch == '1').count();

                // Check if divisible by 3
                int output = (onesCount % 3 == 0) ? 1 : 0;

                testCases.add(binaryStr + "," + output);
            }
        }
        return testCases;
    }




    public static void main(String[] args) {
        List<String> testCases = generateTestCases();

        // Write to file
        try (FileWriter writer = new FileWriter("src/test/manual-testing/exercises/week10/week10_tm1_"+maxLength+".test")) {
            for (String caseStr : testCases) {
                writer.write(caseStr + "\n");
            }
            System.out.println("Test cases written to 'binary_test_cases.txt'");
            System.out.println("Total test cases generated: " + testCases.size());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}

