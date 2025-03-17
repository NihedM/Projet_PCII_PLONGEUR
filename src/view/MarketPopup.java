package view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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
                //  ici la logique de vente
                JOptionPane.showMessageDialog(MarketPopup.this, "Vente effectuée");
                dispose();
            }
        });

        embaucherButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //  ici la logique d'embauche
                JOptionPane.showMessageDialog(MarketPopup.this, "Embauche effectuée");
                dispose();
            }
        });

        add(vendreButton);
        add(embaucherButton);
    }

    // TODO AJOUTER LE MARCHE PAR NIHED
}
