package view;


import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public  class ItemPanel extends JPanel {
    private String itemName;
    private String itemCost;
    private boolean unlocked;
    private boolean selected = false;

    private Image backgroundImage; // Image de fond

    public ItemPanel(String itemName, String itemCost, boolean unlocked, String imagePath) {
        this.itemName = itemName;
        this.itemCost = itemCost;
        this.unlocked = unlocked;

        setPreferredSize(new Dimension(120, 120));
        setLayout(null);

        if (imagePath != null && !imagePath.isEmpty()) {
            try {
                backgroundImage = ImageIO.read(new File(imagePath));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        if (!unlocked) {
            setToolTipText("Débloqué à la profondeur 3");
        }

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (unlocked) {
                    selected = !selected;
                    repaint();
                } else {
                    JOptionPane.showMessageDialog(ItemPanel.this, "Cet item est verrouillé !");
                }
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        } else {
            g.setColor(Color.LIGHT_GRAY);
            g.fillRect(0, 0, getWidth(), getHeight());
        }

        if (!unlocked) {
            g.setColor(new Color(0, 0, 0, 100));
            g.fillRect(0, 0, getWidth(), getHeight());
        }




        // Dessiner le nom de l’item
        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.BOLD, 14));
        FontMetrics fm = g.getFontMetrics();
        int nameWidth = fm.stringWidth(itemName);
        g.drawString(itemName, (getWidth() - nameWidth) / 2, 20);

        // Dessiner le coût
        g.setFont(new Font("Arial", Font.PLAIN, 12));
        String costText = "(" + itemCost + ")";
        int costWidth = g.getFontMetrics().stringWidth(costText);
        g.drawString(costText, (getWidth() - costWidth) / 2, getHeight() - 10);
    }
    public boolean isUnlocked() {
        return unlocked;
    }
}
