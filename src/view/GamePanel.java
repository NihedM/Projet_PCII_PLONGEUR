package view;

import controler.*;
import model.constructions.Base;
import model.gains_joueur.Referee;
import model.objets.*;
import model.ressources.Collier;
import model.unite_controlables.Plongeur;
import model.unite_controlables.PlongeurArme;
import model.unite_controlables.SousMarin;
import model.unite_non_controlables.Enemy;
import model.unite_non_controlables.Pieuvre;
import model.unite_non_controlables.PieuvreBebe;
import view.debeug.GameInfoWindow;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GamePanel extends JPanel {
    private BufferedImage backBuffer;
    private Graphics2D backBufferGraphics;

    private Barre timeProgressBar;
    private static final int WARNING_THRESHOLD = 20; // Seuil d'avertissement pour la barre de temps

    public static int getPanelWidth() {
        return Toolkit.getDefaultToolkit().getScreenSize().width;
    }

    public static int getPanelHeight() {
        return Toolkit.getDefaultToolkit().getScreenSize().height;
    }

    public static int getViewportWidth() {
        return (int) (getPanelWidth() * 0.75); // 75% de la largeur
    }

    public static int getViewportHeight() {
        return getPanelHeight();
    }

    public static int getPanelInfoWidth() {
        return getPanelWidth() - getViewportWidth();
    }


    private static volatile GamePanel instance;

    // Variables pour la caméra
    private int cameraX = 0;
    private int cameraY = 0;
    private boolean isDragging = false;
    private Point dragStart = new Point();

    // Dimensions du terrain
    public static final int TERRAIN_WIDTH = 10000;
    public static final int TERRAIN_HEIGHT = 10000;

    public static final int PANEL_INFO_WIDTH = getPanelWidth() / 4;
    public static final int VIEWPORT_WIDTH = getPanelWidth() - PANEL_INFO_WIDTH;
    public static final int VIEWPORT_HEIGHT = getPanelHeight();

    // Dimensions minimap (même ratio que la carte principale)
    private static final float MAP_RATIO = TERRAIN_WIDTH / (float) TERRAIN_HEIGHT;
    public static final int MINIMAP_HEIGHT = 200; // Hauteur fixe
    public static final int MINIMAP_WIDTH = (int) (MINIMAP_HEIGHT * MAP_RATIO); // Largeur calculée
    private static final int MINIMAP_MARGIN = 10;
    public static final float MINIMAP_SCALE_X = MINIMAP_WIDTH / (float) TERRAIN_WIDTH;
    public static final float MINIMAP_SCALE_Y = MINIMAP_HEIGHT / (float) TERRAIN_HEIGHT;

    private int grid[][] = new int[TileManager.nbTilesWidth][TileManager.nbTilesHeight];
    private ConcurrentHashMap<CoordGrid, CopyOnWriteArrayList<Objet>> objetsMap = new ConcurrentHashMap<>();
    private CopyOnWriteArrayList<UniteControlable> unitesEnJeu = new CopyOnWriteArrayList<>();
    private CopyOnWriteArrayList<UniteControlable> unitesSelected = new CopyOnWriteArrayList<>();


    private ProximityChecker proxy;
    private TileUpdater updater;

    private Terrain terrain;
    private Base baseUnique;   //Temporairement

    // Composants UI
    private JPanel infoContainer;
    private InfoPanel infoPanel;
    private InfoPanelUNC infoPanelUNC;
    private MinimapPanel minimapPanel;

    private boolean paused = false;
    private ArrayList<Ressource> collectedResources = new ArrayList<>();
    private Ressource ressourceSelectionnee;


    // Dimensions du terrain
    public static final int TERRAIN_MIN_X = 0,
            TERRAIN_MAX_X = TERRAIN_WIDTH,
            TERRAIN_MIN_Y = 0,
            TERRAIN_MAX_Y = TERRAIN_HEIGHT,
            GAME_AREA_WIDTH = VIEWPORT_WIDTH;


    private ZoneEnFonctionnement mainZone;
    private CopyOnWriteArrayList<ZoneEnFonctionnement> dynamicZones;

    public static final int VIEWPORT_BUFFER = 500;
    public static final int UNIT_BUFFER = 500;

    private int viewportMinX = cameraX - VIEWPORT_BUFFER;
    private int viewportMinY = cameraY - VIEWPORT_BUFFER;
    private int viewportMaxX = cameraX + VIEWPORT_WIDTH + VIEWPORT_BUFFER;
    private int viewportMaxY = cameraY + VIEWPORT_HEIGHT + VIEWPORT_BUFFER;


    private SelectionClic selectionClic;
    private final int INFO_PANEL_TARGET_WIDTH = 200;
    private int currentInfoPanelWidth = 0;
    private Timer slideTimer;
    private boolean isSliding = false;
    private boolean deplacementMode = false;
    private boolean recuperationMode = false;
    private boolean isShootingMode = false;

    private VictoryManager victoryManager;


    private BufferedImage plongeurImage, plongeurArmeImage, collierImage, pieuvreImage;
    private Image plongeurGif, enemyGif, plongeurArmeGif;

    public GamePanel() {
        instance = this;
        setLayout(null);
        setPreferredSize(new Dimension(getPanelWidth(), getPanelHeight()));
        setBackground(new Color(173, 216, 230));

        loadImages();

        this.terrain = new Terrain(TERRAIN_WIDTH, TERRAIN_HEIGHT);
        this.baseUnique = new Base(new Position(100, 200), 20); //Temporairement
        addObjet(baseUnique); //Temporairement

        int viewportMinX = cameraX - VIEWPORT_BUFFER;
        int viewportMinY = cameraY - VIEWPORT_BUFFER;
        int viewportMaxX = cameraX + VIEWPORT_WIDTH + VIEWPORT_BUFFER;
        int viewportMaxY = cameraY + VIEWPORT_HEIGHT + VIEWPORT_BUFFER;

        mainZone = new ZoneEnFonctionnement(viewportMinX, viewportMinY, viewportMaxX, viewportMaxY);
        dynamicZones = new CopyOnWriteArrayList<>();


        createBackBuffer();
        initUIComponents();
        setupListeners();
        initSystems();
    }

    private void createBackBuffer() {
        backBuffer = new BufferedImage(getPanelWidth(), getPanelHeight(), BufferedImage.TYPE_INT_ARGB);
        backBufferGraphics = backBuffer.createGraphics();
        backBufferGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        backBufferGraphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
    }
    private Map<String, Image> imageCache = new HashMap<>();

    private Image getOptimizedImage(String key, Image original, int width, int height) {
        String cacheKey = key + "_" + width + "x" + height;
        if (!imageCache.containsKey(cacheKey)) {
            Image scaled = original.getScaledInstance(width, height, Image.SCALE_SMOOTH);
            imageCache.put(cacheKey, scaled);
        }
        return imageCache.get(cacheKey);
    }


    public Terrain getTerrain() {
        return terrain;
    }

    public Base getMainBase() {
        return baseUnique;
    }


    private void loadImages() {
        try {
            plongeurImage = ImageIO.read(Objects.requireNonNull(getClass().getResource("/view/images/plongeurNormal.png")));
            plongeurArmeImage = ImageIO.read(Objects.requireNonNull(getClass().getResource("/view/images/plongeurArme.png")));

            collierImage = ImageIO.read(Objects.requireNonNull(getClass().getResource("/view/images/collier.png")));
            pieuvreImage = ImageIO.read(Objects.requireNonNull(getClass().getResource("/view/images/pieuvre.png")));

            plongeurGif = new ImageIcon(Objects.requireNonNull(getClass().getResource("/view/images/plongeurNormal.gif"))).getImage();
            plongeurArmeGif = new ImageIcon(Objects.requireNonNull(getClass().getResource("/view/images/plongeurArme.gif"))).getImage();
            enemyGif = new ImageIcon(Objects.requireNonNull(getClass().getResource("/view/images/enemyTest.gif"))).getImage();

            if (plongeurImage == null || collierImage == null || pieuvreImage == null
                    || plongeurGif == null || enemyGif == null) {
                throw new IOException("Image non trouvée");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initUIComponents() {
        // Création de la barre de temps
        timeProgressBar = new Barre(0, 100, Color.GREEN, getViewportWidth(), 20, WARNING_THRESHOLD);
        timeProgressBar.setBounds(0, 0, getViewportWidth(), 20);
        timeProgressBar.setShowAsTime(true); // Active le format temporel
        timeProgressBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.BLACK));
        add(timeProgressBar);

        // Panel d'informations
        infoContainer = new JPanel(new CardLayout());
        infoContainer.setBackground(new Color(173, 216, 230)); // Même fond que le panel principal
        infoPanel = new InfoPanel();
        infoPanelUNC = new InfoPanelUNC();

        infoContainer.add(infoPanel, "unit");
        infoContainer.add(infoPanelUNC, "resource");
        infoContainer.add(new JPanel(), "empty");

        infoContainer.setBounds(getViewportWidth(), 0, getPanelInfoWidth(), getPanelHeight());
        add(infoContainer);

        // Bouton Market
        JButton marketButton = new JButton("Market");
        marketButton.setBounds(500, 20, 100, 30);
        marketButton.addActionListener(e -> {
            setPaused(true);
            JFrame topFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
            new MarketPopup(topFrame).setVisible(true);
            setPaused(false);
        });
        add(marketButton);

        // Minimap
        minimapPanel = new MinimapPanel();
        minimapPanel.setBounds(
                MINIMAP_MARGIN,
                getPanelHeight() - MINIMAP_HEIGHT - MINIMAP_MARGIN,
                MINIMAP_WIDTH,
                MINIMAP_HEIGHT
        );
        add(minimapPanel);

        // Timer pour rafraîchir la minimap
        new Timer(100, e -> minimapPanel.repaint()).start();
    }


    private void setupListeners() {
        selectionClic = new SelectionClic(this);
        addMouseListener(selectionClic);
        addMouseMotionListener(selectionClic);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON2) {
                    isDragging = true;
                    dragStart = e.getPoint();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON2) {
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
        try {
            executor.submit(updater);
            executor.submit(proxy);
        } finally {
            executor.shutdown();
        }
        new GameInfoWindow(objetsMap, unitesEnJeu, unitesSelected);

    }

    // Méthodes pour la caméra
    public void moveCamera(int dx, int dy) {
        cameraX = Math.max(0, Math.min(TERRAIN_WIDTH - VIEWPORT_WIDTH, cameraX + dx));
        cameraY = Math.max(0, Math.min(TERRAIN_HEIGHT - VIEWPORT_HEIGHT, cameraY + dy));

        viewportMinX = cameraX - VIEWPORT_BUFFER;
        viewportMinY = cameraY - VIEWPORT_BUFFER;
        viewportMaxX = cameraX + VIEWPORT_WIDTH + VIEWPORT_BUFFER;
        viewportMaxY = cameraY + VIEWPORT_HEIGHT + VIEWPORT_BUFFER;

        mainZone.updateMainBounds(viewportMinX, viewportMinY, viewportMaxX, viewportMaxY);


        repaint();
    }

    private Point worldToScreen(int worldX, int worldY) {
        //return new Point(worldX - cameraX, worldY - cameraY);
        Position terrainPosition = new Position(worldX, worldY);
        Position panelPosition = terrainPosition.toPanelPosition(terrain, cameraX, cameraY);
        return new Point(panelPosition.getX(), panelPosition.getY());

    }

    public Point screenToWorld(Point screenPoint) {
        Position panelPosition = new Position(screenPoint.x, screenPoint.y);
        Position terrainPosition = panelPosition.toTerrainPosition(terrain, cameraX, cameraY);
        return new Point(terrainPosition.getX(), terrainPosition.getY());
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

    public void setVictoryManager(VictoryManager vm) {
        this.victoryManager = vm;
    }

    public void reset() {
        // Réinitialiser la caméra
        cameraX = 0;
        cameraY = 0;
        // Réinitialiser le mode de déplacement/récupération
        // (les autres variables booléennes si nécessaire)

        // Vider toutes les collections d'objets
        objetsMap.clear();
        unitesEnJeu.clear();
        unitesSelected.clear();
        collectedResources.clear();

        // Réinitialiser le terrain et recréer la base initiale
        terrain = new Terrain(TERRAIN_WIDTH, TERRAIN_HEIGHT);
        baseUnique = new Base(new Position(100, 200), 20);
        addObjet(baseUnique);

        addUniteControlable(new Plongeur(3, new Position(50, 50)));

        // Réinitialiser le VictoryManager
        victoryManager = null;

        // Repaint pour mettre à jour l'affichage
        repaint();
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
        boolean withinTerrain = x > TERRAIN_MIN_X && x < TERRAIN_MAX_X && y > TERRAIN_MIN_Y && y < TERRAIN_MAX_Y;
        boolean withinTiles = x >= 0 && x < TileManager.nbTilesWidth * TileManager.TILESIZE &&
                y >= 0 && y < TileManager.nbTilesHeight * TileManager.TILESIZE;
        return withinTerrain && withinTiles;
    }


    public void setShootingMode(boolean shootingMode) {
        this.isShootingMode = shootingMode;
        if (shootingMode) {
            setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        } else {
            setCursor(Cursor.getDefaultCursor());
        }
    }

    public boolean isShootingMode() {
        return isShootingMode;
    }


    public ZoneEnFonctionnement getMainZone() {
        return mainZone;
    }

    public CopyOnWriteArrayList<ZoneEnFonctionnement> getDynamicZones() {
        return dynamicZones;
    }


    public void addDynamicZone(ZoneEnFonctionnement newZone) {
        dynamicZones.add(newZone);
        //removeOverlappingZones();
    }

    public void removeDynamicZone(ZoneEnFonctionnement zone) {
        dynamicZones.remove(zone);
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

    public int[][] getVoisins(int x, int y) {
        int[][] voisins = new int[8][2];
        int i = 0;
        for (int j = -1; j <= 1; j++) {
            for (int k = -1; k <= 1; k++) {
                if (j == 0 && k == 0) continue;
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
        if (objetsMap.containsKey(objet.getCoordGrid())) {
            objetsMap.get(objet.getCoordGrid()).add(objet);

        } else {
            CopyOnWriteArrayList<Objet> objetsAtCoord = new CopyOnWriteArrayList<>();
            objetsAtCoord.add(objet);
            objetsMap.put(objet.getCoordGrid(), objetsAtCoord);
        }

        /*if (objet instanceof Ressource){
            Ressource ressource = (Ressource) objet;
            GestionRessource gestionRessource = new GestionRessource(ressource, 1000); // Intervalle de 1 seconde
            gestionRessource.addListener(infoPanelUNC); // ajouter InfoPanelUNC comme listener
            gestionRessource.start(); // Démarrer le thread

        }*/
    }

    public void addUniteControlable(UniteControlable unite) {
        this.addObjet(unite);
        unitesEnJeu.add(unite);
    }

    public void addCollectedResource(model.objets.Ressource r) {
        collectedResources.add(r);
    }


    //-----------------SUPPRESSIONS------------------------------------------------------------------------------------------------------

    public synchronized void removeObjet(Objet objet, CoordGrid coord) {
        CopyOnWriteArrayList<Objet> objetsAtCoord = objetsMap.get(coord);
        if (objetsAtCoord != null) {
            boolean removed = objetsAtCoord.remove(objet);
            if (removed) {
                if (objet instanceof Ressource) {
                    // Décrémente le compteur de ressources pour cette zone
                    terrain.decrementResourcesAt(objet.getPosition().getX(), objet.getPosition().getY());
                }

                if (objetsAtCoord.isEmpty()) {
                    objetsMap.remove(coord);
                }
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

    public void killUnite(Unite unite) {
        boolean existsInObjetsMap = objetsMap.containsKey(unite.getCoordGrid()) && objetsMap.get(unite.getCoordGrid()).contains(unite);
        if (existsInObjetsMap) {
            removeObjet(unite, unite.getCoordGrid());
            if (unite.getDeplacementThread() != null)
                unite.getDeplacementThread().stopThread();
        }
        if (unite instanceof UniteControlable) {
            boolean existsInUnitesEnJeu = unitesEnJeu.contains(unite);
            boolean existsInUnitesSelected = unitesSelected.contains(unite);
            if (existsInUnitesSelected)
                unitesSelected.remove(unite);
            if (existsInUnitesEnJeu)
                unitesEnJeu.remove(unite);


        } else {
            if (unite instanceof Enemy) {
                GameMaster.getInstance().removeEnemy((Enemy) unite);
            }
        }


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
                    infoContainer.setPreferredSize(new Dimension(currentWidth, getPanelHeight()));
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
                    infoContainer.setPreferredSize(new Dimension(currentWidth, getPanelHeight()));
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
        infoContainer.setPreferredSize(new Dimension(200, getPanelHeight()));
        infoContainer.revalidate();
        repaint();
    }

    public void showFixedInfoPanel(String panelType) {
        CardLayout cl = (CardLayout) infoContainer.getLayout();
        cl.show(infoContainer, panelType);
        // Définir la largeur fixe souhaitée (par exemple 200 pixels)
        infoContainer.setPreferredSize(new Dimension(200, getPanelHeight()));
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


    /*public static void printGridContents(ConcurrentHashMap<CoordGrid, CopyOnWriteArrayList<model.objets.Objet>> objetsMap) {
        for (ConcurrentHashMap.Entry<CoordGrid,CopyOnWriteArrayList<model.objets.Objet>> entry : objetsMap.entrySet()) {
            CoordGrid coord = entry.getKey();
            CopyOnWriteArrayList<model.objets.Objet> objetsDansTile = entry.getValue();

            System.out.println("Tile (" + coord.getX() + ", " + coord.getY() + "):");
            for (model.objets.Objet objet : objetsDansTile) {
                System.out.println("  - " + objet.getClass().getSimpleName() + " at (" + objet.getPosition().getX() + ", " + objet.getPosition().getY() + ")");
            }
        }
    }*/


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
        for (int i = 0; i < 8; i++) {
            if (voisins[i][0] < 0 || voisins[i][0] >= TileManager.nbTilesWidth ||
                    voisins[i][1] < 0 || voisins[i][1] >= TileManager.nbTilesHeight) continue;
            paintTile(voisins[i][0], voisins[i][1], color, g);
        }
    }

    @Override
    protected synchronized void paintComponent(Graphics g) {
        super.paintComponent(g);
        

        // Dessin sur le backbuffer
        renderToBackBuffer();

        // Copie du backbuffer à l'écran
        g.drawImage(backBuffer, 0, 0, null);

        if (selectionClic != null) {
            selectionClic.paintSelection(g);
        }
    }

    private void renderToBackBuffer() {
        // Effacer le backbuffer
        backBufferGraphics.setColor(new Color(173, 216, 230));
        backBufferGraphics.fillRect(0, 0, getWidth(), getHeight());

        // Optimisation: ne dessiner que ce qui est visible
        Rectangle clip = backBufferGraphics.getClipBounds();

        // Dessinez vos éléments ici en utilisant backBufferGraphics au lieu de g
        drawBase(backBufferGraphics);
        drawTerrain(backBufferGraphics, clip);
        drawObjects(backBufferGraphics, clip);
        drawObjet(backBufferGraphics, baseUnique, worldToScreen(baseUnique.getPosition().getX(), baseUnique.getPosition().getY()));
    }

    public void drawObjects(Graphics g, Rectangle clip) {
        for (Objet objet : objetsMap.values().stream().flatMap(CopyOnWriteArrayList::stream).toList()) {

            Point screenPos = worldToScreen(objet.getPosition().getX(), objet.getPosition().getY());

            int diametre = objet.getRayon() * 2;

            if (isVisibleInViewport(screenPos, objet.getRayon())) {
                Image image = null;
                Graphics2D g2d = (Graphics2D) g.create();

                if (objet instanceof PlongeurArme)
                    image = ((PlongeurArme) objet).getVitesseCourante() >= 0.1 ? plongeurArmeGif : plongeurArmeImage;

                else if (objet instanceof Plongeur) {
                    image = ((Plongeur) objet).getVitesseCourante() >= 0.1 ? plongeurGif : plongeurImage;
                    if (((Plongeur) objet).isFaitFuire()) {
                        g2d.setColor(Color.ORANGE);
                        int rayonFuite = ((Plongeur) objet).getRayonFuite();
                        g2d.drawOval(screenPos.x - rayonFuite, screenPos.y - rayonFuite, rayonFuite * 2, rayonFuite * 2);
                    }
                } else if (objet instanceof Pieuvre || objet instanceof PieuvreBebe) {
                    image = pieuvreImage;//((Enemy) objet).getVitesseCourante() >= 0.1 ? enemyGif : enemyImage;
                } else if (objet instanceof Collier) {
                    image = collierImage;
                }

                if (objet instanceof Unite unite) {
                    // Draw the circle/base
                    g.fillOval(screenPos.x - objet.getRayon(), screenPos.y - objet.getRayon(), diametre, diametre);

                    assert image != null;
                    int originalImgWidth = image.getWidth(null);
                    int originalImgHeight = image.getHeight(null);

                    // Calculate scaling factor based on hitbox radius (adjust 2.0 multiplier as needed)
                    double scaleFactor;

                    if (unite instanceof UniteControlable) {
                        scaleFactor = (unite.getRayon() * 4.0) / Math.min(originalImgWidth, originalImgHeight);
                    } else {
                        scaleFactor = (unite.getRayon() * 3.0) / Math.min(originalImgWidth, originalImgHeight); // Adjust the factor for non-controllable units
                    }
                    // Scale image dimensions
                    int imgWidth = (int) (originalImgWidth * scaleFactor);
                    int imgHeight = (int) (originalImgHeight * scaleFactor);
                    int halfWidth = imgWidth / 2;
                    int halfHeight = imgHeight / 2;

// Only rotate if there's significant movement
                    if (Math.hypot(unite.getVx(), unite.getVy()) > 0.1) {
                        double angle = Math.atan2(unite.getVy(), unite.getVx());

                        try {
                            g2d.translate(screenPos.x, screenPos.y);
                            g2d.rotate(angle + Math.PI / 2);

                            if (unite.getVx() > 0) {  // Fixed: Changed back to < 0 for left movement
                                g2d.scale(-1, 1);
                            }

                            g2d.drawImage(image,
                                    -halfWidth,
                                    -halfHeight,
                                    imgWidth,
                                    imgHeight,
                                    null);
                        } finally {
                            g2d.dispose();
                        }
                    } else {
                        // Default drawing when not moving
                        g.drawImage(image,
                                screenPos.x - halfWidth,
                                screenPos.y - halfHeight,
                                imgWidth,
                                imgHeight,
                                null);
                    }
                    continue;
                }

                if (image != null) {
                    g.drawImage(image, screenPos.x - objet.getRayon(), screenPos.y - objet.getRayon(), diametre, diametre, null);
                } else {
                    g.setColor(Color.PINK);
                    g.fillOval(screenPos.x - objet.getRayon(), screenPos.y - objet.getRayon(), diametre, diametre);
                }
            }
        }
    }

    public void drawTerrain(Graphics g, Rectangle clip) {
        for (int i = cameraX/TileManager.TILESIZE; i < (cameraX+VIEWPORT_WIDTH)/TileManager.TILESIZE + 1; i++) {
            for (int j = cameraY/TileManager.TILESIZE; j < (cameraY+VIEWPORT_HEIGHT)/TileManager.TILESIZE + 1; j++) {
                if (i >= 0 && i < TileManager.nbTilesWidth && j >= 0 && j < TileManager.nbTilesHeight) {
                    g.setColor(Color.BLACK);
                    Point screenPos = worldToScreen(i * TileManager.TILESIZE, j * TileManager.TILESIZE);
                    g.drawRect(screenPos.x, screenPos.y, TileManager.TILESIZE, TileManager.TILESIZE);
                }
            }
        }
        // Dessin des zones de profondeur
        drawDepthZones(g);
    }

    public static boolean isVisibleInViewport(Point screenPos, int rayon) {
        return screenPos.x + rayon*2 >= 0 && screenPos.x - rayon <= VIEWPORT_WIDTH &&
                screenPos.y + rayon*2 >= 0 && screenPos.y - rayon <= VIEWPORT_HEIGHT;
    }

    private void drawObjet(Graphics g, Objet objet, Point screenPos) {
        int diametre = objet.getRayon() * 2;

        if(objet instanceof Base){
            return;
        }

        if (objet instanceof Ressource) {
            drawRessource(g, (Ressource) objet, screenPos, diametre);
        } else if (objet instanceof UniteControlable) {
            drawUniteControlable(g, (UniteControlable) objet, screenPos, diametre);
        } else {
            g.setColor(Color.PINK);
            g.fillOval(screenPos.x - objet.getRayon(), screenPos.y - objet.getRayon(), diametre, diametre);
        }
    }

    private void drawRessource(Graphics g, Ressource ressource, Point screenPos, int diametre) {
        if (ressource.getEtat() == Ressource.Etat.EN_CROISSANCE) {
            g.setColor(Color.YELLOW);
        } else if (ressource.getEtat() == Ressource.Etat.PRET_A_RECOLTER) {
            g.setColor(Color.GREEN);
        } else {
            return;
        }
        g.fillOval(screenPos.x - ressource.getRayon(), screenPos.y - ressource.getRayon(), diametre, diametre);
    }

    private void drawUniteControlable(Graphics g, UniteControlable unite, Point screenPos, int diametre) {
        /*g.setColor(unite.isSelected() ? Color.RED : Color.BLACK);
        g.fillOval(screenPos.x - unite.getRayon(), screenPos.y - unite.getRayon(), diametre, diametre);

        if (unite.getDestination() != null) {
            Point destScreenPos = worldToScreen(unite.getDestination().getX(), unite.getDestination().getY());
            g.setColor(Color.BLUE);
            g.drawLine(screenPos.x, screenPos.y, destScreenPos.x, destScreenPos.y);
        }

        }*/

        Graphics2D g2d = (Graphics2D) g.create();
        double angle = Math.atan2(unite.getVy(), unite.getVx());
        g2d.rotate(angle, screenPos.x, screenPos.y);

        if (unite instanceof Plongeur) {
            if (plongeurImage != null) {


                if (unite.getVx() < 0) {
                    g2d.drawImage(plongeurImage,
                            screenPos.x - unite.getRayon(),
                            screenPos.y + unite.getRayon(),
                            diametre,
                            -diametre,
                            null);
                } else {
                    g2d.drawImage(plongeurImage,
                            screenPos.x - unite.getRayon(),
                            screenPos.y - unite.getRayon(),
                            diametre,
                            diametre,
                            null);
                }
            } else {
                g2d.setColor(unite.isSelected() ? Color.RED : Color.BLACK);
                g2d.fillOval(screenPos.x - unite.getRayon(), screenPos.y - unite.getRayon(), diametre, diametre);
            }

            if (((Plongeur)unite).isFaitFuire()) {
                g2d.setColor(Color.ORANGE);
                int rayonFuite = ((Plongeur) unite).getRayonFuite();
                g2d.drawOval(screenPos.x - rayonFuite, screenPos.y - rayonFuite, rayonFuite * 2, rayonFuite * 2);
            }
        } else {
            g2d.setColor(unite.isSelected() ? Color.RED : Color.BLACK);
            g2d.fillOval(screenPos.x - unite.getRayon(), screenPos.y - unite.getRayon(), diametre, diametre);
        }

        // Draw a line to indicate the direction
        int lineLength = 20;
        int endX = (int) (screenPos.x + lineLength * Math.cos(angle));
        int endY = (int) (screenPos.y + lineLength * Math.sin(angle));
        g2d.setColor(Color.BLUE);
        g2d.drawLine(screenPos.x, screenPos.y, endX, endY);

        g2d.dispose();

    }

    private void drawSpawnPoints(Graphics g) {
        g.setColor(Color.ORANGE);
        for (EnemySpawnPoint spawnPoint : SpawnManager.getInstance().getSpawnPoints()) {
            Point screenPos = worldToScreen(spawnPoint.getPosition().getX(), spawnPoint.getPosition().getY());
            int diameter = spawnPoint.getRayon() * 2;
            g.fillOval(screenPos.x - spawnPoint.getRayon(), screenPos.y - spawnPoint.getRayon(), diameter, diameter);
        }
    }

    private void drawBase(Graphics g) {

        Point baseScreenPos = worldToScreen(baseUnique.getPosition().getX(), baseUnique.getPosition().getY());


        g.setColor(Color.GREEN);
        Position[] coints = baseUnique.getCoints();
        Point topLeftScreenPos = worldToScreen(coints[0].getX(), coints[0].getY());

        g.fillRect(topLeftScreenPos.x, topLeftScreenPos.y, baseUnique.getLargeur(), baseUnique.getLongueur());


        g.setColor(Color.CYAN);
        g.fillOval(baseScreenPos.x- baseUnique.getRayon(),
                baseScreenPos.y - baseUnique.getRayon(),
                baseUnique.getRayon() * 2, baseUnique.getRayon() * 2);



        // A REDEFINIR
        java.util.List<SousMarin> submarines = baseUnique.getSubmarines();
        if (submarines != null && !submarines.isEmpty()) {
            // Positionner les sous-marins à droite de la base
            Position topRight = coints[1];
            Point topRightScreenPos = worldToScreen(topRight.getX(), topRight.getY());
            int offsetX = 10; // Marge à droite
            int startX = topRightScreenPos.x + offsetX;
            int startY = topRightScreenPos.y;

            int submarineWidth = 20;
            int submarineHeight = 10;
            int spacing = 5; // Espace entre les sous-marins

            g.setColor(Color.BLUE);
            for (int i = 0; i < submarines.size(); i++) {
                int x = startX + i * (submarineWidth + spacing);
                g.fillRect(x, startY, submarineWidth, submarineHeight);
            }
        }

    }

    private void drawPlayerInfo(Graphics g) {
        g.setColor(Color.BLACK);
        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.BOLD, 16));
        g.drawString("Points: " + Referee.getInstance().getPointsVictoire(), 10, 40);
        g.drawString("Argent: " + Referee.getInstance().getArgentJoueur(), 10, 60);
        g.drawString("Unités: " + unitesEnJeu.size(), 10, 80);
    }

    public Barre getTimeProgressBar() {
        return timeProgressBar;
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

    public static float getMinimapScaleX() {
        return MINIMAP_SCALE_X;
    }

    public static float getMinimapScaleY() {
        return MINIMAP_SCALE_Y;
    }

    public MinimapPanel getMinimapPanel() {
        return minimapPanel;
    }

    public void checkAndClearResourcePanel(Ressource ressource) {
        if (ressourceSelectionnee != null && ressourceSelectionnee.equals(ressource)) {
            setRessourceSelectionnee(null);
            showEmptyInfoPanel();
        }
    }

    private void drawDepthZones(Graphics g) {
        for (int x = 0; x < TERRAIN_WIDTH; x += TileManager.TILESIZE) {
            for (int y = 0; y < TERRAIN_HEIGHT; y += TileManager.TILESIZE) {
                int depth = terrain.getDepthAt(x, y);
                Point screenPos = worldToScreen(x, y);

                // Couleurs de base pour chaque profondeur
                Color color;
                switch (depth) {
                    case 1: color = new Color(100, 200, 255, 80); break;  // Bleu clair
                    case 2: color = new Color(50, 150, 220, 100); break;  // Bleu moyen
                    case 3: color = new Color(0, 100, 190, 120); break;   // Bleu foncé
                    case 4: color = new Color(0, 50, 150, 140); break;    // Bleu très foncé
                    default: color = Color.BLACK;
                }

                g.setColor(color);
                g.fillRect(screenPos.x, screenPos.y,
                        TileManager.TILESIZE, TileManager.TILESIZE);
            }
        }
    }

    private void drawAmmo(Graphics g) {
        for (Ammo ammo : AmmoManager.getInstance().getActiveAmmo()) {
            Point screenPos = worldToScreen(ammo.getPosition().getX(), ammo.getPosition().getY());
            int diameter = ammo.getRayon() * 2;
            g.setColor(Color.RED); // Set the color for the ammo
            g.fillOval(screenPos.x - ammo.getRayon(), screenPos.y - ammo.getRayon(), diameter, diameter);
        }
    }

}