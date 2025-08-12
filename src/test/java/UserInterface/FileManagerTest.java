package UserInterface;

import common.Automaton;
import NondeterministicFiniteAutomaton.NFA;
import DeterministicFiniteAutomaton.DFA;
import PushDownAutomaton.PDA;
import TuringMachine.TM;
// import ContextFreeGrammar.CFGAutomaton; // TODO: Uncomment when implemented
// import RegularExpression.RegularExpressionAutomaton; // TODO: Uncomment when implemented

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * JUnit 5 test class for FileManager functionality.
 * Tests file operations, extension mapping, and panel creation.
 */
@DisplayName("FileManager Tests")
public class FileManagerTest {

    private MainPanel.FileManager fileManager;
    private MainPanel mockMainPanel;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        // Create a minimal MainPanel for testing
        mockMainPanel = new MainPanel();
        fileManager = mockMainPanel.fileManager;
    }

    @Nested
    @DisplayName("File Extension Tests")
    class FileExtensionTests {
        
        @Test
        @DisplayName("NFA should return .nfa extension")
        void testNFAExtension() {
            NFA nfa = new NFA();
            assertEquals(".nfa", fileManager.getExtensionForAutomaton(nfa),
                "NFA should return .nfa extension");
        }
        
        @Test
        @DisplayName("DFA should return .dfa extension")
        void testDFAExtension() {
            DFA dfa = new DFA();
            assertEquals(".dfa", fileManager.getExtensionForAutomaton(dfa),
                "DFA should return .dfa extension");
        }
        
        @Test
        @DisplayName("PDA should return .pda extension")
        void testPDAExtension() {
            PDA pda = new PDA();
            assertEquals(".pda", fileManager.getExtensionForAutomaton(pda),
                "PDA should return .pda extension");
        }
        
        @Test
        @DisplayName("TM should return .tm extension")
        void testTMExtension() {
            TM tm = new TM();
            assertEquals(".tm", fileManager.getExtensionForAutomaton(tm),
                "TM should return .tm extension");
        }
        
        // TODO: Uncomment when CFGAutomaton is implemented
        /*
        @Test
        @DisplayName("CFGAutomaton should return .cfg extension")
        void testCFGExtension() {
            CFGAutomaton cfg = new CFGAutomaton();
            assertEquals(".cfg", fileManager.getExtensionForAutomaton(cfg),
                "CFGAutomaton should return .cfg extension");
        }
        */
        
        // TODO: Uncomment when RegularExpressionAutomaton is implemented
        /*
        @Test
        @DisplayName("RegularExpressionAutomaton should return .rex extension")
        void testREXExtension() {
            RegularExpressionAutomaton rex = new RegularExpressionAutomaton();
            assertEquals(".rex", fileManager.getExtensionForAutomaton(rex),
                "RegularExpressionAutomaton should return .rex extension");
        }
        */
        
        @Test
        @DisplayName("Unknown automaton should return .txt extension")
        void testUnknownExtension() {
            // Create a mock automaton that doesn't match any known types
            Automaton unknown = new Automaton(Automaton.MachineType.DFA) {
                @Override
                public ParseResult parse(String inputText) { return null; }
                @Override
                public ExecutionResult execute(String inputText) { return null; }
                @Override
                public java.util.List<ValidationMessage> validate() { return null; }
                @Override
                public String toDotCode(String inputText) { return null; }
            };
            
            assertEquals(".txt", fileManager.getExtensionForAutomaton(unknown),
                "Unknown automaton should return .txt extension");
        }
    }

    @Nested
    @DisplayName("Color Mapping Tests")
    class ColorMappingTests {
        
        @Test
        @DisplayName("NFA and PDA extensions should have green color")
        void testGreenExtensions() {
            java.awt.Color expectedColor = new java.awt.Color(230, 250, 230);
            
            assertEquals(expectedColor, fileManager.getColorForExtension(".nfa"),
                ".nfa extension should have green color");
            assertEquals(expectedColor, fileManager.getColorForExtension(".pda"),
                ".pda extension should have green color");
        }
        
        @Test
        @DisplayName("DFA and TM extensions should have orange color")
        void testOrangeExtensions() {
            java.awt.Color expectedColor = new java.awt.Color(250, 240, 230);
            
            assertEquals(expectedColor, fileManager.getColorForExtension(".dfa"),
                ".dfa extension should have orange color");
            assertEquals(expectedColor, fileManager.getColorForExtension(".tm"),
                ".tm extension should have orange color");
        }
        
        @Test
        @DisplayName("CFG and REX extensions should have purple color")
        void testPurpleExtensions() {
            java.awt.Color expectedColor = new java.awt.Color(240, 230, 250);
            
            assertEquals(expectedColor, fileManager.getColorForExtension(".cfg"),
                ".cfg extension should have purple color");
            assertEquals(expectedColor, fileManager.getColorForExtension(".rex"),
                ".rex extension should have purple color");
        }
        
        @Test
        @DisplayName("Unknown extension should have light gray color")
        void testDefaultColor() {
            assertEquals(java.awt.Color.LIGHT_GRAY, fileManager.getColorForExtension(".unknown"),
                "Unknown extension should have light gray color");
        }
        
        @Test
        @DisplayName("Case insensitive color mapping should work")
        void testCaseInsensitive() {
            java.awt.Color expectedColor = new java.awt.Color(230, 250, 230);
            
            assertEquals(expectedColor, fileManager.getColorForExtension(".NFA"),
                "Uppercase .NFA should work");
            assertEquals(expectedColor, fileManager.getColorForExtension(".Nfa"),
                "Mixed case .Nfa should work");
        }
    }

    @Nested
    @DisplayName("File Creation Tests")
    class FileCreationTests {
        
        @Test
        @DisplayName("Creating NFA panel from .nfa file should succeed")
        void testCreateNFAPanel() throws IOException {
            // Create a temporary NFA file
            File nfaFile = tempDir.resolve("test.nfa").toFile();
            String nfaContent = "states: q0 q1\n" +
                               "alphabet: a b\n" +
                               "start: q0\n" +
                               "finals: q1\n" +
                               "transitions:\n" +
                               "q0 -> q1 (a)\n";
            
            Files.write(nfaFile.toPath(), nfaContent.getBytes());
            
            assertDoesNotThrow(() -> {
                javax.swing.JPanel panel = fileManager.createPanelForFile(nfaFile);
                assertNotNull(panel, "Created panel should not be null");
                assertTrue(panel instanceof NFAPanel, "Panel should be NFAPanel instance");
            }, "Creating NFA panel should not throw exception");
        }
        
        @Test
        @DisplayName("Creating DFA panel from .dfa file should succeed")
        void testCreateDFAPanel() throws IOException {
            // Create a temporary DFA file
            File dfaFile = tempDir.resolve("test.dfa").toFile();
            String dfaContent = "Start: q0\n" +
                               "Finals: q1\n" +
                               "Alphabet: a b\n" +
                               "States: q0 q1\n" +
                               "Transitions:\n" +
                               "q0 -> q1 (a)\n";
            
            Files.write(dfaFile.toPath(), dfaContent.getBytes());
            
            assertDoesNotThrow(() -> {
                javax.swing.JPanel panel = fileManager.createPanelForFile(dfaFile);
                assertNotNull(panel, "Created panel should not be null");
                assertTrue(panel instanceof DFAPanel, "Panel should be DFAPanel instance");
            }, "Creating DFA panel should not throw exception");
        }
        
        @Test
        @DisplayName("Creating panel from .cfg file should throw not implemented exception")
        void testCreateCFGPanelNotImplemented() throws IOException {
            // Create a temporary CFG file
            File cfgFile = tempDir.resolve("test.cfg").toFile();
            String cfgContent = "Variables: S A\n" +
                               "Terminals: a b\n" +
                               "Start: S\n" +
                               "Productions:\n" +
                               "S -> aA\n" +
                               "A -> b\n";
            
            Files.write(cfgFile.toPath(), cfgContent.getBytes());
            
            // Should throw IllegalArgumentException with "not yet implemented" message
            Exception exception = assertThrows(IllegalArgumentException.class, () -> {
                fileManager.createPanelForFile(cfgFile);
            });
            assertTrue(exception.getMessage().contains("not yet implemented"),
                "Exception message should indicate CFG is not yet implemented");
        }
        
        @Test
        @DisplayName("Creating panel from .rex file should throw not implemented exception")
        void testCreateREXPanelNotImplemented() throws IOException {
            // Create a temporary REX file
            File rexFile = tempDir.resolve("test.rex").toFile();
            String rexContent = "(a|b)*abb\n" +
                               "a, b\n";
            
            Files.write(rexFile.toPath(), rexContent.getBytes());
            
            // Should throw IllegalArgumentException with "not yet implemented" message
            Exception exception = assertThrows(IllegalArgumentException.class, () -> {
                fileManager.createPanelForFile(rexFile);
            });
            assertTrue(exception.getMessage().contains("not yet implemented"),
                "Exception message should indicate REX is not yet implemented");
        }
        
        @Test
        @DisplayName("Creating panel from unsupported file should throw exception")
        void testUnsupportedFileType() throws IOException {
            // Create a temporary file with unsupported extension
            File unsupportedFile = tempDir.resolve("test.xyz").toFile();
            Files.write(unsupportedFile.toPath(), "some content".getBytes());
            
            assertThrows(IllegalArgumentException.class, () -> {
                fileManager.createPanelForFile(unsupportedFile);
            }, "Creating panel from unsupported file should throw IllegalArgumentException");
        }
        
        @Test
        @DisplayName("Creating panel from non-existent file should throw exception")
        void testNonExistentFile() {
            File nonExistentFile = tempDir.resolve("nonexistent.nfa").toFile();
            
            assertThrows(Exception.class, () -> {
                fileManager.createPanelForFile(nonExistentFile);
            }, "Creating panel from non-existent file should throw exception");
        }
    }

    @Nested
    @DisplayName("Recent Files Management Tests")
    class RecentFilesTests {
        
        @Test
        @DisplayName("Adding file to recent files should work")
        void testAddToRecentFiles() throws IOException {
            File testFile = tempDir.resolve("test.nfa").toFile();
            Files.write(testFile.toPath(), "test content".getBytes());
            
            int initialSize = mockMainPanel.savedPageList.size();
            fileManager.addToRecentFiles(testFile);
            
            assertEquals(initialSize + 1, mockMainPanel.savedPageList.size(),
                "Recent files list should grow by 1");
            assertTrue(mockMainPanel.savedPageList.contains(testFile),
                "Recent files should contain the added file");
        }
        
        @Test
        @DisplayName("Adding duplicate file should not increase list size")
        void testAddDuplicateFile() throws IOException {
            File testFile = tempDir.resolve("test.nfa").toFile();
            Files.write(testFile.toPath(), "test content".getBytes());
            
            fileManager.addToRecentFiles(testFile);
            int sizeAfterFirst = mockMainPanel.savedPageList.size();
            
            fileManager.addToRecentFiles(testFile); // Add same file again
            int sizeAfterSecond = mockMainPanel.savedPageList.size();
            
            assertEquals(sizeAfterFirst, sizeAfterSecond,
                "Adding duplicate file should not increase list size");
        }
    }
}