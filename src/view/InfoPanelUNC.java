package view;

import model.objets.Ressource;

import javax.swing.*;
import java.awt.*;

public class InfoPanelUNC extends JPanel {

    private JLabel infoLabel;
    private JProgressBar progressBar;

    public InfoPanelUNC(){
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(200, 150)); // Augmenter la hauteur pour accommoder la barre de progression
        setBackground(new Color(220, 220, 220));

        infoLabel = new JLabel("Informations :", SwingConstants.CENTER);
        infoLabel.setFont(new Font("Arial", Font.BOLD, 14));
        add(infoLabel, BorderLayout.NORTH);

        // Initialisation de la barre de progression
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true); // Afficher le pourcentage
        progressBar.setPreferredSize(new Dimension(180, 20));
        progressBar.setBackground(Color.WHITE);
        progressBar.setForeground(Color.YELLOW);

        // Ajouter la barre de progression au panel
        JPanel progressPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        progressPanel.add(progressBar);
        add(progressPanel, BorderLayout.CENTER);

        setVisible(false);
    }

    public void updateInfo(Ressource ressource) {
        // Mettre à jour les informations de la ressource
        infoLabel.setText("<html><center>" +
                "Type : " + ressource.getClass().getSimpleName() + "<br/>" +
                "Valeur : " + ressource.getValeur() + "<br/>" +
                "Etat : " + ressource.getEtat() + "<br/>" +
                "Temps restant : " + ressource.getTempsRestant() +
                "</center></html>");

        // Mettre à jour la barre de progression
        int tempsInitial = ressource.getTempsInitial();
        int tempsRestant = ressource.getTempsRestant();
        int progression = (int) ((tempsInitial - tempsRestant) / (double) tempsInitial * 100);

        progressBar.setValue(progression);

        // Changer la couleur de la barre en fonction de l'état de la ressource
        if (ressource.getEtat() == Ressource.Etat.EN_CROISSANCE) {
            progressBar.setForeground(Color.YELLOW);
        } else if (ressource.getEtat() == Ressource.Etat.PRET_A_RECOLTER) {
            progressBar.setForeground(Color.GREEN);
        } else {
            progressBar.setForeground(Color.RED);
        }
    }
}