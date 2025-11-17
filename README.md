# CS.410 Graph System

**An interactive desktop application for learning and working with computational automata and formal languages.**

---

## What is This?

CS.410 Graph System is an educational tool that helps students and educators visualize, test, and understand fundamental concepts in computer science theory. It provides a graphical interface for creating and simulating six types of computational models:

- **DFA** (Deterministic Finite Automaton)
- **NFA** (Nondeterministic Finite Automaton)
- **PDA** (Push-down Automaton)
- **TM** (Turing Machine)
- **CFG** (Context-Free Grammar)
- **REX** (Regular Expression)

Each model can be defined in simple text files, visualized as state diagrams, and tested with input strings to see if they're accepted or rejected.

---

## Features

- **Visual Editor**: Write automaton definitions with syntax highlighting and line numbers
- **Live Visualization**: See your automaton rendered as an interactive graph using Graphviz
- **Interactive Testing**: Test input strings and see step-by-step execution traces
- **File Management**: Save/load automaton definitions with automatic recent files tracking
- **Batch Testing**: Run multiple test cases from files to validate your automaton
- **Multiple Tabs**: Work on multiple automata simultaneously
- **Keyboard Shortcuts**: Fast workflow with keyboard commands (see below)
- **Undo/Redo**: Full undo/redo support for editing
- **Cross-platform**: Works on Windows, macOS, and Linux

---

## Quick Start

### Requirements

- **Java 8** or higher
- **Maven** (only for building from source)

### Running the Application

#### Option 1: Download Pre-built JAR (Recommended)

1. Download the latest `CS410-Exam-1.0.2.jar` from the releases page
2. Run the application:
   ```bash
   java -jar CS410-Exam-1.0.2.jar
   ```

**No Maven or compilation needed!** Just download and run.

#### Option 2: Run with Maven
```bash
mvn compile exec:java
```

#### Option 3: Build JAR from Source
```bash
mvn clean package
java -jar target/CS410-Exam-1.0.2.jar
```

---

## How to Use

### Creating a New Automaton

1. **File → New** → Select automaton type (DFA, NFA, PDA, TM, CFG, or REX)
2. Write your automaton definition in the text editor (see formats below)
3. Click **Compile with Figure** (or press `Cmd/Ctrl+B`) to visualize
4. The graph will appear on the right side

### Testing Input Strings

1. After compiling, click **Run** (or press `Cmd/Ctrl+R`)
2. Enter an input string when prompted
3. The system will show if the input is **Accepted** or **Rejected**
4. View detailed execution traces in the output panel

### Testing with Files

1. Create a test file with one input per line
2. Click **Run with File** (or press `Cmd/Ctrl+Shift+R`)
3. Select your test file
4. See results for all test cases at once

### Keyboard Shortcuts

| Action | Windows/Linux | macOS |
|--------|--------------|--------|
| Compile with Figure | `Ctrl+B` | `Cmd+B` |
| Run | `Ctrl+R` | `Cmd+R` |
| Run with File | `Ctrl+Shift+R` | `Cmd+Shift+R` |
| Save | `Ctrl+S` | `Cmd+S` |
| Open File | `Ctrl+O` | `Cmd+O` |
| Recent File 1-9 | `Ctrl+1-9` | `Cmd+1-9` |

---

## Supported Automata

### 1. DFA (Deterministic Finite Automaton)

**What it does:** Accepts or rejects strings based on a fixed set of rules. For each state and input symbol, there's exactly one next state.

**Example use cases:**
- Strings with an even number of 'a's
- Strings ending with "01"
- Binary numbers divisible by 3

**File format:** `.dfa`

```text
states: q0 q1 q2
alphabet: a b
start: q0
finals: q2
transitions:
q0 -> q1 (a)
q0 -> q0 (b)
q1 -> q2 (b)
q2 -> q1 (a)
```

---

### 2. NFA (Nondeterministic Finite Automaton)

**What it does:** Like DFA, but can have multiple possible transitions for the same input. Can also have epsilon (ε) transitions that don't consume input.

**Example use cases:**
- Pattern matching (strings containing "ab")
- Union of multiple patterns
- Strings with specific substrings

**File format:** `.nfa`

```text
states: q0 q1 q2
alphabet: a b
start: q0
finals: q2
transitions:
q0 -> q1 (a eps)
q0 -> q0 (b)
q1 -> q2 (b)
q2 -> q1 (a)
```

**Note:** Use `eps` for epsilon transitions. Multiple symbols can be listed for each transition.

---

### 3. PDA (Push-down Automaton)

**What it does:** Extends finite automata with a stack memory. Can recognize context-free languages like balanced parentheses and matched pairs.

**Example use cases:**
- Balanced parentheses: `((()))`
- Strings of form `a^n b^n` (same number of a's and b's)
- Nested structures (HTML tags, JSON)

**File format:** `.pda`

```text
states: q0 q1 q2
alphabet: a b
stack_alphabet: Z a
start: q0
stack_start: Z
finals: q2
transitions:
q0 a Z -> q0 aZ
q0 a a -> q0 aa
q0 b a -> q1 eps
q1 b a -> q1 eps
q1 eps Z -> q2 eps
```

**Transition format:** `<state> <input> <stack_pop> -> <next_state> <stack_push>`
- Use `eps` for no input or no stack operation
- Stack push can be multiple characters

---

### 4. TM (Turing Machine)

**What it does:** The most powerful computational model. Has an infinite tape that can be read and written, with a head that moves left or right.

**Example use cases:**
- Recognize any decidable language
- Perform computations (addition, multiplication)
- Copy strings, reverse strings

**File format:** `.tm`

```text
states: q0 q1 q_accept q_reject
input_alphabet: 0 1
tape_alphabet: 0 1 X _
start: q0
accept: q_accept
reject: q_reject
transitions:
q0 0 -> q1 X R
q0 1 -> q0 1 R
q0 _ -> q_accept _ R
q1 0 -> q0 0 R
q1 1 -> q1 1 R
q1 _ -> q_reject _ R
```

**Transition format:** `<state> <read_symbol> -> <next_state> <write_symbol> <direction>`
- Direction is `L` (left) or `R` (right)
- Use `_` for blank symbol

---

### 5. CFG (Context-Free Grammar)

**What it does:** Defines a language using production rules that transform variables into strings of variables and terminals.

**Example use cases:**
- Arithmetic expressions: `1 + 2 * 3`
- Programming language syntax
- Natural language patterns

**File format:** `.cfg`

```text
Variables = S A B
Terminals = a b
Start = S

S -> A B | a S b
A -> a | a A
B -> b | b B
```

**Production rules:**
- Left side: a variable (uppercase)
- Right side: sequence of variables and terminals
- Use `|` for alternatives
- Use `eps` for epsilon (empty string)

The system uses the **CYK algorithm** for parsing.

---

### 6. REX (Regular Expression)

**What it does:** Pattern matching using regular expression syntax. Can generate test cases and validate regex correctness.

**Example use cases:**
- Text pattern matching
- Input validation
- String search and replace

**File format:** `.rex`

```text
(ab*c) u d u eps
a b c d
```

**Format:**
- Line 1: The regular expression
- Line 2: Alphabet (space-separated symbols)

**Operators:**
- `*` - Zero or more (Kleene star)
- `u` - OR (alternation)
- `()` - Grouping
- Concatenation (implicit)

---

## File Formats Quick Reference

| Type | Extension | Key Sections |
|------|-----------|--------------|
| DFA | `.dfa` | states, alphabet, start, finals, transitions |
| NFA | `.nfa` | states, alphabet, start, finals, transitions |
| PDA | `.pda` | states, alphabet, stack_alphabet, start, stack_start, finals, transitions |
| TM | `.tm` | states, input_alphabet, tape_alphabet, start, accept, reject, transitions |
| CFG | `.cfg` | Variables, Terminals, Start, Productions |
| REX | `.rex` | regex pattern, alphabet |

**Comments:** All formats support comments with `#` (except REX)

---

## Building from Source

### Prerequisites
- Java 8 or higher
- Apache Maven 3.x

### Build Commands

```bash
# Clean and compile
mvn clean compile

# Run tests
mvn test

# Run specific test
mvn test -Dtest=DFATest

# Package as JAR
mvn package

# Clean build artifacts
mvn clean
```

### Project Structure

```
CS410-Exam/
├── src/
│   ├── main/java/
│   │   ├── common/              # Base classes and utilities
│   │   ├── DeterministicFiniteAutomaton/
│   │   ├── NondeterministicFiniteAutomaton/
│   │   ├── PushDownAutomaton/
│   │   ├── TuringMachine/
│   │   ├── ContextFreeGrammar/
│   │   ├── RegularExpression/
│   │   └── UserInterface/       # GUI components
│   └── test/java/              # Unit tests
├── pom.xml                     # Maven configuration
└── README.md                   # This file
```

---

## Dependencies

- **JUnit 5.8.2** - Testing framework
- **GraphViz Java 0.18.1** - Graph visualization
- **GraalVM JS 21.3.3** - JavaScript engine for GraphViz rendering

All dependencies are managed by Maven and will be downloaded automatically.

---

## Development Team

**CS.410 Graph System** was developed by:

- Ege Yenen
- Bora Baran
- Berre Delikara
- Eren Yemşen
- Berra Eğcin
- Hakan Akbıyık
- Hakan Çildaş
- Selim Özyılmaz
- Olcay Taner Yıldız

---

## Technical Documentation

For detailed technical documentation and architecture information, see [CLAUDE.md](CLAUDE.md).

For version history and changes, see [CHANGELOG.md](CHANGELOG.md).

---

## Troubleshooting

### Graph visualization not working
- Make sure GraalVM JS dependencies are installed (Maven handles this automatically)
- Try rebuilding: `mvn clean compile`

### Application won't start
- Verify Java version: `java -version` (must be Java 8+)
- Check Maven installation: `mvn -version`

### Tests failing
- Run with verbose output: `mvn test -X`
- Check specific test: `mvn test -Dtest=<TestName>`

---

## Getting Help

- Check the **Help → About** menu for version information
- Review example files in each automaton's package
- See technical documentation in individual README files within each package
- Report issues via the project repository

---

**Happy automaton building!** 🤖
