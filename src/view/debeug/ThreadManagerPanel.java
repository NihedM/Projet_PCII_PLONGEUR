package view.debeug;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

public class ThreadManagerPanel extends JPanel {
    private JTextArea textArea;

    public ThreadManagerPanel() {
        setLayout(new BorderLayout());
        textArea = new JTextArea();
        textArea.setEditable(false);
        add(new JScrollPane(textArea), BorderLayout.CENTER);
    }

    public synchronized void updateThreadCounts(Map<String, Integer> runningThreadCounts, int totalRunningThreadCount) {
        StringBuilder sb = new StringBuilder();
        sb.append("Current running thread counts:\n");
        for (Map.Entry<String, Integer> entry : runningThreadCounts.entrySet()) {
            sb.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }
        sb.append("Total running threads: ").append(totalRunningThreadCount).append("\n");
        SwingUtilities.invokeLater(() -> textArea.setText(sb.toString()));
    }
}
