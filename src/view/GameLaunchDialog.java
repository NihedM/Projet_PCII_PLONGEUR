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
        super(parent, "Menu Principal", ModalityType.APPLICATION_MODAL);
        setLayout(new BorderLayout());
        setSize(800, 600);
        setLocationRelativeTo(parent);

        JPanel titlePanel = new JPanel();
        titlePanel.setBackground(new Color(30, 144, 255)); // Couleur bleu DodgerBlue
        JLabel titleLabel = new JLabel("Bienvenue dans le Jeu !");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        titlePanel.add(titleLabel);
        add(titlePanel, BorderLayout.NORTH);

        // Panneau de paramétrage au centre
        JPanel paramPanel = new JPanel();
        paramPanel.setLayout(new GridBagLayout());
        paramPanel.setBackground(Color.LIGHT_GRAY);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        JLabel timeLabel = new JLabel("Temps de la partie (minutes) :");
        timeLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        paramPanel.add(timeLabel, gbc);

        timeSpinner = new JSpinner(new SpinnerNumberModel(5, 1, 60, 1));
        timeSpinner.setFont(new Font("Arial", Font.PLAIN, 16));
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        paramPanel.add(timeSpinner, gbc);

        pointsLabel = new JLabel("Points pour gagner : 100");
        pointsLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        paramPanel.add(pointsLabel, gbc);

        timeSpinner.addChangeListener(e -> updatePoints());

        add(paramPanel, BorderLayout.CENTER);

        // Panneau des boutons en bas
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(Color.DARK_GRAY);
        JButton launchButton = new JButton("Lancer le jeu");
        launchButton.setFont(new Font("Arial", Font.BOLD, 16));
        launchButton.setFocusPainted(false);
        JButton quitButton = new JButton("Quitter");
        quitButton.setFont(new Font("Arial", Font.BOLD, 16));
        quitButton.setFocusPainted(false);

        launchButton.addActionListener(e -> startGame());
        quitButton.addActionListener(e -> {
            int res = JOptionPane.showConfirmDialog(this, "Voulez-vous vraiment quitter le jeu ?", "Quitter", JOptionPane.YES_NO_OPTION);
            if (res == JOptionPane.YES_OPTION) {
                System.exit(0);
            }
        });

        buttonPanel.add(launchButton);
        buttonPanel.add(quitButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // Gestion de la fermeture de la fenêtre
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter(){
            @Override
            public void windowClosing(WindowEvent e) {
                quitButton.doClick();
            }
        });
    }

    private void updatePoints() {
        int minutes = (int) timeSpinner.getValue();
        int points = calculatePoints(minutes);
        pointsLabel.setText("Points pour gagner : " + points);
    }

    // TODO : Formule à modifier
    private int calculatePoints(int minutes) {
        return minutes * 20;
    }



    private void startGame() {
        int minutes = (int) timeSpinner.getValue();
        int points = calculatePoints(minutes);

        // Réinitialiser les compteurs de victoire et d'argent
        Referee.getInstance().ajouterPointsVictoire(-Referee.getInstance().getPointsVictoire());
        Referee.getInstance().ajouterArgent(-Referee.getInstance().getArgentJoueur());

        JOptionPane.showMessageDialog(this, "La partie va commencer !\nTemps : " + minutes
                + " min\nObjectif : " + points + " points.", "Démarrage", JOptionPane.INFORMATION_MESSAGE);

        // Création et démarrage du VictoryManager
        long gameDuration = minutes * 60 * 1000L;
        VictoryManager vm = new VictoryManager(GamePanel.getInstance(), gameDuration, points);
        GamePanel.getInstance().setVictoryManager(vm);
        vm.startGame();

        GamePanel.getInstance().setVisible(true);
        dispose();
    }
}
