package common;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
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
            // Log Java and GraalVM version info
            String javaVersion = System.getProperty("java.version");
            String javaVendor = System.getProperty("java.vendor");
            System.out.println("[GraphViz] Java Version: " + javaVersion + " (" + javaVendor + ")");

            try {
                GraphvizJdkEngine jdkEngine = new GraphvizJdkEngine();
                Graphviz.useEngine(jdkEngine);
                System.out.println("[GraphViz] Forced GraalVM JDK engine initialization");
            } catch (Exception engineError) {
                System.err.println("[GraphViz] Failed to force JDK engine: " + engineError.getMessage());
                engineError.printStackTrace();
            }

            // Generate graph image directly in memory - no files created
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Graphviz.fromString(dotCode)
                .render(Format.SVG)
                .toOutputStream(baos);

            baos.close();

            String svgText = new String(baos.toByteArray(), StandardCharsets.UTF_8);

            // Batik cannot parse "transparent"
            svgText = svgText.replaceAll("stroke=\"transparent\"", "stroke=\"none\"");

            if (svgText.contains("<svg") && svgText.contains("</svg>")) {
                System.out.println("[GraphViz] Graph rendered successfully using GraalVM JDK engine");
            }else {
                System.out.println("[GraphViz] Graph rendering failed");
            }

            return new JLabel(svgText);

        } catch (Exception e) {
            System.err.println("[GraphViz] Error generating graph: " + e.getMessage());
            e.printStackTrace();

            String errorDetails = e.getMessage();
            if (errorDetails == null) {
                errorDetails = e.getClass().getSimpleName();
            }

            String helpMessage;
            if (errorDetails.contains("None of the provided engines could be initialized")) {
                helpMessage = "<p>No JavaScript engine could be initialized.</p>"
                    + "<p>Please ensure GraalVM JS dependencies are in your classpath.</p>"
                    + "<p>Check that org.graalvm.js:js and org.graalvm.js:js-scriptengine are installed.</p>";
            } else if (errorDetails.contains("native library")) {
                helpMessage = "<p>Native library loading failed.</p>"
                    + "<p>This is expected on Apple Silicon (ARM64) - GraalVM fallback should work.</p>";
            } else {
                helpMessage = "<p>An unexpected error occurred during graph generation.</p>"
                    + "<p>Check console output for details.</p>";
            }

            // Return a more informative error label
            JLabel errorLabel = new JLabel("<html><body style='text-align: center; padding: 20px;'>"
                + "<h2>Graph Generation Failed</h2>"
                + "<p><b>Error:</b> " + errorDetails + "</p>"
                + helpMessage
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

  public String getDefaultTemplate() {
    return "";
  }
  

}
