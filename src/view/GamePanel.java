package view;

import controler.ProximityChecker;
import controler.SelectionClic;
import controler.TileManager;
import controler.TileUpdater;
import model.gains_joueur.Referee;
import model.objets.CoordGrid;
import model.objets.UniteControlable;
import model.unite_controlables.Plongeur;
import view.debeug.GameInfoWindow;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GamePanel extends JPanel {
    public static final int PANELDIMENSION = 800;
    private static view.GamePanel instance;

    private int grid[][] = new int[TileManager.nbTiles][TileManager.nbTiles];
    private ConcurrentHashMap<CoordGrid, CopyOnWriteArrayList<model.objets.Objet>> objetsMap = new ConcurrentHashMap<>();
    private CopyOnWriteArrayList<model.objets.UniteControlable> unitesEnJeu = new CopyOnWriteArrayList<>();
    private CopyOnWriteArrayList<model.objets.UniteControlable> unitesSelected = new CopyOnWriteArrayList<>();
    private CopyOnWriteArrayList<model.objets.Ressource> ressources = new CopyOnWriteArrayList<>();

    private ProximityChecker proxy;
    private TileUpdater updater;

    // Les deux panels d'info regroupés dans un container avec CardLayout
    private JPanel infoContainer;
    private CardLayout infoCardLayout;

    private boolean paused = false;


    private InfoPanel infoPanel;         // pour les unités
    private InfoPanelUNC infoPanelUNC;   // pour les ressources
    private final int INFO_PANEL_TARGET_WIDTH = 200;
    // Largeur actuelle du panneau d'infos (pour l'animation)
    private int currentInfoPanelWidth = 0;
    private Timer slideTimer;
    private boolean isSliding = false;
    private boolean deplacementMode = false;

    private boolean recuperationMode = false;


    public GamePanel() {
        instance = this;
        setLayout(new BorderLayout());


        // Création du container des panneaux d'info avec CardLayout
        infoContainer = new JPanel(new CardLayout());
        infoPanel = new InfoPanel();
        infoPanelUNC = new InfoPanelUNC();
        infoContainer.setPreferredSize(new Dimension(0, PANELDIMENSION));
        JPanel emptyPanel = new JPanel();  // Panel vide
        emptyPanel.setOpaque(false);       // Pour qu'il ne gêne pas le visuel

// Ajout des cartes au container
        infoContainer.add(infoPanel, "unit");
        infoContainer.add(infoPanelUNC, "resource");
        infoContainer.add(emptyPanel, "empty");

// Afficher par défaut la carte vide
        CardLayout cl = (CardLayout) infoContainer.getLayout();
        cl.show(infoContainer, "empty");

        add(infoContainer, BorderLayout.EAST);



        setPreferredSize(new Dimension(PANELDIMENSION, PANELDIMENSION));
        setBackground(new Color(173, 216, 230)); // Fond bleu clair

        SelectionClic selectionClic = new SelectionClic(unitesEnJeu, unitesSelected, this);
        addMouseListener(selectionClic);

        this.updater = new TileUpdater(objetsMap);
        this.proxy = new ProximityChecker(objetsMap, unitesEnJeu);

        ExecutorService executor = Executors.newFixedThreadPool(2);
        executor.submit(updater);
        executor.submit(proxy);
        new GameInfoWindow(objetsMap, unitesEnJeu, unitesSelected);
    }

    public void setRecuperationMode(boolean mode) {
        this.recuperationMode = mode;
    }

    public boolean isRecuperationMode() {
        return recuperationMode;
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
        // Vous pouvez  interrompre ou suspendre certains threads ici si nécessaire
    }

    public boolean isPaused() {
        return paused;
    }

    // Méthode pour faire glisser le panneau d'infos vers l'intérieur (slide in)
    public void slideInInfoPanel(String panelType) {
        final int targetWidth = 200;       // largeur finale du panneau d'infos
        final int animationStep = 5;       // pixels ajoutés à chaque étape
        final int delay = 10;              // délai en millisecondes entre chaque étape

        // Afficher le panel concerné dans le CardLayout
        CardLayout cl = (CardLayout) infoContainer.getLayout();
        cl.show(infoContainer, panelType);

        Timer timer = new Timer(delay, null);
        timer.addActionListener(new ActionListener() {
            int currentWidth = infoContainer.getPreferredSize().width;
            @Override
            public void actionPerformed(ActionEvent e) {
                if (currentWidth < targetWidth) {
                    currentWidth += animationStep;
                    infoContainer.setPreferredSize(new Dimension(currentWidth, PANELDIMENSION));
                    infoContainer.revalidate();
                    repaint();
                } else {
                    timer.stop();
                }
            }
        });
        timer.start();
    }

    // Méthode pour faire glisser le panneau d'infos vers l'extérieur (slide out)
    public void slideOutInfoPanel() {
        final int animationStep = 5;
        final int delay = 10;

        Timer timer = new Timer(delay, null);
        timer.addActionListener(new ActionListener() {
            int currentWidth = infoContainer.getPreferredSize().width;
            @Override
            public void actionPerformed(ActionEvent e) {
                if (currentWidth > 0) {
                    currentWidth -= animationStep;
                    if (currentWidth < 0) {
                        currentWidth = 0;
                    }
                    infoContainer.setPreferredSize(new Dimension(currentWidth, PANELDIMENSION));
                    infoContainer.revalidate();
                    repaint();
                } else {
                    timer.stop();
                }
            }
        });
        timer.start();
    }
    public CopyOnWriteArrayList<model.objets.Ressource> getRessources() {
        return ressources;
    }

    public boolean isDeplacementMode() {
        return deplacementMode;
    }

    public void setDeplacementMode(boolean deplacementMode) {
        this.deplacementMode = deplacementMode;
    }

    public void showMiniPanel(Plongeur plongeur) {
        //  mettre à jour infoPanel avec les infos de l'unité ici (exemple : infoPanel.updateInfo(plongeur);)
        slideInInfoPanel("unit");
    }


    public void hideMiniPanel() {
        CardLayout cl = (CardLayout) infoContainer.getLayout();
        cl.show(infoContainer, "empty");
    }


    public void showResourceInfoPanel(model.objets.Ressource ressource) {
        slideInInfoPanel("resource");
        infoPanelUNC.updateInfo(ressource);
    }


    public void hideResourceInfoPanel() {
        infoPanelUNC.setVisible(false);
    }

    public CopyOnWriteArrayList<model.objets.Ressource> getRessourcesMap() {
        CopyOnWriteArrayList<model.objets.Ressource> ressourcesList = new CopyOnWriteArrayList<>();
        for (CopyOnWriteArrayList<model.objets.Objet> listeObjets : objetsMap.values()) {
            for (model.objets.Objet o : listeObjets) {
                if (o instanceof model.objets.Ressource) {
                    ressourcesList.add((model.objets.Ressource) o);
                }
            }
        }
        System.out.println("getRessourcesMap() retourne " + ressourcesList.size() + " ressources.");
        return ressourcesList;
    }

    public static view.GamePanel getInstance() {
        return instance;
    }

    // ... (les autres méthodes addObjet, removeObjet, paintComponent, etc.)

//-----------------Méthodes pour ajouter et supprimer des objets-----------------


    public synchronized void addObjet(model.objets.Objet objet) {

        /*is les coordonnées de l'objet existent deja dans la map, on ajoute l'objet à la liste d'objets à cette coordonnée
         * sinon on crée une nouvelle entrée dans la map avec la coordonnée de l'objet comme clé et une liste contenant l'objet comme valeur
         *
         */
        if(objetsMap.containsKey(objet.getCoordGrid())){
            objetsMap.get(objet.getCoordGrid()).add(objet);
            //System.out.println(objet.getClass().getSimpleName() + " ajouté à la coordonnée " + objet.getCoordGrid().getX() + " " + objet.getCoordGrid().getY());

        }else{
            CopyOnWriteArrayList<model.objets.Objet> objetsAtCoord = new CopyOnWriteArrayList<>();

            objetsAtCoord.add(objet);
            //objetsAtCoord.addAll(objetsMap.get(objet.getCoordGrid()));
            objetsMap.put(objet.getCoordGrid(), objetsAtCoord);


            //System.out.println(objet.getClass().getSimpleName() + " ajouté à la coordonnée " + objet.getCoordGrid().getX() + " " + objet.getCoordGrid().getY());
        }

        //System.out.println("Map size after addition: " + objetsMap.size());


        //System.out.println("Ajouté : " + objetsMap.get(coord).get(0).getClass().getName() + " à " + coord.getX() + " " + coord.getY());
    }




    public synchronized void removeObjet(model.objets.Objet objet, CoordGrid coord) {
        CopyOnWriteArrayList<model.objets.Objet> objetsAtCoord = objetsMap.get(coord);
        if (objetsAtCoord != null) {
            boolean removed = objetsAtCoord.remove(objet);
            if (removed && objetsAtCoord.isEmpty()) {
                objetsMap.remove(coord);
            }

        }
        //repaint();
    }

    public void addUniteControlable(model.objets.UniteControlable unite) {
        this.addObjet(unite);
        unitesEnJeu.add(unite);
        //repaint();
    }

    /*public void removeUnite(UniteControlable unite) {
        unitesEnJeu.remove(unite);
        removeObjet(unite);
    }*/


    public synchronized void addObjets(ArrayList<model.objets.Objet> objets) {
        for (model.objets.Objet objet : objets) {
            addObjet(objet);
        }
    }

    public void addUnitesControlles(CopyOnWriteArrayList<model.objets.UniteControlable> unites) {
        for (model.objets.UniteControlable unite : unites) {
            addUniteControlable(unite);
        }
    }







    //---------------------------------------------------------------------------------


    public CopyOnWriteArrayList<model.objets.UniteControlable> getUnitesEnJeu() {
        return unitesEnJeu;
    }
    /*public ArrayList<Objet> getObjets() {
        return objets;
    }*/


    public int[][] getGrid() {
        return grid;
    }

    public int[][] getVoisins(int x, int y){
        int[][] voisins = new int[8][2];
        int i = 0;
        for(int j = -1; j <= 1; j++){
            for(int k = -1; k <= 1; k++){
                if(j == 0 && k == 0) continue;
                voisins[i][0] = x + j;
                voisins[i][1] = y + k;
                i++;
            }
        }
        return voisins;
    }



    public static void printGridContents(ConcurrentHashMap<CoordGrid, CopyOnWriteArrayList<model.objets.Objet>> objetsMap) {
        for (ConcurrentHashMap.Entry<CoordGrid,CopyOnWriteArrayList<model.objets.Objet>> entry : objetsMap.entrySet()) {
            CoordGrid coord = entry.getKey();
            CopyOnWriteArrayList<model.objets.Objet> objetsDansTile = entry.getValue();

            System.out.println("Tile (" + coord.getX() + ", " + coord.getY() + "):");
            for (model.objets.Objet objet : objetsDansTile) {
                System.out.println("  - " + objet.getClass().getSimpleName() + " at (" + objet.getPosition().getX() + ", " + objet.getPosition().getY() + ")");
            }
        }
    }

    //----------------------------------------------------------------------






    /*peindre le tile ou ce trouve l'objet*/
    public void paintTile(int tileX, int tileY, Color color, Graphics g) {
        g.setColor(color);
        g.fillRect(tileX * PANELDIMENSION / TileManager.nbTiles, tileY * PANELDIMENSION / TileManager.nbTiles, TileManager.TILESIZE, TileManager.TILESIZE);
    }


    public void paintPerimetre(model.objets.Objet objet, Color color, Graphics g) {

        int tileX = TileManager.transformeP_to_grid(objet.getPosition().getX()) ;
        int tileY = TileManager.transformeP_to_grid(objet.getPosition().getY()) ;
        paintTile(tileX, tileY, color, g);

        int[][] voisins = getVoisins(tileX, tileY);
        for(int i = 0; i < 8; i++){
            if(voisins[i][0] < 0 || voisins[i][0] >= TileManager.nbTiles || voisins[i][1] < 0 || voisins[i][1] >= TileManager.nbTiles) continue;
            paintTile(voisins[i][0], voisins[i][1] , color, g);
        }

    }


    @Override
    protected synchronized void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Dessiner la grille
        for (int i = 0; i < TileManager.nbTiles; i++) {
            for (int j = 0; j < TileManager.nbTiles; j++) {
                g.setColor(Color.BLACK);
                g.drawRect(i * PANELDIMENSION / TileManager.nbTiles, j * PANELDIMENSION / TileManager.nbTiles,
                        TileManager.TILESIZE, TileManager.TILESIZE);
            }
        }

        // Dessiner les objets (unités et ressources)
        for (model.objets.Objet objet : objetsMap.values().stream().flatMap(CopyOnWriteArrayList::stream).toList()) {
            int diametre = objet.getRayon() * 2;

            if (objet instanceof model.objets.Ressource) {
                model.objets.Ressource ressource = (model.objets.Ressource) objet;
                if (ressource.getEtat() == model.objets.Ressource.Etat.EN_CROISSANCE) {
                    g.setColor(Color.YELLOW);
                } else if (ressource.getEtat() == model.objets.Ressource.Etat.PRET_A_RECOLTER) {
                    g.setColor(Color.GREEN);
                } else {
                    continue;
                }
            } else if (objet instanceof model.objets.UniteControlable) {
                model.objets.UniteControlable unite = (model.objets.UniteControlable) objet;
                // Afficher le cercle en rouge si l'unité est sélectionnée, sinon en noir
                if (unite.isSelected()) {
                    g.setColor(Color.RED);
                    // showMiniPanel((Plongeur) unite);
                } else {
                    g.setColor(Color.BLACK);
                    //hideMiniPanel();
                }
                // Tracer la ligne de déplacement si une destination est définie
                if (unite.getDestination() != null) {
                    g.setColor(Color.BLUE);
                    g.drawLine(unite.getPosition().getX(), unite.getPosition().getY(),
                            unite.getDestination().getX(), unite.getDestination().getY());
                }
            }


            // Dessiner l'objet (cercle)
            int x = objet.getPosition().getX() - objet.getRayon();
            int y = objet.getPosition().getY() - objet.getRayon();
            g.fillOval(x, y, diametre, diametre);
        }

        // Afficher les informations du joueur
        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.BOLD, 16));
        g.drawString("Points de victoire: " + Referee.getInstance().getPointsVictoire(), 10, 20);
        g.drawString("Argent: " + Referee.getInstance().getArgentJoueur(), 10, 40);
        g.drawString("Unités: " + unitesEnJeu.size(), 10, 60);

    }

    public void addUnite(model.objets.UniteControlable unite) {
        unitesEnJeu.add(unite);
    }

    public void removeUnite(UniteControlable unite) {
        unitesEnJeu.remove(unite);
    }

}

