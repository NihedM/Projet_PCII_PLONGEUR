package controler;

import model.gains_joueur.Referee;
import view.GameOverDialog;
import view.GamePanel;

import javax.swing.*;
import java.util.Timer;
import java.util.TimerTask;

public class VictoryManager {

    private final int victoryPoints;  // Points à atteindre pour gagner
    private final long gameDuration;  // Durée de la partie en millisecondes

    private final GamePanel gamePanel;
    private final Timer gameTimer;
    private long startTime;
    private boolean gameEnded = false;

    public VictoryManager(GamePanel gamePanel, long gameDuration, int victoryPoints) {
        this.gamePanel = gamePanel;
        this.gameDuration = gameDuration;
        this.victoryPoints = victoryPoints;
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

        // Ne pas arrêter immédiatement si le nombre de points est atteint
        long elapsed = System.currentTimeMillis() - startTime;
        if (elapsed >= gameDuration) {
            // À la fin, on vérifie si l'objectif de points a été atteint
            boolean win = Referee.getInstance().getPointsVictoire() >= victoryPoints;
            endGame(win);
        }
    }



    private void endGame(boolean victory) {
        gameEnded = true;
        gameTimer.cancel();

        SwingUtilities.invokeLater(() -> {
            String message = victory
                    ? "Félicitations ! Vous avez atteint " + victoryPoints + " points de victoire !"
                    : "Temps écoulé ! Vous n'avez pas atteint les " + victoryPoints + " points à temps.";

            JOptionPane.showMessageDialog(gamePanel, message, "Fin de partie", JOptionPane.INFORMATION_MESSAGE);
            // Au lieu de fermer le jeu, on affiche le menu de fin de partie
            GameOverDialog gameOverDialog = new GameOverDialog(SwingUtilities.getWindowAncestor(gamePanel));
            gameOverDialog.setVisible(true);
        });
    }

    public String getRemainingTime() {
        long elapsed = System.currentTimeMillis() - startTime;
        long remaining = Math.max(0, gameDuration - elapsed);

        int minutes = (int) (remaining / (1000 * 60));
        int seconds = (int) ((remaining / 1000) % 60);

        return String.format("%02d:%02d", minutes, seconds);
    }
}