package view;

import model.objets.GestionRessource;
import model.objets.Ressource;

import javax.swing.*;
import java.awt.*;

public class InfoPanelUNC extends BackgroundPanel implements GestionRessource.RessourceListener {

    private JLabel infoLabel;
    private JProgressBar progressBar;

    public InfoPanelUNC(){
        setLayout(new BorderLayout());
        setBackgroundImage("ressourcesPanelBackground.png");


        infoLabel = new JLabel("Informations :", SwingConstants.CENTER);
        infoLabel.setFont(new Font("Arial", Font.BOLD, 14));
        infoLabel.setOpaque(false);
        add(infoLabel, BorderLayout.NORTH);

        progressBar = new JProgressBar(0, 100); // Initialisation de la barre de progression
        progressBar.setStringPainted(true);
        progressBar.setPreferredSize(new Dimension(180, 20));
        progressBar.setBackground(Color.WHITE);
        progressBar.setForeground(Color.YELLOW);

        // Ajouter la barre de progression au panel
        JPanel progressPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        progressPanel.setOpaque(false);
        progressPanel.add(progressBar);
        add(progressPanel, BorderLayout.CENTER);

        setVisible(false);
    }

    @Override
    public void onRessourceUpdated(Ressource ressource) {
        // Mettre à jour les informations de la ressource
        updateInfo(ressource);
    }

    public void updateInfo(Ressource ressource) {
        // Vérifier si la ressource est celle sélectionnée
        if (ressource.equals(GamePanel.getInstance().getRessourceSelectionnee())) {
            System.out.println("updateInfo appelée pour la ressource : " + ressource.getNom());
            infoLabel.setText("<html><center>" +
                    "Type : " + ressource.getNom() + "<br/>" +
                    "Valeur : " + ressource.getValeur() + "<br/>" +
                    "Etat : " + ressource.getEtat() + "<br/>" +
                    "Temps restant : " + ressource.getTempsRestant() +
                    "</center></html>");

            // Mettre à jour la barre de progression
            int tempsInitial = ressource.getTempsInitial();
            int tempsRestant = ressource.getTempsRestant();
            int progression = (int) ((tempsInitial - tempsRestant) / (double) tempsInitial * 100);

            progressBar.setValue(progression); // Mettre à jour la barre de progression

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
}