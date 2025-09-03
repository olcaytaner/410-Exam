package UserInterface;

import common.Automaton;

public class REXPanel extends AbstractAutomatonPanel {
    public REXPanel(MainPanel mainPanel, Automaton automaton) {
        super(mainPanel, automaton);
    }

    @Override
    protected String getTabLabelText() {
        return "REX Tab";
    }
}
