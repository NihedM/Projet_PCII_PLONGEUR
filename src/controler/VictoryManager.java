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
        }, 0, 1000); // Vérifie chaque seconde
    }

    private void checkGameStatus() {
        if (gameEnded) return;

        long elapsed = System.currentTimeMillis() - startTime;
        long remaining = gameDuration - elapsed;

        // Convertir en secondes pour la Barre
        int remainingSeconds = (int)(remaining / 1000);
        int totalSeconds = (int)(gameDuration / 1000);

        SwingUtilities.invokeLater(() -> {
            gamePanel.getTimeProgressBar().updateProgress(remainingSeconds, totalSeconds);

            // La Barre s'occupe elle-même du formatage du texte grâce à setShowAsTime(true)
            // Et de la couleur grâce à updateColor()
        });

        if (elapsed >= gameDuration) {
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
           // GameOverDialog gameOverDialog = new GameOverDialog(SwingUtilities.getWindowAncestor(gamePanel));
           // gameOverDialog.setVisible(true);

          System.exit(0);



        });
    }

    public void triggerVictory() {
        endGame(true);
    }

}