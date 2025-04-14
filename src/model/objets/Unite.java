package model.objets;

import model.unite_non_controlables.Enemy;
import view.ButtonAction;
import view.GamePanel;
import view.Redessine;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class Unite extends Objet {

    private DeplacementThread deplacementThread;
    private Position destination;

    private double vx, vy; // Vecteur vitesse
    private double vitesseCourante = 0.0; //vitesse courrante
    private double vitesseMax; //vitesse de l'unité
    private double acceleration = 0.1; //acceleration de l'unité
    private int heat_points;    //vie de l'unité
    private int MAX_HP;

    private Image currentImage;
    private Image movingImage;
    private ImageIcon unitIcon;



    public Unite(Position position, int rayon, double vitesse, int heat_points) {
        super(position, rayon);
        this.vitesseMax= vitesse;
        this.destination = null;
        this.deplacementThread =null;
        this.vx = 0;
        this.vy = 0;
        this.heat_points = heat_points;
        this.MAX_HP = heat_points;

        this.currentImage = getImage();
        setScalingFactor(2.0);
        scaleFactor = (targetDiameter / Math.max(originalImgWidth, originalImgHeight)) * getScalingFactor();
        this.imgWidth = (int) (originalImgWidth * scaleFactor);
        this.imgHeight = (int) (originalImgHeight * scaleFactor);
        this.halfWidth = imgWidth / 2;
        this.halfHeight = imgHeight / 2;

    }


    public synchronized Position getDestination() {
        return destination;
    }
    public int get_Hp() {return heat_points;}
    public void set_Hp(int hp) {
        if(hp > MAX_HP) return;
        this.heat_points = hp;
    }
    public void setMaxHp(int maxHp) {//faire attention
        this.MAX_HP = maxHp;
    }
    public synchronized void takeDamage(int damage) {
        this.heat_points -= damage;

        if (this.heat_points <= 0) {
            GamePanel.getInstance().killUnite(this);
        }
    }
    public boolean isAlive() {return this.heat_points > 0;}

    public double getVitesseCourante() {return vitesseCourante;}

    public void setVitesseCourante(double vitesse) {
        this.vitesseCourante = vitesse;
        this.currentImage = vitesse > 0 ? movingImage : image;

    }

    public double getVitesseMax() {return vitesseMax;}
    public void setVitesseMax(double vitesseMax) {this.vitesseMax = vitesseMax;}
    public double getAcceleration() {return acceleration;}
    public void setAcceleration(double acceleration) {this.acceleration = acceleration;}

    public double getVx() { return vx; }
    public double getVy() { return vy; }
    public void setVx(double vx) { this.vx = vx; }
    public void setVy(double vy) { this.vy = vy; }

    public synchronized void setDestination(Position destination) {

        synchronized (this) {

            if (this.destination != null && this.destination.equals(destination)) {
                return;
            }



            if (deplacementThread != null) {
                deplacementThread.stopThread();
            }



            if (destination != null) {
                this.destination = new Position(destination.getX(), destination.getY());

                if (deplacementThread != null)
                    deplacementThread.stopThread();


                deplacementThread = new DeplacementThread(this);
                deplacementThread.start();
            }
            else this.destination = null;
        }


    }

    public String getInfo() {
        return "HP: " + get_Hp() + ", Vitesse: " + getVitesseCourante();
    }

    public synchronized Color getColorForKey(String key) {return new Color(50, 150, 50);}// Default green color
    public synchronized int getMaxValueForKey(String key) {return MAX_HP;} //Max HP
    public ConcurrentHashMap<String, String> getAttributes() {
        ConcurrentHashMap<String, String> attributes = new ConcurrentHashMap<>();
        attributes.put("HP", String.valueOf(get_Hp()));
        return attributes;
    }


    public DeplacementThread getDeplacementThread() {return deplacementThread;}



    //-----------logique des actions--------------------------------

    public void stopAction() {
        setDestination(null);
        if (deplacementThread != null) {
            deplacementThread.stopThread();
        }
        setVitesseCourante(0);
        setAcceleration(0.1);
        setVx(0);
        setVy(0);
    }

    //----------------------------------------affichage--------------------------

    public void setMovingImage(String imgPath) {
        Image cachedImage = GamePanel.getCachedImage(imgPath);
        if (cachedImage != null) {
            this.movingImage = cachedImage;
        } else {
            System.err.println("Failed to load moving image: " + imgPath);
        }
    }
    @Override
    protected void setImage(String img) {
        super.setImage(img); // Call the parent class's setImage method
        this.currentImage = this.image; // Update currentImage to reflect the new image
    }
    public ImageIcon getUnitIcon() {
        return unitIcon;
    }

    public void setUnitIcon(ImageIcon unitIcon) {
        this.unitIcon = unitIcon;
    }

    @Override
    public void draw(Graphics2D g2d, Point screenPos) {
        if (image == null) {
            return; // No image to draw
        }

        if (vitesseCourante > 0 && deplacementThread != null && getDestination() != null) {
            //on calcule l'angle de rotation
            double angle = Math.atan2(vy, vx);
            AffineTransform originalTransform = g2d.getTransform();


            try {
                g2d.translate(screenPos.x, screenPos.y);
                g2d.rotate(angle + Math.PI / 2);

                // Flip l'image si on tourne à droite
                if (vx > 0) {
                    g2d.scale(-1, 1);
                }

                g2d.drawImage(currentImage,
                                -halfWidth,
                                -halfHeight,
                                imgWidth,
                                imgHeight, GamePanel.getInstance());

            } finally {
                g2d.setTransform(originalTransform);

            }
        } else {
            g2d.drawImage(currentImage, screenPos.x - halfWidth, screenPos.y - halfHeight, imgWidth, imgHeight, GamePanel.getInstance());
        }

    }


}

