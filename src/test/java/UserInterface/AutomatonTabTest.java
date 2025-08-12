package UserInterface;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;

/**
 * JUnit 5 test class for AutomatonTab functionality.
 * Tests tab state management, unsaved changes tracking, and file association.
 */
@DisplayName("AutomatonTab Tests")
public class AutomatonTabTest {

    private AutomatonTab tab;
    private MockAutomatonPanel mockPanel;
    private File testFile;
    
    // Mock implementation of AutomatonPanel for testing
    private static class MockAutomatonPanel implements AutomatonPanel {
        private String content;
        private File currentFile;
        
        public MockAutomatonPanel(String initialContent) {
            this.content = initialContent;
        }
        
        public void setContent(String content) {
            this.content = content;
        }
        
        @Override
        public void runAutomaton() {}
        
        @Override
        public void compileAutomaton() {}
        
        @Override
        public void saveAutomaton() {}
        
        @Override
        public String getTextAreaContent() {
            return content;
        }
        
        @Override
        public common.Automaton getAutomaton() {
            return null;
        }
        
        @Override
        public File getCurrentFile() {
            return currentFile;
        }
        
        @Override
        public void setCurrentFile(File file) {
            this.currentFile = file;
        }
        
        @Override
        public void addTextChangeListener(Runnable onChange) {}
    }

    @BeforeEach
    void setUp() {
        // Create a mock AutomatonPanel
        mockPanel = new MockAutomatonPanel("initial content");
        
        testFile = new File("test.nfa");
        tab = new AutomatonTab("Test Tab", mockPanel, testFile);
    }

    @Nested
    @DisplayName("Tab Initialization Tests")
    class TabInitializationTests {
        
        @Test
        @DisplayName("Should initialize with correct title")
        void testInitializeTitle() {
            assertEquals("Test Tab", tab.getTitle());
        }
        
        @Test
        @DisplayName("Should initialize with panel")
        void testInitializePanel() {
            assertEquals(mockPanel, tab.getPanel());
        }
        
        @Test
        @DisplayName("Should initialize with file")
        void testInitializeFile() {
            assertEquals(testFile, tab.getFile());
        }
        
        @Test
        @DisplayName("Should initialize without unsaved changes")
        void testInitializeNoUnsavedChanges() {
            assertFalse(tab.hasUnsavedChanges());
        }
        
        @Test
        @DisplayName("Should store original content on initialization")
        void testInitializeOriginalContent() {
            assertEquals("initial content", tab.getOriginalContent());
        }
    }

    @Nested
    @DisplayName("Unsaved Changes Tests")
    class UnsavedChangesTests {
        
        @Test
        @DisplayName("Should detect unsaved changes when content differs")
        void testDetectUnsavedChanges() {
            // Change the mock to return different content
            mockPanel.setContent("modified content");
            
            tab.updateUnsavedStatus();
            
            assertTrue(tab.hasUnsavedChanges());
        }
        
        @Test
        @DisplayName("Should not detect unsaved changes when content is same")
        void testNoUnsavedChangesWhenSame() {
            // Keep the mock returning the same content
            mockPanel.setContent("initial content");
            
            tab.updateUnsavedStatus();
            
            assertFalse(tab.hasUnsavedChanges());
        }
        
        @Test
        @DisplayName("Should mark as saved and update original content")
        void testMarkAsSaved() {
            // First create unsaved changes
            mockPanel.setContent("modified content");
            tab.updateUnsavedStatus();
            assertTrue(tab.hasUnsavedChanges());
            
            // Mark as saved
            tab.markAsSaved();
            
            assertFalse(tab.hasUnsavedChanges());
            assertEquals("modified content", tab.getOriginalContent());
        }
        
        @Test
        @DisplayName("Should manually set unsaved changes flag")
        void testSetUnsavedChanges() {
            tab.setHasUnsavedChanges(true);
            assertTrue(tab.hasUnsavedChanges());
            
            tab.setHasUnsavedChanges(false);
            assertFalse(tab.hasUnsavedChanges());
        }
    }

    @Nested
    @DisplayName("Display Title Tests")
    class DisplayTitleTests {
        
        @Test
        @DisplayName("Should show asterisk when unsaved")
        void testDisplayTitleWithUnsaved() {
            tab.setHasUnsavedChanges(true);
            assertEquals("Test Tab *", tab.getDisplayTitle());
        }
        
        @Test
        @DisplayName("Should not show asterisk when saved")
        void testDisplayTitleWithoutUnsaved() {
            tab.setHasUnsavedChanges(false);
            assertEquals("Test Tab", tab.getDisplayTitle());
        }
        
        @Test
        @DisplayName("Should update display title after marking as saved")
        void testDisplayTitleAfterSave() {
            tab.setHasUnsavedChanges(true);
            assertEquals("Test Tab *", tab.getDisplayTitle());
            
            tab.markAsSaved();
            assertEquals("Test Tab", tab.getDisplayTitle());
        }
    }

    @Nested
    @DisplayName("File Association Tests")
    class FileAssociationTests {
        
        @Test
        @DisplayName("Should update title when file is set")
        void testSetFileUpdatesTitle() {
            File newFile = new File("newfile.dfa");
            tab.setFile(newFile);
            
            assertEquals(newFile, tab.getFile());
            assertEquals("newfile.dfa", tab.getTitle());
        }
        
        @Test
        @DisplayName("Should handle null file")
        void testSetNullFile() {
            tab.setFile(null);
            
            assertNull(tab.getFile());
            assertEquals("Test Tab", tab.getTitle()); // Title should remain unchanged
        }
        
        @Test
        @DisplayName("Should initialize without file")
        void testInitializeWithoutFile() {
            AutomatonTab tabWithoutFile = new AutomatonTab("No File Tab", mockPanel, null);
            
            assertNull(tabWithoutFile.getFile());
            assertEquals("No File Tab", tabWithoutFile.getTitle());
        }
    }

    @Nested
    @DisplayName("Content Management Tests")
    class ContentManagementTests {
        
        @Test
        @DisplayName("Should update original content")
        void testSetOriginalContent() {
            tab.setOriginalContent("new original content");
            assertEquals("new original content", tab.getOriginalContent());
        }
        
        @Test
        @DisplayName("Should track changes from original content")
        void testTrackChangesFromOriginal() {
            tab.setOriginalContent("original");
            mockPanel.setContent("original");
            tab.updateUnsavedStatus();
            assertFalse(tab.hasUnsavedChanges());
            
            mockPanel.setContent("modified");
            tab.updateUnsavedStatus();
            assertTrue(tab.hasUnsavedChanges());
        }
    }

    @Nested
    @DisplayName("Title Management Tests")
    class TitleManagementTests {
        
        @Test
        @DisplayName("Should update title manually")
        void testSetTitle() {
            tab.setTitle("New Title");
            assertEquals("New Title", tab.getTitle());
        }
        
        @Test
        @DisplayName("Should preserve manual title when no file")
        void testManualTitleWithNoFile() {
            AutomatonTab tabNoFile = new AutomatonTab("Manual Title", mockPanel, null);
            tabNoFile.setTitle("Updated Title");
            
            assertEquals("Updated Title", tabNoFile.getTitle());
            assertNull(tabNoFile.getFile());
        }
    }
}