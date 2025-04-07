package view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class MarketPopup extends JDialog {

    public MarketPopup(JFrame parent) {
        super(parent, "Marché", true);
        setLayout(new FlowLayout());
        setSize(400, 200);
        setLocationRelativeTo(parent);

        JButton vendreButton = new JButton("Vendre");
        JButton embaucherButton = new JButton("Embaucher");
        JButton boutiqueButton = new JButton("Acheter");


        vendreButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SellDialog sellDialog = new SellDialog(parent);
                sellDialog.setVisible(true);
            }
        });

        embaucherButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                EmbaucheDialog embaucheDialog = new EmbaucheDialog(MarketPopup.this);
                embaucheDialog.setVisible(true);
            }
        });
        boutiqueButton.addActionListener(e -> {
            BoutiqueDialog itemsDialog = new BoutiqueDialog(parent);
            itemsDialog.setVisible(true);
        });


        add(vendreButton);
        add(embaucherButton);
        add(boutiqueButton);

    }
}