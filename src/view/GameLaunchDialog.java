package view;

import model.gains_joueur.Referee;
import view.GamePanel;
import controler.VictoryManager;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.*;

public class GameLaunchDialog extends JDialog {
    private JSpinner timeSpinner;
    private JSpinner moneySpinner;
    private JLabel pointsLabel;

    public GameLaunchDialog(JFrame parent) {
        super(parent, "Menu Principal", true);
        setSize(1200, 800);
        setLocationRelativeTo(parent);
        setUndecorated(false);

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        // Charger l'image de fond et l'utiliser dans un JLabel
        ImageIcon backgroundIcon = new ImageIcon("src/view/images/Background.png");
        JLabel backgroundLabel = new JLabel(backgroundIcon);
        backgroundLabel.setLayout(new BorderLayout());
        setContentPane(backgroundLabel);

        // Panneau de titre avec texte HTML pour effet d'ombre
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        JLabel titleLabel = new JLabel("<html><div style='color:white;font-family:Segoe UI;font-size:32pt;text-shadow: 2px 2px 4px black;'>Bienvenue dans l'Océan Profond !</div></html>");
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titlePanel.add(titleLabel, BorderLayout.CENTER);
        backgroundLabel.add(titlePanel, BorderLayout.NORTH);

        // Panneau de paramétrage (centre)
        JPanel paramPanel = new JPanel(new GridBagLayout());
        paramPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);

        // Temps de partie
        JLabel timeLabel = new JLabel("<html><div style='color:white;font-family:Segoe UI;font-size:18pt;text-shadow: 1px 1px 2px black;'>Temps de la partie (minutes) :</div></html>");
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        paramPanel.add(timeLabel, gbc);

        timeSpinner = new JSpinner(new SpinnerNumberModel(5, 1, 60, 1));
        timeSpinner.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        paramPanel.add(timeSpinner, gbc);

        // Argent de départ
        JLabel moneyLabel = new JLabel("<html><div style='color:white;font-family:Segoe UI;font-size:18pt;text-shadow: 1px 1px 2px black;'>Argent de départ (en €) :</div></html>");
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST;
        paramPanel.add(moneyLabel, gbc);

        moneySpinner = new JSpinner(new SpinnerNumberModel(500, 0, 10000, 50));
        moneySpinner.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        paramPanel.add(moneySpinner, gbc);

        // Points pour gagner
        pointsLabel = new JLabel("<html><div style='color:white;font-family:Segoe UI;font-size:20pt;text-shadow: 1px 1px 2px black;'>Points pour gagner : 292</div></html>");
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        paramPanel.add(pointsLabel, gbc);

        // Mises à jour dynamiques
        timeSpinner.addChangeListener(e -> updatePoints());
        moneySpinner.addChangeListener(e -> updatePoints());
        backgroundLabel.add(paramPanel, BorderLayout.CENTER);

        // Panneau des boutons (bas)
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.setOpaque(false);
        JButton launchButton = new JButton("Lancer le jeu");
        launchButton.setFont(new Font("Segoe UI", Font.BOLD, 20));
        launchButton.setFocusPainted(false);
        launchButton.setBackground(new Color(0, 128, 128));
        launchButton.setForeground(Color.WHITE);
        launchButton.setBorder(new LineBorder(Color.WHITE, 2));

        JButton quitButton = new JButton("Quitter");
        quitButton.setFont(new Font("Segoe UI", Font.BOLD, 20));
        quitButton.setFocusPainted(false);
        quitButton.setBackground(new Color(139, 0, 0));
        quitButton.setForeground(Color.WHITE);
        quitButton.setBorder(new LineBorder(Color.WHITE, 2));

        launchButton.addActionListener(e -> startGame());
        quitButton.addActionListener(e -> {
            int res = JOptionPane.showConfirmDialog(this, "Voulez-vous vraiment quitter le jeu ?", "Quitter", JOptionPane.YES_NO_OPTION);
            if (res == JOptionPane.YES_OPTION) {
                System.exit(0);
            }
        });
        buttonPanel.add(launchButton);
        buttonPanel.add(quitButton);
        backgroundLabel.add(buttonPanel, BorderLayout.SOUTH);

        // Gestion de fermeture
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                quitButton.doClick();
            }
        });
    }

    private void updatePoints() {
        int minutes = (int) timeSpinner.getValue();
        int initialMoney = (int) moneySpinner.getValue();
        int points = calculateTargetPoints(initialMoney, minutes);
        pointsLabel.setText("<html><div style='color:white;font-family:Segoe UI;font-size:20pt;text-shadow: 1px 1px 2px black;'>Points pour gagner : " + points + "</div></html>");
    }

    public int calculateTargetPoints(int initialMoney, int timeInMinutes) {
        double timeComponent = 25 * timeInMinutes;
        double moneyComponent = 20 * Math.pow(initialMoney, 0.3);
        double sousMarinBonus = 30 * (initialMoney / 400.0);
        double target = timeComponent + moneyComponent + sousMarinBonus;
        return (int) Math.round(target);
    }

    private void startGame() {
        int minutes = (int) timeSpinner.getValue();
        int initialMoney = (int) moneySpinner.getValue();
        int points = calculateTargetPoints(initialMoney, minutes);

        Referee.getInstance().ajouterPointsVictoire(-Referee.getInstance().getPointsVictoire());
        Referee.getInstance().ajouterArgent(-Referee.getInstance().getArgentJoueur());
        Referee.getInstance().ajouterArgent(initialMoney);

        JOptionPane.showMessageDialog(this, "La partie va commencer !\nTemps : " + minutes
                        + " min\nArgent de départ : " + initialMoney + " €\nObjectif : " + points + " points.",
                "Démarrage", JOptionPane.INFORMATION_MESSAGE);

        long gameDuration = minutes * 60 * 1000L;
        VictoryManager vm = new VictoryManager(GamePanel.getInstance(), gameDuration, points);
        GamePanel.getInstance().setVictoryManager(vm);
        vm.startGame();
        GamePanel.getInstance().setVisible(true);
        dispose();
    }
}
