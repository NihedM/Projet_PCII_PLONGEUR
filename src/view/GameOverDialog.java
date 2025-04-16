package view;

import model.gains_joueur.Referee;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class GameOverDialog extends JDialog {
    public GameOverDialog(Window parent, String message) {
        super(parent, "Fin de partie", ModalityType.APPLICATION_MODAL);

        // Afficher un fond opaque via le glass pane
        if (parent instanceof JFrame) {
            JFrame frame = (JFrame) parent;
            JPanel glassPane = (JPanel) frame.getGlassPane();
            glassPane.setOpaque(true);
            glassPane.setBackground(new Color(0, 0, 0, 255));
            glassPane.setVisible(true);
        }

        setLayout(new BorderLayout());
        setSize(700, 150);
        setLocationRelativeTo(parent);

        JLabel messageLabel = new JLabel(message, SwingConstants.CENTER);
        add(messageLabel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        JButton quitButton = new JButton("Quitter");
        buttonPanel.add(quitButton);
        add(buttonPanel, BorderLayout.SOUTH);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                resetGlassPane();
            }

            @Override
            public void windowClosing(WindowEvent e) {
                resetGlassPane();
            }

            private void resetGlassPane() {
                if (parent instanceof JFrame) {
                    ((JFrame) parent).getGlassPane().setVisible(false);
                }
            }
        });



        quitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (parent instanceof JFrame) {
                    ((JFrame) parent).getGlassPane().setVisible(false);
                }
                System.exit(0);
            }
        });
    }
}
