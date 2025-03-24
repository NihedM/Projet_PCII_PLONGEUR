package controler;

import model.gains_joueur.Referee;
import view.GamePanel;

import javax.swing.*;
import java.util.Timer;
import java.util.TimerTask;

public class VictoryManager {
    private static final int VICTORY_POINTS = 10000;
    private static final int GAME_DURATION = 10 * 60 * 1000; // 10 minutes en millisecondes

    private final GamePanel gamePanel;
    private final Timer gameTimer;
    private long startTime;
    private boolean gameEnded = false;

    public VictoryManager(GamePanel gamePanel) {
        this.gamePanel = gamePanel;
        this.gameTimer = new Timer();
    }

    public void startGame() {
        this.startTime = System.currentTimeMillis();
        gameTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                checkGameStatus();
                gamePanel.repaint(); // Rafraîchir l'affichage du temps
            }
        }, 1000, 1000); // Vérifie chaque seconde
    }

    private void checkGameStatus() {
        if (gameEnded) return;

        // Vérifier victoire
        if (Referee.getInstance().getPointsVictoire() >= VICTORY_POINTS) {
            endGame(true);
            return;
        }

        // Vérifier défaite (temps écoulé)
        long elapsed = System.currentTimeMillis() - startTime;
        if (elapsed >= GAME_DURATION) {
            endGame(false);
        }
    }

    private void endGame(boolean victory) {
        gameEnded = true;
        gameTimer.cancel();

        SwingUtilities.invokeLater(() -> {
            String message = victory
                    ? "Félicitations ! Vous avez atteint " + VICTORY_POINTS + " points de victoire !"
                    : "Temps écoulé ! Vous n'avez pas atteint les " + VICTORY_POINTS + " points à temps.";

            JOptionPane.showMessageDialog(gamePanel, message, "Fin de partie", JOptionPane.INFORMATION_MESSAGE);
            System.exit(0); // Fermer le jeu
        });
    }

    public String getRemainingTime() {
        long elapsed = System.currentTimeMillis() - startTime;
        long remaining = Math.max(0, GAME_DURATION - elapsed);

        int minutes = (int) (remaining / (1000 * 60));
        int seconds = (int) ((remaining / 1000) % 60);

        return String.format("%02d:%02d", minutes, seconds);
    }
}