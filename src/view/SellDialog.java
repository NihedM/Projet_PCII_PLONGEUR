package view;

import model.gains_joueur.Referee;
import model.objets.Ressource;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;


// Fenêtre de vente des ressources
class SellDialog extends JDialog {
    private JPanel resourceListPanel;
    // Pour mémoriser la ressource sélectionnée
    private ResourceItemPanel selectedResourcePanel = null;

    public SellDialog(JFrame parent) {
        super(parent, "Vendre Ressources", true);
        setLayout(new BorderLayout());
        setSize(400, 300);
        setLocationRelativeTo(parent);

        resourceListPanel = new JPanel();
        resourceListPanel.setLayout(new BoxLayout(resourceListPanel, BoxLayout.Y_AXIS));
        updateResourceList();

        JScrollPane scrollPane = new JScrollPane(resourceListPanel);
        add(scrollPane, BorderLayout.CENTER);

        JButton vendreButton = new JButton("Vendre");
        vendreButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (selectedResourcePanel != null) {
                    Ressource resource = selectedResourcePanel.getResource();
                    // Augmenter l'argent du joueur
                    Referee.getInstance().ajouterArgent(resource.getValeur());
                    // Retirer la ressource de l'inventaire (GamePanel)
                    GamePanel.getInstance().removeCollectedResource(resource);

                    //Augmneter le nb de points de victoire
                    model.gains_joueur.Referee.getInstance().ajouterPointsVictoire(10);

                    // Actualiser l'affichage
                    updateResourceList();
                    JOptionPane.showMessageDialog(SellDialog.this, "Ressource vendue pour " + resource.getValeur() + " €.");
                    selectedResourcePanel = null;
                } else {
                    JOptionPane.showMessageDialog(SellDialog.this, "Veuillez sélectionner une ressource.");
                }
            }
        });
        add(vendreButton, BorderLayout.SOUTH);
    }

    // Met à jour la liste affichée en fonction des ressources collectées
    private void updateResourceList() {
        resourceListPanel.removeAll();
        for (Ressource r : GamePanel.getInstance().getCollectedResources()) {
            ResourceItemPanel itemPanel = new ResourceItemPanel(r);
            resourceListPanel.add(itemPanel);
        }
        resourceListPanel.revalidate();
        resourceListPanel.repaint();
    }

    // Panneau représentant un item  dans la liste
    class ResourceItemPanel extends JPanel {
        private Ressource resource;
        public ResourceItemPanel(Ressource resource) {
            this.resource = resource;
            setLayout(new BorderLayout());
            setBorder(BorderFactory.createLineBorder(Color.GRAY));
            setPreferredSize(new Dimension(350, 60));

            // Aperçu graphique de la ressource
            JPanel previewPanel = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    int diameter = 40;
                    int x = (getWidth() - diameter) / 2;
                    int y = (getHeight() - diameter) / 2;
                    g.setColor(Color.ORANGE);
                    g.fillOval(x, y, diameter, diameter);
                }
            };
            previewPanel.setPreferredSize(new Dimension(60, 60));
            add(previewPanel, BorderLayout.WEST);

            // Affichage du type et du prix de la ressource
            JLabel label = new JLabel("<html>" + resource.getClass().getSimpleName() +
                    "<br/>Prix : " + resource.getValeur() + "</html>");
            add(label, BorderLayout.CENTER);

            // Sélection via clic
            addMouseListener(new MouseAdapter(){
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (selectedResourcePanel != null) {
                        selectedResourcePanel.setBackground(null);
                        selectedResourcePanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
                    }
                    selectedResourcePanel = ResourceItemPanel.this;
                    setBackground(Color.LIGHT_GRAY);
                    setBorder(BorderFactory.createLineBorder(Color.BLUE, 2));
                }
            });
        }

        public Ressource getResource() {
            return resource;
        }
    }
}






