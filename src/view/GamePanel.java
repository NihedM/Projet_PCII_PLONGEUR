package view;

import controler.*;
import model.gains_joueur.Referee;
import model.objets.*;
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
import java.util.concurrent.locks.ReentrantLock;

public class GamePanel extends JPanel {
    public static final int PANELDIMENSION = 800;
    private static GamePanel instance;
    private final VictoryManager victoryManager;

    private int grid[][] = new int[TileManager.nbTiles][TileManager.nbTiles];
    private ConcurrentHashMap<CoordGrid, CopyOnWriteArrayList<Objet>> objetsMap = new ConcurrentHashMap<>();
    private CopyOnWriteArrayList<UniteControlable> unitesEnJeu = new CopyOnWriteArrayList<>();
    private CopyOnWriteArrayList<UniteControlable> unitesSelected = new CopyOnWriteArrayList<>();

    private ProximityChecker proxy;
    private TileUpdater updater;

    // Les deux panels d'info regroupés dans un container avec CardLayout
    private JPanel infoContainer;
    private CardLayout infoCardLayout;

    private boolean paused = false;

    private ArrayList<model.objets.Ressource> collectedResources = new ArrayList<>();

    private Ressource ressourceSelectionnee; // Ressource actuellement sélectionnée





    //ajouter classe terrain, dimentions temporaires
    public static final int TERRAIN_MIN_X = 10,
            TERRAIN_MAX_X = PANELDIMENSION - 200-10,
            TERRAIN_MIN_Y = 10,
            TERRAIN_MAX_Y = PANELDIMENSION -10,
            GAME_AREA_WIDTH = PANELDIMENSION - 205;


    private SelectionClic selectionClic;


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
        setLayout(null);

        setPreferredSize(new Dimension(PANELDIMENSION, PANELDIMENSION));
        setBackground(new Color(173, 216, 230)); // Fond bleu clair

        // Initialisation et positionnement de l'infoContainer (anciennement ajouté en EAST)
        infoContainer = new JPanel(new CardLayout());
        infoPanel = new InfoPanel();
        infoPanelUNC = new InfoPanelUNC();
        infoContainer.setPreferredSize(new Dimension(200, PANELDIMENSION));
        JPanel emptyPanel = new JPanel();
        emptyPanel.setOpaque(false);


        infoContainer.add(infoPanel, "unit");
        infoContainer.add(infoPanelUNC, "resource");
        infoContainer.add(emptyPanel, "empty");


        // Par défaut, affiche la carte vide
        CardLayout cl = (CardLayout) infoContainer.getLayout();
        cl.show(infoContainer, "empty");


        // Positionnement manuel : on place l'infoContainer à droite
        infoContainer.setBounds(PANELDIMENSION - 200, 0, 200, PANELDIMENSION);
        add(infoContainer);


        // Ajout direct du bouton "Market" dans le JPanel
        JButton marketButton = new JButton("Market");
        // Positionné en haut à droite (100px de largeur, 30px de hauteur, ajuster selon vos besoins)
        marketButton.setBounds(500, 10, 100, 30);
        marketButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Mettre le jeu en pause
                GamePanel.this.setPaused(true);
                // Récupérer la fenêtre parente du GamePanel
                JFrame topFrame = (JFrame) SwingUtilities.getWindowAncestor(GamePanel.this);
                // Créer et afficher la popup Market
                MarketPopup popup = new MarketPopup(topFrame);
                popup.setVisible(true);
                // Une fois la popup fermée, reprendre le jeu
                GamePanel.this.setPaused(false);
            }
        });
        add(marketButton);






        setPreferredSize(new Dimension(PANELDIMENSION, PANELDIMENSION));
        setBackground(new Color(173, 216, 230)); // Fond bleu clair

        selectionClic = new SelectionClic(this);
        addMouseListener(selectionClic);
        addMouseMotionListener(selectionClic);


        this.updater = new TileUpdater(objetsMap);
        this.proxy = new ProximityChecker(objetsMap, unitesEnJeu);

        ExecutorService executor = Executors.newFixedThreadPool(2);
        executor.submit(updater);
        executor.submit(proxy);
        new GameInfoWindow(objetsMap, unitesEnJeu, unitesSelected);

        // Initialisation du KeyboardController
        setFocusable(true);
        requestFocusInWindow();

        // Initialisation de la gestion des victoires
        this.victoryManager = new VictoryManager(this);
    }

    public void startGame() {
        victoryManager.startGame();
    }


    //------------------GETTERS------------------------------------------------------------------------------------------------------
    public static GamePanel getInstance() {
        return instance;
    }


    public synchronized ConcurrentHashMap<CoordGrid, CopyOnWriteArrayList<model.objets.Objet>> getObjetsMap() {
        return objetsMap;
    }
    public synchronized Ressource getRessourceSelectionnee() {
        return ressourceSelectionnee;
    }

    public InfoPanelUNC getInfoPanelUNC() {
        return infoPanelUNC; // Méthode pour accéder à InfoPanelUNC
    }

    public InfoPanel getInfoPanel() {
        return infoPanel; // Méthode pour accéder à InfoPanel
    }

    public boolean isRecuperationMode() {
        return recuperationMode;
    }
    public boolean isDeplacementMode() {
        return deplacementMode;
    }
    public ArrayList<model.objets.Ressource> getCollectedResources() {
        return collectedResources;
    }

    public boolean isPaused() {
        return paused;
    }

    public boolean isWithinTerrainBounds(Position position) {
        int x = position.getX();
        int y = position.getY();
        return x >= TERRAIN_MIN_X && x <= TERRAIN_MAX_X && y >= TERRAIN_MIN_Y && y <= TERRAIN_MAX_Y;
    }

    public CopyOnWriteArrayList<model.objets.UniteControlable> getUnitesEnJeu() {
        return unitesEnJeu;
    }
    /*public ArrayList<Objet> getObjets() {
        return objets;
    }*/

    public CopyOnWriteArrayList<UniteControlable> getUnitesSelected() {
        return unitesSelected;
    }


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
    public CopyOnWriteArrayList<Ressource> getRessourcesMap() {
        CopyOnWriteArrayList<Ressource> ressourcesList = new CopyOnWriteArrayList<>();
        for (CopyOnWriteArrayList<model.objets.Objet> listeObjets : objetsMap.values()) {
            for (model.objets.Objet o : listeObjets) {
                if (o instanceof Ressource) {
                    ressourcesList.add((Ressource) o);
                }
            }
        }
        //System.out.println("getRessourcesMap() retourne " + ressourcesList.size() + " ressources.");
        return ressourcesList;
    }
    public ArrayList<Ressource> getRessources() {
        ArrayList<Ressource> ressources = new ArrayList<>();
        for (CopyOnWriteArrayList<Objet> objets : objetsMap.values()) {
            for (Objet objet : objets) {
                if (objet instanceof Ressource) {
                    ressources.add((Ressource) objet);
                }
            }
        }
        return ressources;
    }


    //-----------------SETTERS--------------------------------------------------------------------------------------------------------
    public synchronized void setRessourceSelectionnee(Ressource ressource) {
        this.ressourceSelectionnee = ressource;
    }
    public void setRecuperationMode(boolean mode) {
        this.recuperationMode = mode;
    }
    public void setPaused(boolean paused) {
        this.paused = paused;
        // Vous pouvez  interrompre ou suspendre certains threads ici si nécessaire
    }
    public void setDeplacementMode(boolean deplacementMode) {
        this.deplacementMode = deplacementMode;
    }




    //-----------------AJOUTS------------------------------------------------------------------------------------------------------



    //methode pour ajouter un objet sur le jeu
    public synchronized void addObjet(Objet objet) {

        /*is les coordonnées de l'objet existent deja dans la map, on ajoute l'objet à la liste d'objets à cette coordonnée
         * sinon on crée une nouvelle entrée dans la map avec la coordonnée de l'objet comme clé et une liste contenant l'objet comme valeur
         *
         */
        if(objetsMap.containsKey(objet.getCoordGrid())){
            objetsMap.get(objet.getCoordGrid()).add(objet);

        }else{
            CopyOnWriteArrayList<Objet> objetsAtCoord = new CopyOnWriteArrayList<>();
            objetsAtCoord.add(objet);
            objetsMap.put(objet.getCoordGrid(), objetsAtCoord);
        }

        if (objet instanceof Ressource){
            Ressource ressource = (Ressource) objet;
            GestionRessource gestionRessource = new GestionRessource(ressource, 1000); // Intervalle de 1 seconde
            gestionRessource.addListener(infoPanelUNC); // ajouter InfoPanelUNC comme listener
            gestionRessource.start(); // Démarrer le thread

        }
    }

    public void addUniteControlable(UniteControlable unite) {
        this.addObjet(unite);
        unitesEnJeu.add(unite);
    }

    public void addCollectedResource(model.objets.Ressource r) {
        collectedResources.add(r);
    }


    //-----------------SUPPRESSIONS------------------------------------------------------------------------------------------------------

    public synchronized void removeObjet(model.objets.Objet objet, CoordGrid coord) {
        CopyOnWriteArrayList<model.objets.Objet> objetsAtCoord = objetsMap.get(coord);
        if (objetsAtCoord != null) {
            boolean removed = objetsAtCoord.remove(objet);
            if (removed && objetsAtCoord.isEmpty()) {
                objetsMap.remove(coord);
            }

        }
    }
    public void removeCollectedResource(model.objets.Ressource r) {
        collectedResources.remove(r);
    }


    /*public void removeUnite(UniteControlable unite) {
        unitesEnJeu.remove(unite);
        removeObjet(unite);
    }*/





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


    //-----------------AFFICHAGE------------------------------------------------------------------------------------------------------
    public void showMiniPanel(Plongeur plongeur) {
        //  mettre à jour infoPanel avec les infos de l'unité ici (exemple : infoPanel.updateInfo(plongeur);)
        slideInInfoPanel("unit");
    }
    public void showEmptyInfoPanel() {
        CardLayout cl = (CardLayout) infoContainer.getLayout();
        cl.show(infoContainer, "empty");
        // Définir une largeur fixe pour le panneau d'info (par exemple 200 pixels)
        infoContainer.setPreferredSize(new Dimension(200, PANELDIMENSION));
        infoContainer.revalidate();
        repaint();
    }
    public void showFixedInfoPanel(String panelType) {
        CardLayout cl = (CardLayout) infoContainer.getLayout();
        cl.show(infoContainer, panelType);
        // Définir la largeur fixe souhaitée (par exemple 200 pixels)
        infoContainer.setPreferredSize(new Dimension(200, PANELDIMENSION));
        infoContainer.revalidate();
        repaint();
    }
    public void hideMiniPanel() {
        CardLayout cl = (CardLayout) infoContainer.getLayout();
        cl.show(infoContainer, "empty");
    }


    public void showResourceInfoPanel(Ressource ressource) {
        System.out.println("Affichage des informations pour la ressource : " + ressource.getNom());
        setRessourceSelectionnee(ressource); // Mettre à jour la ressource sélectionnée
        slideInInfoPanel("resource");
        infoPanelUNC.updateInfo(ressource); // Mettre à jour les informations de la ressource
    }


    public void hideResourceInfoPanel() {
        infoPanelUNC.setVisible(false);
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
            else{
                g.setColor(Color.PINK);
            }


            // Dessiner l'objet (cercle)
            int x = objet.getPosition().getX() - objet.getRayon();
            int y = objet.getPosition().getY() - objet.getRayon();
            g.fillOval(x, y, diametre, diametre);

            // Dessiner le périmètre de fuite pour les Plongeurs
            if (objet instanceof Plongeur) {
                Plongeur plongeur = (Plongeur) objet;
                if (plongeur.isFaitFuire()) {
                    g.setColor(Color.ORANGE);
                    int rayonFuite = plongeur.getRayonFuite();
                    int xFuite = plongeur.getPosition().getX() - rayonFuite;
                    int yFuite = plongeur.getPosition().getY() - rayonFuite;
                    int diametreFuite = rayonFuite * 2;
                    g.drawOval(xFuite, yFuite, diametreFuite, diametreFuite);
                }
            }

        }
        g.setColor(Color.ORANGE); // Set the color for spawn points
        for (EnemySpawnPoint spawnPoint : SpawnManager.getInstance().getSpawnPoints()) {
            Position pos = spawnPoint.getPosition();
            int x2 = pos.getX() - spawnPoint.getRayon();
            int y2 = pos.getY() - spawnPoint.getRayon();
            int diameter = spawnPoint.getRayon() * 2;
            g.fillOval(x2, y2, diameter, diameter);
        }

        // pour tester la gestion de proximité
        for(UniteControlable unite: unitesEnJeu){
            CopyOnWriteArrayList<Objet> voisins = proxy.getVoisins(unite);

            for (Objet voisin : voisins) {
                int x1 = unite.getPosition().getX();
                int y1 = unite.getPosition().getY();
                int x2 = voisin.getPosition().getX();
                int y2 = voisin.getPosition().getY();

                g.setColor(Color.RED);  // Choisir la couleur de la ligne
                g.drawLine(x1, y1, x2, y2);
            }
        }



        selectionClic.paintSelection(g);


        // Afficher les informations du joueur
        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.BOLD, 16));
        g.drawString("Points de victoire: " + Referee.getInstance().getPointsVictoire(), 10, 20);
        g.drawString("Argent: " + Referee.getInstance().getArgentJoueur(), 10, 40);
        g.drawString("Unités: " + unitesEnJeu.size(), 10, 60);

        // Ajout du temps restant
        if (victoryManager != null) {
            g.drawString("Temps restant: " + victoryManager.getRemainingTime(), 10, 80);
        }

    }

}

