# Manual Testing Structure for Automata Theory

This directory contains organized test scenarios for the CS.410 Automata Theory course exam system.

## Directory Structure

```
manual-testing/
├── exercises/           # Weekly course exercises
│   ├── week1/          # Week 1 NFA exercises
│   ├── week2/          # Week 2 NFA exercises  
│   ├── week3/          # Week 3 NFA exercises
│   ├── week4/          # Week 4 NFA exercises
|   ├── week6/          # Week 6 CFG exercises
|   ├── week7/          # Week 7 CFG exercises 
│   └── week10/         # Week 10 Turing Machine exercises
├── examples/           # Example automata by type
│   ├── dfa/           # Deterministic Finite Automata examples
│   ├── nfa/           # Non-deterministic Finite Automata examples
│   ├── pda/           # Push-down Automata examples
│   ├── tm/            # Turing Machine examples
│   └── cfg/           # Context-Free Grammar examples
└── README.md          # This file
```

## File Naming Convention

- **Automaton files**: Named with appropriate extensions (`.nfa`, `.dfa`, `.pda`, `.tm`, `.cfg`)
- **Test files**: Matching name with `.test` extension
- **Example**: `week1-1.nfa` has corresponding test data in `week1-1.test`

## Test File Format

Test files use simple CSV format:
```
inputString,expectedResult
```

Where:
- `inputString` is the input to test (empty for epsilon/empty string)
- `expectedResult` is `1` for accept, `0` for reject

### Example Test File (`week1-1.test`):
```
,0
0,1
1,1
00,1
01,0
10,0
```

## Using the Test System

### In the GUI Application:

1. **Open an automaton file** from this directory (e.g., `week1-1.nfa`)
2. **Click the "Test" button** in the UI
3. **System automatically finds** the corresponding `.test` file
4. **View results** showing which tests passed/failed

### Test Results Display:

```
Test Results:
Passed: 8/10 tests

✓ ε → REJECT
✓ "0" → ACCEPT  
✓ "1" → ACCEPT
✗ "00" → REJECT (Expected: ACCEPT)
✓ "01" → REJECT
```

## Creating New Test Scenarios

### For Students:
1. Create your automaton file (e.g., `mytest.nfa`)
2. Create corresponding test file (e.g., `mytest.test`)
3. Place both files in the same directory
4. Use the Test button in the GUI

### For Instructors:
1. Create exercise automaton files in appropriate week folders
2. Create comprehensive test files covering edge cases
3. Ensure test files thoroughly validate student solutions

## File Organization Guidelines

- **Exercises**: Place weekly exercises in `exercises/weekN/`
- **Examples**: Place reference examples in `examples/type/`
- **Pairing**: Always create `.test` files for `.automaton` files
- **Naming**: Use descriptive names (e.g., `nfa-basic.nfa`, `dfa-complex.dfa`)

## Supported Automaton Types

- **DFA** (`.dfa`): Deterministic Finite Automata
- **NFA** (`.nfa`): Non-deterministic Finite Automata  
- **PDA** (`.pda`): Push-down Automata
- **TM** (`.tm`): Turing Machines
- **CFG** (`.cfg`): Context-Free Grammars

## Notes

- Test files must be in the **same directory** as automaton files for UI testing to work
- Empty string input is represented as empty field before comma: `,1`
- Comments in test files start with `#`
- The system validates automaton syntax before running tests
- Failed parsing will prevent test execution with appropriate error messages

## Examples in This Repository

- `exercises/week1/week1-1.nfa` - Basic NFA with binary alphabet
- `examples/dfa/dfa-basic.dfa` - Multi-state DFA with complex transitions
- `examples/nfa/nfa-correct.nfa` - Correct NFA with epsilon transitions

Each example includes comprehensive test cases demonstrating expected behavior.
