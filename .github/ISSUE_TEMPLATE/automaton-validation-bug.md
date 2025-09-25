---
name: Automaton Validation Bug Report
about: Report an issue with automaton validation (DFA, NFA, PDA, TM, CFG, or Regular Expression)
title: '[VALIDATION] '
labels: bug, validation
assignees: ''

---

## Bug Description
<!-- Provide a clear and concise description of the validation issue -->

## Automaton Type
<!-- Check the type of automaton that has the issue -->
- [ ] DFA (Deterministic Finite Automaton)
- [ ] NFA (Nondeterministic Finite Automaton)
- [ ] PDA (Push-down Automaton)
- [ ] TM (Turing Machine)
- [ ] CFG (Context-Free Grammar)
- [ ] REX (Regular Expression)

## Automaton Definition
<!-- Paste your automaton definition here using the format below -->
```
Start:
Finals:
Alphabet:
States:

Transitions:

```

## Expected Behavior
<!-- What should happen when this automaton is validated? -->
<!-- For example: "This should be rejected as invalid because a DFA cannot have multiple transitions from the same state on the same symbol" -->

## Actual Behavior
<!-- What actually happens when you try to validate this automaton? -->
<!-- For example: "The automaton is accepted as valid" -->

## Test Inputs (if applicable)
<!-- If the issue is with execution, provide test inputs and expected outputs -->
```
Input:
Expected Output:
Actual Output:
```

## Steps to Reproduce
1. Create a new file with extension (e.g., `.dfa`, `.nfa`, etc.)
2. Paste the automaton definition
3. Load it in the application
4. Observe the validation result

## Environment
- Java Version:
- Operating System:
- Application Version/Commit:

## Additional Context
<!-- Add any other context, screenshots, or relevant information about the problem here -->

## Possible Solution
<!-- If you have suggestions on how to fix this issue, please describe them here -->