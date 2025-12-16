package UserInterface;

import common.Automaton;

/**
 * CFG (Context-Free Grammar) panel implementation.
 * Extends AbstractAutomatonPanel to eliminate code duplication.
 */
public class CFGPanel extends AbstractAutomatonPanel {

    public CFGPanel(MainPanel mainPanel, Automaton automaton) {
        super(mainPanel, automaton);
    }

    @Override
    protected String getTabLabelText() {
        return "CFG Tab";
    }
}