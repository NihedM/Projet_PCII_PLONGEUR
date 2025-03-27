package view;

import controler.*;
import model.gains_joueur.Referee;
import model.objets.*;
import model.unite_controlables.Plongeur;
import view.debeug.GameInfoWindow;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GamePanel extends JPanel {
    public static final int PANELDIMENSION = 800;
    private static GamePanel instance;
    private final VictoryManager victoryManager;

    // Variables pour la caméra
    private int cameraX = 0;
    private int cameraY = 0;
    private boolean isDragging = false;
    private Point dragStart = new Point();

    // Dimensions du terrain
    public static final int TERRAIN_WIDTH = 1600;  // Largeur totale du terrain
    public static final int TERRAIN_HEIGHT = 2400; // Hauteur totale du terrain
    public static final int VIEWPORT_WIDTH = PANELDIMENSION - 200; // Largeur visible
    public static final int VIEWPORT_HEIGHT = PANELDIMENSION;     // Hauteur visible

    // Minimap
    private static final int MINIMAP_WIDTH = 150;
    private static final int MINIMAP_HEIGHT = 150;
    private static final int MINIMAP_MARGIN = 10;
    private static final float MINIMAP_SCALE = MINIMAP_WIDTH / (float)TERRAIN_WIDTH;

    private int grid[][] = new int[TileManager.nbTilesWidth][TileManager.nbTilesHeight];
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
    private Ressource ressourceSelectionnee;

    public static final Position BASE_POSITION = new Position(40, 100);

    // Dimensions du terrain
    public static final int TERRAIN_MIN_X = 0,
            TERRAIN_MAX_X = TERRAIN_WIDTH,
            TERRAIN_MIN_Y = 0,
            TERRAIN_MAX_Y = TERRAIN_HEIGHT,
            GAME_AREA_WIDTH = VIEWPORT_WIDTH;

    private SelectionClic selectionClic;
    private InfoPanel infoPanel;
    private InfoPanelUNC infoPanelUNC;
    private final int INFO_PANEL_TARGET_WIDTH = 200;
    private int currentInfoPanelWidth = 0;
    private Timer slideTimer;
    private boolean isSliding = false;
    private boolean deplacementMode = false;
    private boolean recuperationMode = false;

    public GamePanel() {
        instance = this;
        setLayout(null);
        setPreferredSize(new Dimension(PANELDIMENSION, PANELDIMENSION));
        setBackground(new Color(173, 216, 230));

        // Initialisation des composants UI
        initUIComponents();

        // Configuration des listeners
        setupListeners();

        // Initialisation des systèmes
        initSystems();

        // Initialisation de la gestion des victoires
        this.victoryManager = new VictoryManager(this);
    }

    private void drawMinimap(Graphics g) {
        // Fond de la minimap
        g.setColor(new Color(30, 30, 30, 200));
        g.fillRect(PANELDIMENSION - MINIMAP_WIDTH - MINIMAP_MARGIN,
                MINIMAP_MARGIN,
                MINIMAP_WIDTH,
                MINIMAP_HEIGHT);

        // Dessiner le terrain sur la minimap
        g.setColor(new Color(100, 100, 100));
        g.fillRect(PANELDIMENSION - MINIMAP_WIDTH - MINIMAP_MARGIN,
                MINIMAP_MARGIN,
                (int)(TERRAIN_WIDTH * MINIMAP_SCALE),
                (int)(TERRAIN_HEIGHT * MINIMAP_SCALE));

        // Dessiner les objets sur la minimap
        for (Objet objet : objetsMap.values().stream().flatMap(CopyOnWriteArrayList::stream).toList()) {
            int x = (int)(objet.getPosition().getX() * MINIMAP_SCALE);
            int y = (int)(objet.getPosition().getY() * MINIMAP_SCALE);
            x = PANELDIMENSION - MINIMAP_WIDTH - MINIMAP_MARGIN + x;
            y = MINIMAP_MARGIN + y;

            if (objet instanceof UniteControlable) {
                g.setColor(objet instanceof Plongeur ? Color.BLUE : Color.GREEN);
            } else if (objet instanceof Ressource) {
                g.setColor(Color.YELLOW);
            } else {
                g.setColor(Color.RED);
            }

            g.fillRect(x, y, 2, 2);
        }

        // Dessiner le rectangle de la vue actuelle
        g.setColor(Color.WHITE);
        int viewX = (int)(cameraX * MINIMAP_SCALE);
        int viewY = (int)(cameraY * MINIMAP_SCALE);
        int viewWidth = (int)(VIEWPORT_WIDTH * MINIMAP_SCALE);
        int viewHeight = (int)(VIEWPORT_HEIGHT * MINIMAP_SCALE);

        g.drawRect(PANELDIMENSION - MINIMAP_WIDTH - MINIMAP_MARGIN + viewX,
                MINIMAP_MARGIN + viewY,
                viewWidth,
                viewHeight);
    }

    private void initUIComponents() {
        infoContainer = new JPanel(new CardLayout());
        infoPanel = new InfoPanel();
        infoPanelUNC = new InfoPanelUNC();
        infoContainer.setPreferredSize(new Dimension(200, PANELDIMENSION));
        JPanel emptyPanel = new JPanel();
        emptyPanel.setOpaque(false);

        infoContainer.add(infoPanel, "unit");
        infoContainer.add(infoPanelUNC, "resource");
        infoContainer.add(emptyPanel, "empty");

        CardLayout cl = (CardLayout) infoContainer.getLayout();
        cl.show(infoContainer, "empty");

        infoContainer.setBounds(PANELDIMENSION - 200, 0, 200, PANELDIMENSION);
        add(infoContainer);

        JButton marketButton = new JButton("Market");
        marketButton.setBounds(500, 10, 100, 30);
        marketButton.addActionListener(e -> {
            setPaused(true);
            JFrame topFrame = (JFrame) SwingUtilities.getWindowAncestor(GamePanel.this);
            MarketPopup popup = new MarketPopup(topFrame);
            popup.setVisible(true);
            setPaused(false);
        });
        add(marketButton);
    }

    private void setupListeners() {
        selectionClic = new SelectionClic(this);
        addMouseListener(selectionClic);
        addMouseMotionListener(selectionClic);

        // Ajouter les listeners pour le déplacement de la caméra
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON3) {
                    isDragging = true;
                    dragStart = e.getPoint();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON3) {
                    isDragging = false;
                }
            }
        });

        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (isDragging) {
                    int dx = e.getX() - dragStart.x;
                    int dy = e.getY() - dragStart.y;

                    moveCamera(-dx, -dy);
                    dragStart = e.getPoint();
                }
            }
        });

        setFocusable(true);
        requestFocusInWindow();
    }

    private void initSystems() {
        this.updater = new TileUpdater(objetsMap);
        this.proxy = new ProximityChecker(objetsMap, unitesEnJeu);

        ExecutorService executor = Executors.newFixedThreadPool(2);
        executor.submit(updater);
        executor.submit(proxy);
        new GameInfoWindow(objetsMap, unitesEnJeu, unitesSelected);
    }

    // Méthodes pour la caméra
    public void moveCamera(int dx, int dy) {
        cameraX = Math.max(0, Math.min(TERRAIN_WIDTH - VIEWPORT_WIDTH, cameraX + dx));
        cameraY = Math.max(0, Math.min(TERRAIN_HEIGHT - VIEWPORT_HEIGHT, cameraY + dy));
        repaint();
    }

    private Point worldToScreen(int worldX, int worldY) {
        return new Point(worldX - cameraX, worldY - cameraY);
    }

    public int getCameraX() {
        return cameraX;
    }

    public int getCameraY() {
        return cameraY;
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
        g.fillRect(tileX * TileManager.TILESIZE - cameraX,
                tileY * TileManager.TILESIZE - cameraY,
                TileManager.TILESIZE, TileManager.TILESIZE);
    }


    public void paintPerimetre(model.objets.Objet objet, Color color, Graphics g) {
        int tileX = TileManager.transformeP_to_grid(objet.getPosition().getX());
        int tileY = TileManager.transformeP_to_grid(objet.getPosition().getY());
        paintTile(tileX, tileY, color, g);

        int[][] voisins = getVoisins(tileX, tileY);
        for(int i = 0; i < 8; i++){
            if(voisins[i][0] < 0 || voisins[i][0] >= TileManager.nbTilesWidth ||
                    voisins[i][1] < 0 || voisins[i][1] >= TileManager.nbTilesHeight) continue;
            paintTile(voisins[i][0], voisins[i][1], color, g);
        }
    }


    @Override
    protected synchronized void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Dessiner la partie visible du terrain
        for (int i = cameraX/TileManager.TILESIZE; i < (cameraX+VIEWPORT_WIDTH)/TileManager.TILESIZE + 1; i++) {
            for (int j = cameraY/TileManager.TILESIZE; j < (cameraY+VIEWPORT_HEIGHT)/TileManager.TILESIZE + 1; j++) {
                if (i >= 0 && i < TileManager.nbTilesWidth && j >= 0 && j < TileManager.nbTilesHeight) {
                    g.setColor(Color.BLACK);
                    Point screenPos = worldToScreen(i * TileManager.TILESIZE, j * TileManager.TILESIZE);
                    g.drawRect(screenPos.x, screenPos.y, TileManager.TILESIZE, TileManager.TILESIZE);
                }
            }
        }

        // Dessiner les bordures de la carte
        g.setColor(Color.RED);
        g.drawRect(-cameraX, -cameraY, TERRAIN_WIDTH, TERRAIN_HEIGHT);

        // Dessiner les objets visibles
        for (Objet objet : objetsMap.values().stream().flatMap(CopyOnWriteArrayList::stream).toList()) {
            Point screenPos = worldToScreen(objet.getPosition().getX(), objet.getPosition().getY());

            // Ne dessiner que si visible dans la vue
            if (screenPos.x + objet.getRayon()*2 >= 0 && screenPos.x - objet.getRayon() <= VIEWPORT_WIDTH &&
                    screenPos.y + objet.getRayon()*2 >= 0 && screenPos.y - objet.getRayon() <= VIEWPORT_HEIGHT) {

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
                    g.setColor(unite.isSelected() ? Color.RED : Color.BLACK);

                    if (unite.getDestination() != null) {
                        Point destScreenPos = worldToScreen(unite.getDestination().getX(), unite.getDestination().getY());
                        g.setColor(Color.BLUE);
                        g.drawLine(screenPos.x, screenPos.y, destScreenPos.x, destScreenPos.y);
                    }
                } else {
                    g.setColor(Color.PINK);
                }

                // Dessiner l'objet
                g.fillOval(screenPos.x - objet.getRayon(), screenPos.y - objet.getRayon(), diametre, diametre);

                // Dessiner le périmètre de fuite pour les Plongeurs
                if (objet instanceof Plongeur) {
                    Plongeur plongeur = (Plongeur) objet;
                    if (plongeur.isFaitFuire()) {
                        g.setColor(Color.ORANGE);
                        int rayonFuite = plongeur.getRayonFuite();
                        g.drawOval(screenPos.x - rayonFuite, screenPos.y - rayonFuite, rayonFuite * 2, rayonFuite * 2);
                    }
                }
            }
        }

        // Dessiner les spawn points
        g.setColor(Color.ORANGE);
        for (EnemySpawnPoint spawnPoint : SpawnManager.getInstance().getSpawnPoints()) {
            Point screenPos = worldToScreen(spawnPoint.getPosition().getX(), spawnPoint.getPosition().getY());
            int diameter = spawnPoint.getRayon() * 2;
            g.fillOval(screenPos.x - spawnPoint.getRayon(), screenPos.y - spawnPoint.getRayon(), diameter, diameter);
        }

        // Dessiner la base
        Point baseScreenPos = worldToScreen(BASE_POSITION.getX(), BASE_POSITION.getY());
        g.setColor(Color.GREEN);
        int baseSize = 20;
        g.fillRect(baseScreenPos.x - baseSize/2, baseScreenPos.y - baseSize/2, baseSize, baseSize);

        // Afficher les informations du joueur
        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.BOLD, 16));
        g.drawString("Points: " + Referee.getInstance().getPointsVictoire(), 10, 20);
        g.drawString("Argent: " + Referee.getInstance().getArgentJoueur(), 10, 40);
        g.drawString("Unités: " + unitesEnJeu.size(), 10, 60);

        if (victoryManager != null) {
            g.drawString("Temps: " + victoryManager.getRemainingTime(), 10, 80);
        }

        // Dessiner la sélection
        selectionClic.paintSelection(g);

        // Dessiner la minimap
        drawMinimap(g);
    }

    public static int getMinimapWidth() {
        return MINIMAP_WIDTH;
    }

    public static int getMinimapHeight() {
        return MINIMAP_HEIGHT;
    }

    public static int getMinimapMargin() {
        return MINIMAP_MARGIN;
    }

    public static float getMinimapScale() {
        return MINIMAP_SCALE;
    }

    public void checkAndClearResourcePanel(Ressource ressource) {
        if (ressourceSelectionnee != null && ressourceSelectionnee.equals(ressource)) {
            setRessourceSelectionnee(null);
            showEmptyInfoPanel();
        }
    }

}

