package UserInterface;

import common.Automaton;
import java.io.File;

/**
 * Interface for automaton panels to provide common actions functionality
 */
public interface AutomatonPanel {
    
    /**
     * Runs the automaton with current text input and displays the graph
     */
    void runAutomaton();
    
    /**
     * Compiles/validates the automaton and displays warnings
     */
    void compileAutomaton();
    
    /**
     * Saves the current content to a file
     */
    void saveAutomaton();
    
    /**
     * Gets the current text from the text editor
     */
    String getTextAreaContent();
    
    /**
     * Gets the automaton associated with this panel
     */
    Automaton getAutomaton();
    
    /**
     * Gets the current file if any
     */
    File getCurrentFile();
    
    /**
     * Sets the current file
     */
    void setCurrentFile(File file);
    
    /**
     * Adds a document listener to track text changes
     */
    void addTextChangeListener(Runnable onChange);
}
