package UserInterface;

import common.Automaton;
import java.io.File;

/**
 * Interface for automaton panels to provide common actions functionality
 */
public interface AutomatonPanel {
    
    /**
     * Compiles the automaton and generates a GraphViz visualization
     */
    void compileWithFigure();
    
    /**
     * Runs tests from the matching .test file
     */
    void run();
    
    /**
     * Runs tests from a user-selected test file
     */
    void runWithFile();
    
    /**
     * Saves the current content to a file (quick save if file exists, save as if new)
     */
    void saveAutomaton();
    
    /**
     * Always shows save dialog (Save As functionality)
     */
    default void saveAsAutomaton() {
        // Default implementation for backward compatibility
        saveAutomaton();
    }
    
    // Deprecated methods for backward compatibility
    @Deprecated
    default void runAutomaton() {
        compileWithFigure();
    }
    
    @Deprecated
    default void compileAutomaton() {
        // No longer needed - validation happens during compileWithFigure
    }
    
    @Deprecated
    default void testAutomaton() {
        run();
    }
    
    @Deprecated
    default void testAutomatonWithFile() {
        runWithFile();
    }
    
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
