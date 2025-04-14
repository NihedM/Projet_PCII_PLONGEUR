package model.objets;

import controler.TileManager;
import view.GamePanel;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Objet {
    private volatile Position position;
    public volatile CoordGrid coordGrid;

    private  int rayon;

    protected Image image;
    protected double scalingFactor = 1.0;

    protected  double targetDiameter;
    protected final int originalImgWidth = 1024;      //normalement toutes les images doivent faire 1024x1024
    protected final int originalImgHeight = 1024;
    protected double scaleFactor = (targetDiameter / Math.max(originalImgWidth, originalImgHeight)) * scalingFactor;

    protected int imgWidth;
    protected int imgHeight;
    protected int halfWidth ;
    protected int halfHeight ;
    protected boolean dimensionsUpdated = false;





    public Objet(Position position, int rayon) {
        this.targetDiameter = rayon * 2;
        this.position = position;
        this.rayon = rayon;
        this.coordGrid = TileManager.transformePos_to_Coord(position);

        setImage("unknown.png");
        updateDimensions();
        dimensionsUpdated = false;

    }


    public Position getPosition() {return position;}

    public CoordGrid getCoordGrid() {return coordGrid;}

    public int getRayon() {return rayon;}


    public double distance(model.objets.Objet other) {
        int dx = position.getX() - other.getPosition().getX();
        int dy = position.getY() - other.getPosition().getY();
        return Math.sqrt(dx * dx + dy * dy);
    }
    public double distance(Position other) {
        int dx = position.getX() - other.getX();
        int dy = position.getY() - other.getY();
        return Math.sqrt(dx * dx + dy * dy);
    }
    public synchronized void updatePosition() {
        coordGrid = TileManager.transformePos_to_Coord(position);
    }

    public Map<String, String> getAttributes() {
        Map<String, String> attributes = new HashMap<>();
        return attributes;
    }

    public Image getImage() {return image;}
    public double getScalingFactor() {return scalingFactor;}

    protected void setImage(String imgPath) {
        Image cachedImage = GamePanel.getCachedImage(imgPath);
        if (cachedImage != null) {
            this.image = cachedImage;
            dimensionsUpdated = false; // Mark dimensions for recalculation
        } else {
            System.err.println("Failed to load image: " + imgPath);
        }
    }
    public void setScalingFactor(double scalingFactor) {
        this.scalingFactor = scalingFactor;
        dimensionsUpdated = false;
    }

    public void draw(Graphics2D g2d, Point screenPos) {
        if (image == null) {
            return;
        }
        g2d.drawImage(image, screenPos.x - halfWidth, screenPos.y - halfHeight, imgWidth, imgHeight, GamePanel.getInstance());

    }

    protected void updateDimensions() {
        if (!dimensionsUpdated) {
            this.scaleFactor = (targetDiameter / Math.max(originalImgWidth, originalImgHeight)) * scalingFactor;
            this.imgWidth = (int) (originalImgWidth * scaleFactor);
            this.imgHeight = (int) (originalImgHeight * scaleFactor);
            this.halfWidth = imgWidth / 2;
            this.halfHeight = imgHeight / 2;
            dimensionsUpdated = true;
        }
    }
    public void setPosition(Position position) {
        this.position = position;
    }

    protected void setRayon(int r) {
        this.rayon = r;

    }
}

