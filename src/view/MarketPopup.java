package view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class MarketPopup extends JDialog {
    private final JFrame parentFrame;

    public MarketPopup(JFrame parent) {
        super(parent, "Marché", true);
        this.parentFrame = parent;
        setLayout(new FlowLayout());
        setSize(400, 200);
        setLocationRelativeTo(parent);

        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        // Gestionnaire pour toutes les fermetures
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                returnFocusToGamePanel();
            }

            @Override
            public void windowClosing(WindowEvent e) {
                returnFocusToGamePanel();
            }
        });

        // Création des boutons
        JButton vendreButton = createButton("Vendre", () -> showDialog(new SellDialog(parentFrame)));
        JButton embaucherButton = createButton("Embaucher", () -> showDialog(new EmbaucheDialog(this)));
        JButton boutiqueButton = createButton("Acheter", () -> showDialog(new BoutiqueDialog(parentFrame)));

        add(vendreButton);
        add(embaucherButton);
        add(boutiqueButton);
    }

    private JButton createButton(String text, Runnable action) {
        JButton button = new JButton(text);
        button.addActionListener(e -> {
            action.run();
            returnFocusToGamePanel(); // Focus immédiat après clic
        });
        return button;
    }

    private void showDialog(JDialog dialog) {
        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                returnFocusToGamePanel();
            }
        });
        dialog.setVisible(true);
    }

    private void returnFocusToGamePanel() {
        // Focus sur la frame parente
        parentFrame.requestFocusInWindow();

        // Focus sur le GamePanel après un léger délai
        SwingUtilities.invokeLater(() -> {
            Component gamePanel = findGamePanel(parentFrame);
            if (gamePanel != null) {
                gamePanel.requestFocusInWindow();
                //System.out.println("Focus restauré sur: " + gamePanel.getClass().getSimpleName());
            }
        });
    }

    private Component findGamePanel(Container parent) {
        for (Component comp : parent.getComponents()) {
            if (comp instanceof GamePanel) {
                return comp;
            }
            if (comp instanceof Container) {
                Component found = findGamePanel((Container) comp);
                if (found != null) return found;
            }
        }
        return null;
    }
}