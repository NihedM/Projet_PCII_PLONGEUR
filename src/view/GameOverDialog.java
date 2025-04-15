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
        setSize(300, 150);
        setLocationRelativeTo(parent);

        JLabel messageLabel = new JLabel(message, SwingConstants.CENTER);
        add(messageLabel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        JButton restartButton = new JButton("Rejouer");
        JButton quitButton = new JButton("Quitter");
        buttonPanel.add(restartButton);
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

        restartButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Masquer la vue de jeu
                GamePanel.getInstance().setVisible(false);
                dispose();
                if (parent instanceof JFrame) {
                    ((JFrame) parent).getGlassPane().setVisible(false);
                }
                // Réinitialiser les compteurs et l'état du jeu
                Referee.getInstance().reset();
                GamePanel.getInstance().reset();
                // (Optionnel) Réinitialiser GameMaster si nécessaire :
                // gameMaster.reset(); ou recréer une nouvelle instance

                // Ouvrir la fenêtre de paramétrage pour une nouvelle partie
                JFrame frame = (JFrame) parent;
                GameLaunchDialog launchDialog = new GameLaunchDialog(frame);
                launchDialog.setVisible(true);
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
