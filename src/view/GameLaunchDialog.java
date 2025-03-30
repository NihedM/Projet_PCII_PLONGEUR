package view;

import model.gains_joueur.Referee;
import view.GamePanel;
import controler.VictoryManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class GameLaunchDialog extends JDialog {
    private JSpinner timeSpinner;
    private JLabel pointsLabel;

    public GameLaunchDialog(JFrame parent) {
        super(parent, "Paramétrer la partie", true);
        setLayout(new GridBagLayout());
        setSize(300, 180);
        setLocationRelativeTo(parent);

        if (parent instanceof JFrame) {
            JFrame frame = (JFrame) parent;
            JPanel glassPane = (JPanel) frame.getGlassPane();
            glassPane.setOpaque(true);
            glassPane.setBackground(new Color(0, 0, 0, 128)); // noir semi-transparent
            glassPane.setVisible(true);
        }

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter(){
            @Override
            public void windowClosing(WindowEvent e) {
                int res = JOptionPane.showConfirmDialog(
                        GameLaunchDialog.this,
                        "Voulez-vous quitter le jeu ?",
                        "Confirmation",
                        JOptionPane.YES_NO_OPTION
                );
                if (res == JOptionPane.YES_OPTION) {
                    hideGlassPane(parent);
                    System.exit(0); // Arrête entièrement le programme
                }
                // Sinon, ne rien faire
            }

            @Override
            public void windowClosed(WindowEvent e) {
                hideGlassPane(parent);
            }
        });

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        // Choix du temps de partie
        JLabel timeLabel = new JLabel("Temps de la partie (minutes) :");
        gbc.gridx = 0;
        gbc.gridy = 0;
        add(timeLabel, gbc);

        timeSpinner = new JSpinner(new SpinnerNumberModel(5, 1, 60, 1));
        gbc.gridx = 1;
        add(timeSpinner, gbc);

        // Affichage dynamique des points nécessaires
        pointsLabel = new JLabel("Points pour gagner : 100");
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        add(pointsLabel, gbc);

        // Mise à jour dynamique lors d'un changement dans le spinner
        timeSpinner.addChangeListener(e -> updatePoints());

        // Bouton de lancement de la partie
        JButton launchButton = new JButton("Lancer la partie");
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(launchButton, gbc);

        launchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startGame();
            }
        });
    }

    // Mise à jour du label en fonction du temps choisi
    private void updatePoints() {
        int minutes = (int) timeSpinner.getValue();
        int points = calculatePoints(minutes);
        pointsLabel.setText("Points pour gagner : " + points);
    }

    // Formule  points = minutes * 20
    private int calculatePoints(int minutes) {
        return minutes * 20;
    }

    // Méthode pour lancer la partie
    private void startGame() {
        int minutes = (int) timeSpinner.getValue();
        int points = calculatePoints(minutes);

        // Réinitialisation des compteurs
        Referee.getInstance().ajouterPointsVictoire(-Referee.getInstance().getPointsVictoire());
        Referee.getInstance().ajouterArgent(-Referee.getInstance().getArgentJoueur());

        JOptionPane.showMessageDialog(this, "La partie va commencer ! Temps : "
                + minutes + " min, Objectif : " + points + " points.");

        // Création du VictoryManager avec la durée et l'objectif définis
        long gameDuration = minutes * 60 * 1000L;
        VictoryManager vm = new VictoryManager(GamePanel.getInstance(), gameDuration, points);
        GamePanel.getInstance().setVictoryManager(vm);
        vm.startGame();

        // Réafficher le GamePanel (caché lors du GameOverDialog)
        GamePanel.getInstance().setVisible(true);

        dispose(); // Fermer la fenêtre de paramétrage
    }


    // Masquer le  glass pane
    private void hideGlassPane(Window parent) {
        if (parent instanceof JFrame) {
            JFrame frame = (JFrame) parent;
            frame.getGlassPane().setVisible(false);
        }
    }
}
