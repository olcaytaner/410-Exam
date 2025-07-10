package DeterministicFiniteAutomaton;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DFAParser {
  public static DFA parse(String filePath) throws IOException {
    Path path = Paths.get(filePath);
    List<String> lines = Files.readAllLines(path);

    Map<String, State> stateMap = new HashMap<>();
    Set<Symbol> alphabet = new HashSet<>();
    Set<State> finalStates = new HashSet<>();
    Set<Transition> transitions = new HashSet<>();

    State startState = null;
    boolean inTransitionSection = false;

    Pattern transitionPattern = Pattern.compile(
      "^\\s*(\\w+)\\s*->\\s*(\\w+)\\s*\\(\\s*([a-zA-Z0-9]+(?:\\s+[a-zA-Z0-9]+)*)\\s*\\)\\s*$"
    );

    for (String rawLine : lines) {
      String line = rawLine.trim();
      if (line.isEmpty()) continue;

      if (line.startsWith("Start:")) {
        String startName = line.substring(6).trim();
        if (startName.equals("eps")) {
          throw new IllegalArgumentException("Invalid start state: 'eps' is reserved");
        }

        startState = stateMap.computeIfAbsent(startName, State::new);
        startState.setStart(true);

      } else if (line.startsWith("Finals:")) {
        String[] finals = line.substring(7).trim().split("\\s+");
        for (String name : finals) {
          if (name.equals("eps")) {
            throw new IllegalArgumentException("Invalid final state: 'eps' is reserved");
          }

          State s = stateMap.computeIfAbsent(name, State::new);
          s.setFinal(true);
          finalStates.add(s);
        }

      } else if (line.toLowerCase().startsWith("alphabet:")) {
        String[] symbols = line.substring(9).trim().split("\\s+");
        for (String sym : symbols) {
          if (sym.equals("eps")) {
            throw new IllegalArgumentException("Invalid symbol: 'eps' is reserved");
          }
          alphabet.add(new Symbol(sym.charAt(0)));
        }

      } else if (line.equalsIgnoreCase("Transitions:")) {
        inTransitionSection = true;

      } else if (inTransitionSection) {
        Matcher matcher = transitionPattern.matcher(line);

        if (matcher.matches()) {
          String fromName = matcher.group(1);
          String toName = matcher.group(2);
          String[] symbols = matcher.group(3).trim().split("\\s+");

          State from = stateMap.computeIfAbsent(fromName, State::new);
          State to = stateMap.computeIfAbsent(toName, State::new);

          for (String symStr : symbols) {
            if (symStr.equals("eps")) {
              throw new IllegalArgumentException("Invalid transition symbol: 'eps' is reserved");
            }

            Symbol sym = new Symbol(symStr.charAt(0));
            alphabet.add(sym);
            transitions.add(new Transition(from, sym, to));
          }

        } else {
          throw new IllegalArgumentException("Invalid transition line: " + line);
        }
      }
    }

    if (startState == null) {
      throw new IllegalStateException("No start state defined.");
    }

    DFA dfa = new DFA(
      new HashSet<>(stateMap.values()),
      alphabet,
      startState,
      finalStates,
      transitions
    );
    DFAValidator.validateCompleteness(dfa);

    return dfa;
  }


}
