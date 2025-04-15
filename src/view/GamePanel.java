package view;

import controler.*;
import model.constructions.Base;
import model.gains_joueur.Referee;
import model.objets.*;
import model.objets.spawns.EnemySpawnPoint;
import model.ressources.Bague;
import model.ressources.Coffre;
import model.ressources.Collier;
import model.ressources.Tresor;
import model.unite_controlables.Plongeur;
import model.unite_controlables.PlongeurArme;
import model.unite_controlables.SousMarin;
import model.unite_non_controlables.Calamar;
import model.unite_non_controlables.Enemy;
import model.unite_non_controlables.Pieuvre;
import model.unite_non_controlables.PieuvreBebe;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
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
    //-------------------------objets stocké dans la classe---------------------------------------------
    private static volatile GamePanel instance;
    private Terrain terrain;
    private Base baseUnique;

    //conteneur principal
    private ConcurrentHashMap<CoordGrid, CopyOnWriteArrayList<Objet>> objetsMap = new ConcurrentHashMap<>();
    private CopyOnWriteArrayList<UniteControlable> unitesEnJeu = new CopyOnWriteArrayList<>();  //unite que le joueur peut controler
    private CopyOnWriteArrayList<UniteControlable> unitesSelected = new CopyOnWriteArrayList<>();   //unités que le joueur controle courament

    private ZoneEnFonctionnement mainZone;
    private CopyOnWriteArrayList<ZoneEnFonctionnement> dynamicZones;

    private ArrayList<Ressource> collectedResources = new ArrayList<>();
    private Ressource ressourceSelectionnee;


    //"observateurs"
    private ProximityChecker proxy;
    private TileUpdater updater;
    private VictoryManager victoryManager;


    //-------------------------dimentions et autres constantes---------------------------------------

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

    // Dimensions du terrain
    public static final int TERRAIN_MIN_X = 0,
            TERRAIN_MAX_X = TERRAIN_WIDTH,
            TERRAIN_MIN_Y = 0,
            TERRAIN_MAX_Y = TERRAIN_HEIGHT,
            GAME_AREA_WIDTH = VIEWPORT_WIDTH;

    private static final int WARNING_THRESHOLD = 20; // Seuil d'avertissement pour la barre de temps

    private final int INFO_PANEL_TARGET_WIDTH = 200;

    //pour les zones de fonctionnement
    public static final int VIEWPORT_BUFFER = 500;
    public static final int UNIT_BUFFER = 1000;

    //Limite d'unités pour le joueur
    public static final int MAX_UNITS_IN_GAME = 100; // Maximum number of units in the game
    public static final int MAX_SELECTED_UNITS = 10; // Maximum number of selected units


    public static final Font CUSTOM_FONT = loadCustomFont();


    //------------------------------variables----------------------------------------------------------------


    private Barre timeProgressBar;
    private boolean paused = false;


    // Variables pour la caméra
    private int cameraX = 0;
    private int cameraY = 0;
    private boolean isDragging = false;
    private Point dragStart = new Point();

    // Composants UI
    private BackgroundPanel infoContainer;
    private InfoPanel infoPanel;
    private InfoPanelUNC infoPanelUNC;
    private MinimapPanel minimapPanel;
    private JPanel playerInfoPanel;
    private JLabel pointsLabel, moneyLabel, unitsLabel;



    private int viewportMinX = cameraX - VIEWPORT_BUFFER;
    private int viewportMinY = cameraY - VIEWPORT_BUFFER;
    private int viewportMaxX = cameraX + VIEWPORT_WIDTH + VIEWPORT_BUFFER;
    private int viewportMaxY = cameraY + VIEWPORT_HEIGHT + VIEWPORT_BUFFER;



    private SelectionClic selectionClic;
    private int currentInfoPanelWidth = 0;
    private Timer slideTimer;
    private boolean isSliding = false;
    private boolean deplacementMode = false;
    private boolean recuperationMode = false;
    private boolean isAttackingMode = false;

    private boolean pendingShootAction = false;

    private boolean boardingMode = false;
    private SousMarin targetSubmarine = null;

    private JPanel overlayPanel;



    public GamePanel() {
        instance = this;
        setLayout(null);
        setPreferredSize(new Dimension(getPanelWidth(), getPanelHeight()));
        setBackground(new Color(173, 216, 230));

        loadImages();

        this.terrain = new Terrain(TERRAIN_WIDTH, TERRAIN_HEIGHT);
        this.baseUnique = new Base(new Position(200, 300), 45);
        addObjet(baseUnique); //Temporairement

        int viewportMinX = cameraX - VIEWPORT_BUFFER;
        int viewportMinY = cameraY - VIEWPORT_BUFFER;
        int viewportMaxX = cameraX + VIEWPORT_WIDTH + VIEWPORT_BUFFER;
        int viewportMaxY = cameraY + VIEWPORT_HEIGHT + VIEWPORT_BUFFER;

        mainZone = new ZoneEnFonctionnement(viewportMinX, viewportMinY, viewportMaxX, viewportMaxY);
        dynamicZones = new CopyOnWriteArrayList<>();

        overlayPanel = new JPanel();
        overlayPanel.setBackground(new Color(0, 0, 0, 255));
        overlayPanel.setBounds(0, 0, getWidth(), getHeight());
        overlayPanel.setVisible(false);
        add(overlayPanel);

        setLayout(null);


        //createBackBuffer();
        initUIComponents();
        setupListeners();
        initSystems();
    }

    //-------------------------------------------Méthodes pour initialiser le panel-------------------------------------------------------------------------------------
    private void loadImages() {
        /*try {


        } catch (IOException e) {
            e.printStackTrace();
        }*/
    }

    /*private void createBackBuffer() {
        backBuffer = new BufferedImage(getPanelWidth(), getPanelHeight(), BufferedImage.TYPE_INT_ARGB);
        backBufferGraphics = backBuffer.createGraphics();
        backBufferGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        backBufferGraphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
    }*/

    private void initUIComponents() {
        // Création de la barre de temps
        timeProgressBar = new Barre(0, 100, Color.GREEN, getViewportWidth(), 20, WARNING_THRESHOLD);
        timeProgressBar.setBounds(0, 0, getViewportWidth(), 20);
        timeProgressBar.setShowAsTime(true); // Active le format temporel
        timeProgressBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.BLACK));
        add(timeProgressBar);

        // Panel d'informations
        infoContainer = new BackgroundPanel();
        infoContainer.setLayout(new CardLayout());

        infoPanel = new InfoPanel();
        infoPanelUNC = new InfoPanelUNC();


        infoContainer.add(infoPanel, "unit");
        infoContainer.add(infoPanelUNC, "resource");
        infoContainer.add(new JPanel(), "empty");

        infoContainer.setBounds(getViewportWidth(), 0, getPanelInfoWidth(), getPanelHeight());

        add(infoContainer);

        // Bouton Market
        ImageIcon marketIcon = new ImageIcon(GamePanel.getCachedImage("marketIcon.png").getScaledInstance(150, 75, Image.SCALE_SMOOTH));

        JButton marketButton = new JButton("Market", marketIcon);
        marketButton.setHorizontalTextPosition(SwingConstants.CENTER); // Center the text horizontally
        marketButton.setVerticalTextPosition(SwingConstants.CENTER);   // Center the text vertically
        marketButton.setFont(GamePanel.CUSTOM_FONT.deriveFont(16f));   // Set custom font and size
        marketButton.setForeground(Color.BLACK);                      // Set text color

        marketButton.setBounds(500, 20, 200, 50);
        marketButton.setBorderPainted(false); // Remove button border
        marketButton.setContentAreaFilled(false);
        marketButton.setFocusPainted(false);


        marketButton.addActionListener(e -> {
            setPaused(true);
            Window topWindow = SwingUtilities.getWindowAncestor(this);
            if (topWindow instanceof JWindow) {

                //todo


                // Create a temporary JFrame to act as the parent
                JFrame tempFrame = new JFrame();
                tempFrame.setUndecorated(true); // Make it invisible
                tempFrame.setLocationRelativeTo(topWindow); // Position it relative to the JWindow
                tempFrame.setVisible(true); // Required to initialize the frame
                new MarketPopup(tempFrame).setVisible(true);
                tempFrame.dispose(); // Dispose of the temporary frame after use




            } else if (topWindow instanceof JFrame) {
                new MarketPopup((JFrame) topWindow).setVisible(true);
            }
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

        initializePlayerInfoPanel();

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
        //new GameInfoWindow(objetsMap, unitesEnJeu, unitesSelected);

    }
    private void initializePlayerInfoPanel() {
        playerInfoPanel = new JPanel();
        playerInfoPanel.setLayout(new BoxLayout(playerInfoPanel, BoxLayout.X_AXIS)); // Horizontal alignment
        playerInfoPanel.setOpaque(false); // Make it transparent if needed

        // Load and scale icons
        ImageIcon pointsIcon = new ImageIcon(GamePanel.getCachedImage("vpIcon.png").getScaledInstance(150, 150, Image.SCALE_SMOOTH));
        ImageIcon moneyIcon = new ImageIcon(GamePanel.getCachedImage("moneyIcon.png").getScaledInstance(150, 150, Image.SCALE_SMOOTH));
        ImageIcon unitsIcon = new ImageIcon(GamePanel.getCachedImage("uniteIcon.png").getScaledInstance(150, 150, Image.SCALE_SMOOTH));

        // Create labels for points
        pointsLabel = new JLabel(pointsIcon);
        pointsLabel.setLayout(new BorderLayout());
        JLabel pointsValue = new JLabel("", SwingConstants.CENTER);
        pointsValue.setForeground(Color.BLACK);
        pointsValue.setFont(GamePanel.CUSTOM_FONT.deriveFont(30f));
        pointsValue.setBorder(BorderFactory.createEmptyBorder(-75, -5, 0, 0));
        pointsLabel.add(pointsValue, BorderLayout.CENTER);

        // Create labels for money
        moneyLabel = new JLabel(moneyIcon);
        moneyLabel.setLayout(new BorderLayout());
        JLabel moneyValue = new JLabel("", SwingConstants.CENTER);
        moneyValue.setForeground(Color.BLACK);
        moneyValue.setFont(GamePanel.CUSTOM_FONT.deriveFont(30f));
        moneyValue.setBorder(BorderFactory.createEmptyBorder(-75, -5, 0, 0));
        moneyLabel.add(moneyValue, BorderLayout.CENTER);

        // Create labels for units
        unitsLabel = new JLabel(unitsIcon);
        unitsLabel.setLayout(new BorderLayout());
        JLabel unitsValue = new JLabel("", SwingConstants.CENTER);
        unitsValue.setForeground(Color.BLACK);
        unitsValue.setFont(GamePanel.CUSTOM_FONT.deriveFont(30f));
        unitsValue.setBorder(BorderFactory.createEmptyBorder(-75, -5, 0, 0));
        unitsLabel.add(unitsValue, BorderLayout.CENTER);


        // Add labels to the panel
        playerInfoPanel.add(pointsLabel);
        playerInfoPanel.add(Box.createHorizontalStrut(-75));
        playerInfoPanel.add(moneyLabel);
        playerInfoPanel.add(Box.createHorizontalStrut(-75));
        playerInfoPanel.add(unitsLabel);


        // Add the panel to the main container
        this.add(playerInfoPanel);
        playerInfoPanel.setBounds(5, 10, 1000, 150);
    }
    //-------------------------------------------getters et setters des objets de la classe------------------------------------------------

    public static GamePanel getInstance() {return instance;}
    public Terrain getTerrain() {return terrain;}
    public Base getMainBase() {return baseUnique;}

    public synchronized ConcurrentHashMap<CoordGrid, CopyOnWriteArrayList<model.objets.Objet>> getObjetsMap() {return objetsMap;}
    public CopyOnWriteArrayList<model.objets.UniteControlable> getUnitesEnJeu() {return unitesEnJeu;}
    public CopyOnWriteArrayList<UniteControlable> getUnitesSelected() {return unitesSelected;}

    public ZoneEnFonctionnement getMainZone() {return mainZone;}
    public CopyOnWriteArrayList<ZoneEnFonctionnement> getDynamicZones() {return dynamicZones;}

    public ArrayList<model.objets.Ressource> getCollectedResources() {return collectedResources;}
    public synchronized Ressource getRessourceSelectionnee() {return ressourceSelectionnee;}
    public synchronized void setRessourceSelectionnee(Ressource ressource) {this.ressourceSelectionnee = ressource;}

    public void setVictoryManager(VictoryManager vm) {this.victoryManager = vm;}
    // Méthode pour accéder à l'instance de VictoryManager
    public VictoryManager getVictoryManager() {
        return victoryManager;
    }
    public void startGame() {victoryManager.startGame();}

    //---------------------------------------getters constantes------------------------------------------------
    public static int getPanelWidth() {return 1200;}
    public static int getPanelHeight() {return 800;}
    public static int getViewportWidth() {return (int) (getPanelWidth() * 0.75);} // 75% de la largeur
    public static int getViewportHeight() {return getPanelHeight();}
    public static int getPanelInfoWidth() {return getPanelWidth() - getViewportWidth();}

    public static int getMinimapWidth() {return MINIMAP_WIDTH;}
    public static int getMinimapHeight() {return MINIMAP_HEIGHT;}
    public static int getMinimapMargin() {return MINIMAP_MARGIN;}
    public static float getMinimapScaleX() {return MINIMAP_SCALE_X;}
    public static float getMinimapScaleY() {return MINIMAP_SCALE_Y;}

    //---------------------------------------getters des variables------------------------------------------------
    public Barre getTimeProgressBar() {return timeProgressBar;}
    public boolean isPaused() {return paused;}
    public void setPaused(boolean paused) {
        this.paused = paused;
        // Vous pouvez  interrompre ou suspendre certains threads ici si nécessaire
    }


    //-------------camera

    public int getCameraX() {return cameraX;}
    public int getCameraY() {return cameraY;}


    //--------------panels

    public InfoPanelUNC getInfoPanelUNC() {return infoPanelUNC;}
    public synchronized InfoPanel getInfoPanel() {return infoPanel;}
    public MinimapPanel getMinimapPanel() {return minimapPanel;}

    public boolean isRecuperationMode() {return recuperationMode;}public void setRecuperationMode(boolean mode) {this.recuperationMode = mode;}
    public boolean isDeplacementMode() {return deplacementMode;}
    public void setDeplacementMode(boolean deplacementMode) {this.deplacementMode = deplacementMode;}
    public boolean isAttackingMode() {return isAttackingMode;}
    public void setAttackinggMode(boolean attackingMode) {
        this. isAttackingMode = attackingMode;
        if (attackingMode) {
            setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        } else {
            setCursor(Cursor.getDefaultCursor());
        }
    }

    public boolean isPendingShootAction() {
        return pendingShootAction;
    }

    public void setPendingShootAction(boolean pendingShootAction) {

        this.pendingShootAction = pendingShootAction;
        if (pendingShootAction) {
            setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        } else {
            setCursor(Cursor.getDefaultCursor());
        }
    }




    //--------------------------------------------autres getters et setters--------------------------------

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

    public CopyOnWriteArrayList<Objet> getRessources() {
        CopyOnWriteArrayList<Objet> ressources = new CopyOnWriteArrayList<>();
        for (CopyOnWriteArrayList<Objet> objets : objetsMap.values()) {
            for (Objet objet : objets) {
                if (objet instanceof Ressource) {
                    ressources.add((Ressource) objet);
                }
            }
        }
        return ressources;
    }

    public void setGameOver(boolean gameOver) {
        overlayPanel.setVisible(gameOver);
        // Désactiver les interactions si nécessaire
        setEnabled(!gameOver);
    }

    //--------------------------------------------méthodes pour le fonctionnement------------------------------------------------

    public boolean isWithinTerrainBounds(Position position) {
        int x = position.getX();
        int y = position.getY();
        boolean withinTerrain = x > TERRAIN_MIN_X && x < TERRAIN_MAX_X && y > TERRAIN_MIN_Y && y < TERRAIN_MAX_Y;
        return withinTerrain ;
    }

    public static boolean isVisibleInViewport(Point screenPos, int rayon) {
        return screenPos.x + rayon*2 >= 0 && screenPos.x - rayon <= VIEWPORT_WIDTH &&
                screenPos.y + rayon*2 >= 0 && screenPos.y - rayon <= VIEWPORT_HEIGHT;
    }

    //-------------------------camera
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
    public void reset() {
        // Réinitialiser la caméra
        cameraX = 0;
        cameraY = 0;
        // Réinitialiser le mode de déplacement/récupération
        // (les autres variables booléennes si nécessaire)

        // Vider toutes les collections d'objets
        // Synchroniser toutes les opérations critiques
        synchronized(objetsMap) {
            objetsMap.clear();
        }
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


    //-----------------------zone de fonctionnement
    public void addDynamicZone(ZoneEnFonctionnement newZone) {dynamicZones.add(newZone);}
    public void removeDynamicZone(ZoneEnFonctionnement zone) {dynamicZones.remove(zone);}


    //-----------------------ajout d'objets
    //TODO :pop up pour dire qu'on ne peut plus récruter des unités
    //déterminer correctement les coordonnées de l'unitée qu'on embauche
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

        if(objet instanceof Ressource) {
            GameMaster.getInstance().addResource((Ressource) objet);
            GameMaster.getInstance().updateTargets();
        }

    }
    public void addUniteControlable(UniteControlable unite) {
        if (unitesEnJeu.size() >= MAX_UNITS_IN_GAME) {
            System.out.println("Cannot add more units. Maximum limit reached: " + MAX_UNITS_IN_GAME);
            return;
        }
        this.addObjet(unite);
        unitesEnJeu.add(unite);
    }
    public void addCollectedResource(model.objets.Ressource r) {collectedResources.add(r);}
    public void recrute(UniteControlable unite) {
       addUniteControlable(unite);
    }

    // Dans GamePanel.java
    public void refillOxygenForAll() {
        // On parcourt toutes les unités en jeu
        for (model.objets.UniteControlable unite : getUnitesEnJeu()) {
            if (unite instanceof model.unite_controlables.Plongeur) {
                // On remet l'oxygène au maximum (ici, 100 – ou utilisez une constante/méthode si vous préférez)
                ((model.unite_controlables.Plongeur) unite).setCurrentOxygen(100);
            }
        }
    }
    public boolean isBoardingMode() {
        return boardingMode;
    }

    public void setBoardingMode(boolean boardingMode) {
        this.boardingMode = boardingMode;
    }

    public SousMarin getTargetSubmarine() {
        return targetSubmarine;
    }

    public void setTargetSubmarine(SousMarin targetSubmarine) {
        this.targetSubmarine = targetSubmarine;
    }

    //-----------------------suppression d'objets
    public synchronized void removeObjet(Objet objet, CoordGrid coord) {
        CopyOnWriteArrayList<Objet> objetsAtCoord = objetsMap.get(coord);
        if (objetsAtCoord != null) {
            boolean removed = objetsAtCoord.remove(objet);
            if (removed) {
                // Déplacer l'appel à updateTargets avant la suppression
                if (objet instanceof Ressource) {
                    GameMaster.getInstance().updateTargets();
                }

                if (objetsAtCoord.isEmpty()) {
                    objetsMap.remove(coord);
                }
            }
            GameMaster.getInstance().updateTargets();
        }
    }
    public void removeCollectedResource(model.objets.Ressource r) {
        collectedResources.remove(r);
    }
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

        repaint();

    }


    //-------------------------------------------méthodes pour les panels------------------------------------------------


    //-------------------info panel
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
    public void showResourceInfoPanel(Ressource ressource) {
        System.out.println("Affichage des informations pour la ressource : " + ressource.getNom());
        setRessourceSelectionnee(ressource); // Mettre à jour la ressource sélectionnée
        slideInInfoPanel("resource");
        infoPanelUNC.updateInfo(ressource); // Mettre à jour les informations de la ressource
        infoPanelUNC.setVisible(true);
    }
    public void hideResourceInfoPanel() {
        infoPanelUNC.setVisible(false);
    }



    //------------------minimap
    public void hideMiniPanel() {
        CardLayout cl = (CardLayout) infoContainer.getLayout();
        cl.show(infoContainer, "empty");
    }


    //------------------info panel UNC
    public void checkAndClearResourcePanel(Ressource ressource) {
        if (ressourceSelectionnee != null && ressourceSelectionnee.equals(ressource)) {
            setRessourceSelectionnee(null);
            showEmptyInfoPanel();
        }
    }





    //------------------------------méthodes de dessin TEST-------------------------------------------------------

    void affichageTest(Graphics g){

        Map<Class<? extends Objet>, Color> colorMap = Map.of(
                Collier.class, Color.YELLOW,
                Plongeur.class, Color.BLUE,
                PlongeurArme.class, Color.RED,
                Pieuvre.class, Color.MAGENTA,
                Calamar.class, Color.RED,
                SousMarin.class, Color.GREEN,
                PieuvreBebe.class, Color.PINK,
                Bague.class, Color.CYAN,
                Tresor.class, Color.ORANGE,
                Coffre.class, Color.GRAY

        );

        Map<Class<? extends Objet>, ArrayList<Objet>> groupedObjects = new HashMap<>();
        for (Objet objet : objetsMap.values().stream().flatMap(CopyOnWriteArrayList::stream).toList()) {
            groupedObjects
                    .computeIfAbsent(objet.getClass(), k -> new ArrayList<>())
                    .add(objet);
        }


        //drawTerrainTest(g, getBounds());
        drawTerrainBackground(g);
        drawPronfondeur(g);
        drawTiles(g);
        drawEnemiesGrid(g);
        drawSpawnPointsTest(g);
        drawBaseTest(g);
        drawObjectsTest(g, getBounds(), colorMap, groupedObjects);
        drawAmmoTest(g);
        drawPlayerInfoTest(g);
        drawDetectionProximite(g);
        drawSubmarinesTest(g);

    }



    public void drawTerrainTest(Graphics g, Rectangle clip) {
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
        //rawDepthZones(g);
    }
    private void drawPlayerInfoTest(Graphics g) {
        g.setColor(Color.BLACK);
        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.BOLD, 16));
        g.drawString("Points: " + Referee.getInstance().getPointsVictoire(), 10, 40);
        g.drawString("Argent: " + Referee.getInstance().getArgentJoueur(), 10, 60);
        g.drawString("Unités: " + unitesEnJeu.size(), 10, 80);
    }
    private void drawPronfondeur(Graphics g){

        Map<Integer, Color> depthColors = Map.of(
                1, new Color(100, 200, 255, 80),  // Light Blue
                2, new Color(50, 150, 220, 100), // Medium Blue
                3, new Color(0, 100, 190, 120),  // Dark Blue
                4, new Color(0, 50, 150, 140)    // Very Dark Blue
        );

        for (int i = 0; i < terrain.getBackgroundDepthMap().length; i++) {
            for (int j = 0; j < terrain.getBackgroundDepthMap()[i].length; j++) {
                int depth = terrain.getBackgroundDepthMap()[i][j];
                Color color = depthColors.getOrDefault(depth, Color.BLACK);
                g.setColor(color);

                int screenX = i * terrain.getCubeWidth() - cameraX;
                int screenY = j * terrain.getCubeHeight() - cameraY;

                if (screenX + terrain.getCubeWidth()  > 0 && screenY +terrain.getCubeHeight()  > 0 &&
                        screenX < VIEWPORT_WIDTH && screenY < VIEWPORT_HEIGHT) {
                    g.fillRect(screenX, screenY, terrain.getCubeWidth() , terrain.getCubeHeight() );
                }
            }
        }

    }
    private void drawAmmoTest(Graphics g) {
        if(AmmoManager.getInstance().getActiveAmmo().isEmpty()) return;
        for (Ammo ammo : AmmoManager.getInstance().getActiveAmmo()) {
            Point screenPos = worldToScreen(ammo.getPosition().getX(), ammo.getPosition().getY());
            int diameter = ammo.getRayon() * 2;
            g.setColor(Color.RED); // Set the color for the ammo
            g.fillOval(screenPos.x - ammo.getRayon(), screenPos.y - ammo.getRayon(), diameter, diameter);
        }
    }
    private void drawSpawnPointsTest(Graphics g) {
        g.setColor(Color.ORANGE);
        for (EnemySpawnPoint spawnPoint : SpawnManager.getInstance().getSpawnPoints()) {
            Point screenPos = worldToScreen(spawnPoint.getPosition().getX(), spawnPoint.getPosition().getY());
            if (isVisibleInViewport(screenPos, spawnPoint.getRayon())) {
                int diameter = spawnPoint.getRayon() * 2;
                g.fillOval(screenPos.x - spawnPoint.getRayon(), screenPos.y - spawnPoint.getRayon(), diameter, diameter);
            }
        }
    }
    public void drawObjectsTest(Graphics g, Rectangle clip, Map<Class<? extends Objet>, Color> colorMap, Map<Class<? extends Objet>, ArrayList<Objet>> groupedObjects) {
        for (Map.Entry<Class<? extends Objet>, ArrayList<Objet>> entry : groupedObjects.entrySet()) {
            Color color = colorMap.get(entry.getKey());
            if (color != null) {
                g.setColor(color);
                for (Objet objet : entry.getValue()) {
                    Point screenPos = worldToScreen(objet.getPosition().getX(), objet.getPosition().getY());
                    int diametre = objet.getRayon() * 2;
                    if (isVisibleInViewport(screenPos, objet.getRayon())) {
                        g.fillOval(screenPos.x - objet.getRayon(), screenPos.y - objet.getRayon(), diametre, diametre);
                    }
                    if (objet instanceof Plongeur  && ((Plongeur)objet).isFaitFuire()) {
                        g.setColor(Color.ORANGE);
                        int rayonFuite = ((Plongeur) objet).getRayonFuite();
                        g.drawOval(screenPos.x - rayonFuite, screenPos.y - rayonFuite, rayonFuite * 2, rayonFuite * 2);
                    }
                    if (objet instanceof PlongeurArme plongeurArme && plongeurArme.isDefending()){
                        g.setColor(Color.GREEN);
                        Position center = plongeurArme.getDefendCircleCenter();
                        int radius = plongeurArme.getDefendCircleRadius();

                        g.setColor(Color.BLUE);
                        g.drawOval(screenPos.x - radius, screenPos.y - radius, radius * 2, radius * 2);
                    }

                }
            }
        }
    }
    private void drawDetectionProximite(Graphics g){
        for (UniteControlable unite : unitesEnJeu) {

            Point screenPos = worldToScreen(unite.getPosition().getX(), unite.getPosition().getY());
            if (isVisibleInViewport(screenPos, unite.getRayon())) {

                //getVoisins renvoie les coordonnées des 8 voisins de l'unité
                CopyOnWriteArrayList<Objet> voisins = proxy.getVoisins(unite);
                for (Objet voisin : voisins) {
                    Point uniteScreenPos = worldToScreen(unite.getPosition().getX(), unite.getPosition().getY());
                    Point voisinScreenPos = worldToScreen(voisin.getPosition().getX(), voisin.getPosition().getY());
                    g.setColor(Color.RED);
                    g.drawLine(uniteScreenPos.x, uniteScreenPos.y, voisinScreenPos.x, voisinScreenPos.y);

                }
            }

        }

    }
    private void drawBaseTest(Graphics g) {

        Point baseScreenPos = worldToScreen(baseUnique.getPosition().getX(), baseUnique.getPosition().getY());
        if (isVisibleInViewport(baseScreenPos, baseUnique.getRayon())) {
            g.setColor(Color.GREEN);
            Position[] coints = baseUnique.getCoints();
            Point topLeftScreenPos = worldToScreen(coints[0].getX(), coints[0].getY());

            g.fillRect(topLeftScreenPos.x, topLeftScreenPos.y, baseUnique.getLargeur(), baseUnique.getLongueur());

            g.setColor(Color.CYAN);
            g.fillOval(baseScreenPos.x - baseUnique.getRayon(),
                    baseScreenPos.y - baseUnique.getRayon(),
                    baseUnique.getRayon() * 2, baseUnique.getRayon() * 2);
        }

    }
    private void drawTiles(Graphics g) {
        g.setColor(Color.BLACK);

        for (int x = 0; x < TileManager.nbTilesWidth; x++) {
            for (int y = 0; y < TileManager.nbTilesHeight; y++) {
                int tileX = x * TileManager.TILESIZE;
                int tileY = y * TileManager.TILESIZE;

                int screenX = tileX - cameraX;
                int screenY = tileY - cameraY;

                if (screenX + TileManager.TILESIZE >= 0 && screenX <= VIEWPORT_WIDTH &&
                        screenY + TileManager.TILESIZE >= 0 && screenY <= VIEWPORT_HEIGHT) {
                    g.drawRect(screenX, screenY, TileManager.TILESIZE, TileManager.TILESIZE);


                }
            }
        }
    }
    private void drawEnemiesGrid(Graphics g) {
        g.setColor(Color.RED);

        // Dessiner les lignes verticales
        for (int x = 0; x <= TERRAIN_WIDTH; x += GameMaster.CELL_SIZE) {
            int screenX = x - cameraX;
            if (screenX >= 0 && screenX <= VIEWPORT_WIDTH) {
                g.drawLine(screenX, 0, screenX, VIEWPORT_HEIGHT);
            }
        }

        // Dessiner les lignes horizontales
        for (int y = 0; y <= TERRAIN_HEIGHT; y += GameMaster.CELL_SIZE) {
            int screenY = y - cameraY;
            if (screenY >= 0 && screenY <= VIEWPORT_HEIGHT) {
                g.drawLine(0, screenY, VIEWPORT_WIDTH, screenY);
            }
        }


    }

    private void drawSubmarinesTest(Graphics g) {
        // Parcourir la liste des unités en jeu
        for (model.objets.UniteControlable unite : getUnitesEnJeu()) {
            // Si l'unité est un SousMarin, la dessiner comme un carré
            if (unite instanceof model.unite_controlables.SousMarin) {
                int squareSize = 90; // Taille du carré (modifiable selon vos besoins)
                int x = (int)unite.getPosition().getX();
                int y = (int)unite.getPosition().getY();
                Point screenPos = worldToScreen(x, y);
                g.setColor(Color.ORANGE);

                g.fillRect(screenPos.x, screenPos.y, 20, 20);

                g.setColor(Color.ORANGE);

            }
        }
    }


    //------------------------------methodes de dessin JEU-------------------------------------------------------------

    void affichageJeu(Graphics g){

        // on calcule les parties de la carte visibles à l'aide des tiles
        int minGridX = Math.max(cameraX / TileManager.TILESIZE, 0);
        int maxGridX = Math.min((cameraX + VIEWPORT_WIDTH) / TileManager.TILESIZE, TileManager.nbTilesWidth - 1);
        int minGridY = Math.max(cameraY / TileManager.TILESIZE, 0);
        int maxGridY = Math.min((cameraY + VIEWPORT_HEIGHT) / TileManager.TILESIZE, TileManager.nbTilesHeight - 1);


        drawTerrainBackground(g);
        updatePlayerInfoPanel();
        drawSpawnPoints(g);
        drawAmmo(g);
        drawBase(g);
        drawObjects(g, minGridX, minGridY, maxGridX, maxGridY);


    }
    private void updatePlayerInfoPanel() {
        Referee referee = Referee.getInstance();
        int currentPoints = referee.getPointsVictoire();
        int pointsToWin = victoryManager != null ? victoryManager.getVictoryPoints() : 0;

        JLabel pointsValue = (JLabel) pointsLabel.getComponent(0);
        pointsValue.setText(currentPoints + " / " + pointsToWin);

        if (pointsToWin > 0) {
            double progress = (double) currentPoints / pointsToWin;
            if (progress >= 1.0) {
                pointsValue.setForeground(Color.GREEN); // Objectif atteint
            } else if (progress >= 0.75) {
                pointsValue.setForeground(Color.ORANGE); // Proche de l'objectif
            } else {
                pointsValue.setForeground(Color.BLACK); // Début de partie
            }
        }

        ((JLabel) moneyLabel.getComponent(0)).setText(String.valueOf(referee.getArgentJoueur()));
        ((JLabel) unitsLabel.getComponent(0)).setText(String.valueOf(unitesEnJeu.size()));
    }
    private static Font loadCustomFont() {
        try {
            Font font = Font.createFont(Font.TRUETYPE_FONT, Objects.requireNonNull(GamePanel.class.getResourceAsStream("/view/fonts/Gelio Pasteli.ttf")));
            return font.deriveFont(Font.BOLD, 12); // Set size and style
        } catch (FontFormatException | IOException e) {
            e.printStackTrace();
            return new Font("Arial", Font.BOLD, 16); // Fallback font
        }
    }

    public void drawObjects(Graphics g, int minGridX,int minGridY, int maxGridX, int maxGridY ) {
        /*for (int gridX = minGridX; gridX <= maxGridX; gridX++) {
            for (int gridY = minGridY; gridY <= maxGridY; gridY++) {
                CoordGrid gridCoord = new CoordGrid(gridX, gridY);
                CopyOnWriteArrayList<Objet> objetsInCell = objetsMap.get(gridCoord);
                if (objetsInCell != null) {
                    for (Objet objet : objetsMap.) {
                        Point screenPos = worldToScreen(objet.getPosition().getX(), objet.getPosition().getY());

                            Image image = objet.getImage(); // Assuming getImage() returns the unit's image
                            if (image != null) {
                                g.drawImage(image, screenPos.x - objet.getRayon(), screenPos.y - objet.getRayon(),
                                        objet.getRayon() * 2, objet.getRayon() * 2, null);
                            }
                            int diametre = objet.getRayon() * 2;
                            g.fillOval(screenPos.x - objet.getRayon(), screenPos.y - objet.getRayon(), diametre, diametre);


                    }
                }
            }
        }*/
        for (Objet objet : objetsMap.values().stream().flatMap(CopyOnWriteArrayList::stream).toList()) {
            Point screenPos = worldToScreen(objet.getPosition().getX(), objet.getPosition().getY());

            Graphics2D g2d = (Graphics2D) g.create();

            objet.draw((Graphics2D) g, screenPos);
            g2d.dispose();


        }
    }
    public void drawBase(Graphics g) {
        Point baseScreenPos = worldToScreen(baseUnique.getPosition().getX(), baseUnique.getPosition().getY());
        if (isVisibleInViewport(baseScreenPos, baseUnique.getRayon())) {
            Position[] corners = baseUnique.getCoints();
            Position topLeft = corners[0];
            Position bottomRight = corners[3];

            Point topLeftScreenPos = worldToScreen(topLeft.getX(), topLeft.getY());
            Point bottomRightScreenPos = worldToScreen(bottomRight.getX(), bottomRight.getY());
            int width = bottomRightScreenPos.x - topLeftScreenPos.x;
            int height = bottomRightScreenPos.y - topLeftScreenPos.y;

            Image perimeterImage = GamePanel.getCachedImage("basePerimeter.png");
            if (perimeterImage != null) {
                g.drawImage(perimeterImage, topLeftScreenPos.x - baseUnique.getLongueur()/2, topLeftScreenPos.y - baseUnique.getLargeur()/2, width*2, height*2, null);
            }

            baseUnique.draw((Graphics2D) g, baseScreenPos);

        }
    }

    private void drawSpawnPoints(Graphics g) {
        for (EnemySpawnPoint spawnPoint : SpawnManager.getInstance().getEpicSpawnPoints()) {
            Point screenPos = worldToScreen(spawnPoint.getPosition().getX(), spawnPoint.getPosition().getY());
            if (isVisibleInViewport(screenPos, spawnPoint.getRayon())) {
                Graphics2D g2d = (Graphics2D) g.create();

                spawnPoint.draw((Graphics2D) g, screenPos);
                g2d.dispose();
            }
        }
    }

    private void drawAmmo(Graphics g) {
        if(AmmoManager.getInstance().getActiveAmmo().isEmpty()) return;
        for (Ammo ammo : AmmoManager.getInstance().getActiveAmmo()) {
            Point screenPos = worldToScreen(ammo.getPosition().getX(), ammo.getPosition().getY());

            Graphics2D g2d = (Graphics2D) g.create();
            g.setColor(Color.BLACK);
            g.fillOval(screenPos.x- ammo.getRayon(), screenPos.y- ammo.getRayon() , ammo.getRayon()*2, ammo.getRayon()*2);
            ammo.draw((Graphics2D) g, screenPos);
            g2d.dispose();
        }
    }


    private static final Map<String, Image> imageCache = new HashMap<>();

    public static Image getCachedImage(String imgPath) {
        if (imageCache.containsKey(imgPath)) {
            return imageCache.get(imgPath);
        } else {
            try {
                Image image = new ImageIcon(Objects.requireNonNull(GamePanel.class.getResource("/view/images/" + imgPath))).getImage();
                imageCache.put(imgPath, image);
                return image;
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                return null; // Fallback if the image cannot be loaded
            }
        }
    }

    private void drawTerrainBackground(Graphics g) {
        int minGridX = Math.max(cameraX / terrain.getCubeWidth(), 0);
        int maxGridX = Math.min((cameraX + VIEWPORT_WIDTH) / terrain.getCubeWidth(), terrain.getBackgroundDepthMap().length - 1);
        int minGridY = Math.max(cameraY / terrain.getCubeHeight(), 0);
        int maxGridY = Math.min((cameraY + VIEWPORT_HEIGHT) / terrain.getCubeHeight(), terrain.getBackgroundDepthMap()[0].length - 1);

        for (int gridX = minGridX; gridX <= maxGridX; gridX++) {
            for (int gridY = minGridY; gridY <= maxGridY; gridY++) {
                int depth = terrain.getBackgroundDepthMap()[gridX][gridY];
                Image backgroundImage = terrain.getBackgroundImageForDepth(depth);

                if (backgroundImage != null) {
                    int screenX = (gridX * terrain.getCubeWidth()) - cameraX;
                    int screenY = (gridY * terrain.getCubeHeight()) - cameraY;

                    g.drawImage(backgroundImage, screenX, screenY, terrain.getCubeWidth(), terrain.getCubeHeight(), null);
                }
            }
        }
    }




    //------------------------------fonction principale-------------------------------------------------------
    @Override
    protected synchronized void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Dessin sur le backbuffer
        //renderToBackBuffer();



        //affichageTest(g);
        affichageJeu(g);

        if (selectionClic != null) {
            selectionClic.paintSelection(g);
        }
    }
}