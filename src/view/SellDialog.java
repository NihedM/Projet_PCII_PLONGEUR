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

        resourceListPanel = new JPanel(new GridLayout(1, 3, 10, 10));
        resourceListPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        updateResourceList();

        add(resourceListPanel, BorderLayout.CENTER);

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

        java.util.List<Ressource> ressources = GamePanel.getInstance().getCollectedResources();
        int total = ressources.size();

        if (total <= 3) {
            resourceListPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));
        } else {
            int columns = 3;
            int rows = (int) Math.ceil(total / (double) columns);
            resourceListPanel.setLayout(new GridLayout(rows, columns, 10, 10));
        }

        for (Ressource r : ressources) {
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
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setPreferredSize(new Dimension(120, 120));
            setBorder(BorderFactory.createLineBorder(Color.GRAY));
            setBackground(Color.WHITE);

            // Aperçu graphique de la ressource
            String imagePath = getImagePathFor(resource); // voir méthode ci-dessous
            ImageIcon icon = new ImageIcon(new ImageIcon(imagePath).getImage().getScaledInstance(64, 64, Image.SCALE_SMOOTH));
            JLabel iconLabel = new JLabel(icon);
            iconLabel.setAlignmentX(CENTER_ALIGNMENT);
            add(iconLabel);



            // Affichage du type et du prix de la ressource
            JLabel label = new JLabel("<html>" + resource.getClass().getSimpleName() +
                    "<br/>Prix : " + resource.getValeur() + "</html>");
            label.setAlignmentX(CENTER_ALIGNMENT);
            add(label);

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
        private String getImagePathFor(Ressource r) {
            String name = r.getClass().getSimpleName().toLowerCase();
            return "src/view/images/" + name + ".png";
        }

    }
}





