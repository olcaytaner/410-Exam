package common;

import java.io.ByteArrayOutputStream;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.engine.GraphvizJdkEngine;

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
  public void setMachineType(MachineType type) {
    this.type = type;
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

public String getFileExtension(){
    switch(type){
      case DFA:
        return ".dfa";
      case NFA:
        return ".nfa";
      case PDA:
        return ".pda";
      case TM:
        return ".tm";
      case CFG:
        return ".cfg";
      case REGEX:
        return ".rex";
      default:
        return ".txt";
    }
  }

  public JLabel toGraphviz(String inputText) {
    {
        // Parse the input first to populate the automaton
        ParseResult parseResult = parse(inputText);
        
        // Check if parsing was successful
        if (!parseResult.isSuccess()) {
            // Return an error label if parsing failed
            JLabel errorLabel = new JLabel("<html><body style='text-align: center;'>"
                + "<h2>Parsing Failed</h2>"
                + "<p>Check warnings for details</p>"
                + "</body></html>");
            errorLabel.setHorizontalAlignment(SwingConstants.CENTER);
            return errorLabel;
        }
        
        // Use the successfully parsed automaton for DOT generation
        Automaton parsedAutomaton = parseResult.getAutomaton();
        if (parsedAutomaton == null) {
            parsedAutomaton = this; // fallback to current instance
        }
        
        String dotCode = parsedAutomaton.toDotCode(inputText);
        
        try {
            // Configure to use pure Java engine (no GraphViz installation required)
            // GraphvizJdkEngine uses GraalVM or Nashorn JavaScript engine for rendering
            // This works on all platforms including ARM64 macOS
            Graphviz.useEngine(new GraphvizJdkEngine());
            
            // Generate graph image directly in memory - no files created
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Graphviz.fromString(dotCode)
                .render(Format.PNG)
                .toOutputStream(baos);
            
            byte[] imageData = baos.toByteArray();
            baos.close();
            
            // Create ImageIcon directly from byte array
            ImageIcon imageIcon = new ImageIcon(imageData);
            JLabel imageLabel = new JLabel(imageIcon);
            return imageLabel;
            
        } catch (Exception e) {
            System.err.println("Error generating graph: " + e.getMessage());
            e.printStackTrace();
            
            // Return a more informative error label
            JLabel errorLabel = new JLabel("<html><body style='text-align: center;'>"
                + "<h2>Graph Generation Failed</h2>"
                + "<p>Error: " + e.getMessage() + "</p>"
                + "<p>Make sure GraalVM JS dependencies are installed</p>"
                + "</body></html>");
            errorLabel.setHorizontalAlignment(SwingConstants.CENTER);
            return errorLabel;
        }
    }
}
 
  public String inputText;
  
  public void setInputText(String inputText) {
    this.inputText = inputText;
  }
  
  public String getInputText() {
    return this.inputText;
  }


  public abstract ParseResult parse(String inputText);
  public abstract ExecutionResult execute(String inputText);
  public abstract List<ValidationMessage> validate();

  public abstract String toDotCode(String inputText);
 
  

}
