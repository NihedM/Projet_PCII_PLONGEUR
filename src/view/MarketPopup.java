package view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;

import model.objets.Position;
import model.objets.Ressource;
import model.gains_joueur.Referee;
import model.unite_controlables.Plongeur;
import view.GamePanel;

public class MarketPopup extends JDialog {

    public MarketPopup(JFrame parent) {
        super(parent, "Marché", true);
        setLayout(new FlowLayout());
        setSize(300, 150);
        setLocationRelativeTo(parent);

        JButton vendreButton = new JButton("Vendre");
        JButton embaucherButton = new JButton("Embaucher");

        vendreButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Ouvrir la fenêtre de vente
                SellDialog sellDialog = new SellDialog(parent);
                sellDialog.setVisible(true);
            }
        });

        embaucherButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Au lieu d'afficher un simple message, on ouvre une nouvelle fenêtre d'embauche
                EmbaucheDialog embaucheDialog = new EmbaucheDialog(MarketPopup.this);
                embaucheDialog.setVisible(true);
                dispose(); // on ferme la popup du Market
            }
        });
        add(vendreButton);
        add(embaucherButton);
    }

}