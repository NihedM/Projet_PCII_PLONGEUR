package controler;

import model.objets.CoordGrid;
import model.objets.Position;
import view.GamePanel;

public class TileManager {


    public static final int TILESIZE = GamePanel.PANELDIMENSION / 4; // Taille d'une tile
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
        return coordTiles[transformeP_to_grid(pos.getX())][transformeP_to_grid(pos.getY())];
    }

    public static CoordGrid getCoordTile(int x, int y) {
        if (x >= 0 && x < nbTilesWidth && y >= 0 && y < nbTilesHeight) {
            return coordTiles[x][y];
        }
        return null;
    }
}