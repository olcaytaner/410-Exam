# Regular Expression Syntax Tree

Implements a **regular expression engine** using a syntax tree.  
It also includes a **checker utility** that can evaluate regex correctness against labeled datasets.

---

## Features

- ✅ Parse a regex and build its **syntax tree**
- ✅ Match strings against the regex
- ✅ Generate **matching (positive)** and **non-matching (negative)** test cases
- ✅ Save cases in CSV format (`string,label`)
- ✅ Evaluate test cases against a regex (compute accuracy and other kind of metrics)

---

## Example Usage

### 1. Using `RegularExpression`

```java
import RegularExpression.Checker.Check;
import RegularExpression.SyntaxTree.RegularExpression;

public class Main {
    public static void main(String[] args) {
        // Regex matches strings like "0(01)*1" — multiples of 3 in binary
        String regex = "0(01)*1";
        char[] alphabet = {'0', '1'};
        RegularExpression re = new RegularExpression(regex, alphabet);

        // Test matching
        boolean matches = re.match("00101"); // true
        System.out.println(matches);

        // Generate exhaustive correct cases and save to file
        re.generateCorrectCasesExhaustive(5, "correct_cases.txt");

        // Generate exhaustive wrong cases and save to file
        re.generateWrongCasesExhaustive(5, "wrong_cases.txt");
        
        // Checking
        String check_regex_loc = "regex.rex";
        String cases_loc = "regex_cases.test";
        
        // Prints results to stdout
        Check.check(check_regex_loc, cases_loc);
    }
}
```

### Note:

**regex.rex** should be in the form:\
`(ab*)c` // the regular expression\
`a b c` // the alphabet, whitespace seperated


**regex_cases.test** should be in the form:\
`case,label`\
`case,label`\
`case,label`\
(any desired amount of cases ...)\
Where `label` is either `0` or `1`