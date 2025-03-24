package view;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

public class AtributInfo extends JPanel {

    public AtributInfo() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(Color.GREEN);
    }

    public void updateInfo(Map<String, String> attributes) {
        removeAll();
        for (Map.Entry<String, String> entry : attributes.entrySet()) {
            JLabel label = new JLabel(entry.getKey() + ": " + entry.getValue());
            add(label);
        }
        revalidate();
        repaint();
    }
}
