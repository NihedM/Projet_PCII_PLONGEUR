package controler;

import model.objets.CoordGrid;
import model.objets.Position;
import view.GamePanel;

public class TileManager {


    public static final int TILESIZE = 100; // Taille d'une tile constante
    public static final int nbTilesWidth = GamePanel.TERRAIN_WIDTH / TILESIZE;
    public static final int nbTilesHeight = GamePanel.TERRAIN_HEIGHT / TILESIZE;

    private static CoordGrid[][] coordTiles = new CoordGrid[nbTilesWidth][nbTilesHeight];

    static {
        for (int i = 0; i < nbTilesWidth; i++) {
            for (int j = 0; j < nbTilesHeight; j++) {
                coordTiles[i][j] = new CoordGrid(i, j);
            }
        }
    }

    public static int transformeP_to_grid(int x) {
        return x / TILESIZE;
    }

    public static CoordGrid transformePos_to_Coord(Position pos) {
        //print dimentions
        //System.out.println("nbTilesWidth: " + nbTilesWidth + " nbTilesHeight: " + nbTilesHeight);
        //System.out.println("x: " + pos.getX() + " y: " + pos.getY());
        //System.out.println("x: " + transformeP_to_grid(pos.getX()) + " y: " + transformeP_to_grid(pos.getY()));
        return coordTiles[transformeP_to_grid(pos.getX())][transformeP_to_grid(pos.getY())];
    }

    public static CoordGrid getCoordTile(int x, int y) {
        if (x >= 0 && x < nbTilesWidth && y >= 0 && y < nbTilesHeight) {
            return coordTiles[x][y];
        }
        return null;
    }
}