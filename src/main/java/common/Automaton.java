package common;

import java.util.List;

public abstract class Automaton {

  public enum MachineType {
    DFA,
    NFA,
    PDA,
    TM,
    CFG,
    REGEX
  }

  protected MachineType type;
  public MachineType getType() {
    return type;
  }

  protected Automaton(MachineType type) {
    this.type = type;
  }

//classes

  public static class ValidationMessage {

    public enum ValidationMessageType {
      ERROR,
      WARNING,
      INFO
    }

    private String message;
    private int lineNumber;
    private ValidationMessageType type;

    public ValidationMessage(String message, int lineNumber, ValidationMessageType type) {
      this.message = message;
      this.lineNumber = lineNumber;
      this.type = type;
    }

    public String getMessage() {
      return message;
    }

    public int getLineNumber() {
      return lineNumber;
    }

    public ValidationMessageType getType() {
      return type;
    }

    @Override
    public String toString() {
      return String.format("%s: %s in line %d", type, message, lineNumber);
    }

  }

  public static class ParseResult {
    private boolean success;
    private List<ValidationMessage> validationMessages;
    private Automaton automaton;

    public ParseResult(boolean success, List<ValidationMessage> validationMessages, Automaton automaton) {
      this.success = success;
      this.validationMessages = validationMessages;
      this.automaton = automaton;
    }

    public boolean isSuccess() {
      return success;
    }

    public List<ValidationMessage> getValidationMessages() {
      return validationMessages;
    }

    public Automaton getAutomaton() {
      return automaton;
    }
  }

  public static class ExecutionResult { //puanlamada kullanılabilir
    private boolean accepted;
    private List<ValidationMessage> runtimeMessages;
    private String trace;

    public ExecutionResult(boolean accepted, List<ValidationMessage> runtimeMessages, String trace) {
      this.accepted = accepted;
      this.runtimeMessages = runtimeMessages;
      this.trace = trace;
    }

    public boolean isAccepted() {
      return accepted;
    }

    public List<ValidationMessage> getRuntimeMessages() {
      return runtimeMessages;
    }

    public String getTrace() {
      return trace;
    }
  }

//functions

  public abstract ParseResult parse(String inputText);
  public abstract ExecutionResult execute(String inputText);
  public abstract List<ValidationMessage> validate();

  public abstract String toDotCode(String inputText);


}