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

        // Image de fond
        ImageIcon backgroundIcon = new ImageIcon("src/view/images/Background.png");
        JLabel backgroundLabel = new JLabel(backgroundIcon);
        backgroundLabel.setLayout(new BorderLayout());
        setContentPane(backgroundLabel);

        // Titre
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        JLabel titleLabel = new JLabel("<html><div style='color:white;font-family:Segoe UI;font-size:32pt;text-shadow: 2px 2px 4px black;'>Bienvenue dans l'Océan Profond !</div></html>");
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titlePanel.add(titleLabel, BorderLayout.CENTER);
        backgroundLabel.add(titlePanel, BorderLayout.NORTH);

        // Paramètres
        JPanel paramPanel = new JPanel(new GridBagLayout());
        paramPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);

        JLabel timeLabel = new JLabel("<html><div style='color:white;font-family:Segoe UI;font-size:18pt;text-shadow: 1px 1px 2px black;'>Temps de la partie (minutes) :</div></html>");
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.EAST;
        paramPanel.add(timeLabel, gbc);

        timeSpinner = new JSpinner(new SpinnerNumberModel(5, 1, 60, 1));
        timeSpinner.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        paramPanel.add(timeSpinner, gbc);

        ((JSpinner.DefaultEditor) timeSpinner.getEditor()).getTextField().setInputVerifier(new InputVerifier() {
            @Override
            public boolean verify(JComponent input) {
                try {
                    Integer.parseInt(((JTextField) input).getText());
                    return true;
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(input, "Veuillez entrer uniquement des chiffres.", "Erreur de saisie", JOptionPane.WARNING_MESSAGE);
                    return false;
                }
            }
        });


        JLabel moneyLabel = new JLabel("<html><div style='color:white;font-family:Segoe UI;font-size:18pt;text-shadow: 1px 1px 2px black;'>Argent de départ (en €) :</div></html>");
        gbc.gridx = 0; gbc.gridy = 1; gbc.anchor = GridBagConstraints.EAST;
        paramPanel.add(moneyLabel, gbc);

        moneySpinner = new JSpinner(new SpinnerNumberModel(500, 0, 10000, 50));
        moneySpinner.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        paramPanel.add(moneySpinner, gbc);
        ((JSpinner.DefaultEditor) moneySpinner.getEditor()).getTextField().setInputVerifier(new InputVerifier() {
            @Override
            public boolean verify(JComponent input) {
                try {
                    Integer.parseInt(((JTextField) input).getText());
                    return true;
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(input, "Veuillez entrer uniquement des chiffres.", "Erreur de saisie", JOptionPane.WARNING_MESSAGE);
                    return false;
                }
            }
        });

        pointsLabel = new JLabel("<html><div style='color:white;font-family:Segoe UI;font-size:20pt;text-shadow: 1px 1px 2px black;'>Points pour gagner : 292</div></html>");
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER;
        paramPanel.add(pointsLabel, gbc);

        timeSpinner.addChangeListener(e -> updatePoints());
        moneySpinner.addChangeListener(e -> updatePoints());
        backgroundLabel.add(paramPanel, BorderLayout.CENTER);

        // Boutons glossy
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.setOpaque(false);

        JButton launchButton = new GlossyButton("Lancer le jeu");
        JButton quitButton = new GlossyButton("Quitter");

        launchButton.addActionListener(e -> startGame());
        quitButton.addActionListener(e -> {
            int res = JOptionPane.showConfirmDialog(this, "Voulez-vous vraiment quitter le jeu ?", "Quitter", JOptionPane.YES_NO_OPTION);
            if (res == JOptionPane.YES_OPTION) System.exit(0);
        });

        buttonPanel.add(launchButton);
        buttonPanel.add(quitButton);
        backgroundLabel.add(buttonPanel, BorderLayout.SOUTH);

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

    // === Classe GlossyButton ===
    private static class GlossyButton extends JButton {
        public GlossyButton(String text) {
            super(text);
            setFont(new Font("Segoe UI", Font.BOLD, 20));
            setForeground(Color.BLACK);
            setFocusPainted(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setOpaque(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();

            int width = getWidth();
            int height = getHeight();

            // Dégradé blond glossy
            GradientPaint gp = new GradientPaint(0, 0, new Color(255, 247, 153), 0, height, new Color(232, 204, 115));
            g2.setPaint(gp);
            g2.fillRoundRect(0, 0, width, height, 40, 40);

            // Reflet lumineux
            GradientPaint gloss = new GradientPaint(0, 0, new Color(255, 255, 255, 120), 0, height / 2, new Color(255, 255, 255, 0));
            g2.setPaint(gloss);
            g2.fillRoundRect(0, 0, width, height / 2, 40, 40);

            // Contour
            g2.setColor(new Color(170, 130, 0));
            g2.setStroke(new BasicStroke(2));
            g2.drawRoundRect(0, 0, width - 1, height - 1, 40, 40);

            super.paintComponent(g2);
            g2.dispose();
        }

        @Override
        public void paintBorder(Graphics g) {}
    }
}
