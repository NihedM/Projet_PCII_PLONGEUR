package view;

import java.awt.event.ActionListener;

public class ButtonAction {
    private String label;
    private ActionListener action;

    public ButtonAction(String label, ActionListener action) {
        this.label = label;
        this.action = action;
    }

    public String getLabel() {
        return label;
    }

    public ActionListener getAction() {
        return action;
    }
}
