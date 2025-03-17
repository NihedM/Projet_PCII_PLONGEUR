package controler;

import model.objets.CoordGrid;
import model.objets.Position;
import view.GamePanel;

public class TileManager {

    public static final int nbTiles = 20, TILESIZE = GamePanel.PANELDIMENSION / nbTiles;
    private GamePanel gamePanel;

    private static CoordGrid[][] coordTiles = new CoordGrid[nbTiles][nbTiles];

    static{
        for (int i = 0; i < nbTiles; i++) {
            for (int j = 0; j < nbTiles; j++) {
                coordTiles[i][j] =  new CoordGrid(i, j) ;
            }
        }
    }

    public TileManager(GamePanel gamePanel) {
        this.gamePanel = gamePanel;

    }






    public static int transformeP_to_grid(int x) {
        return x / TILESIZE;
    }


    public static CoordGrid transformePos_to_Coord(Position pos) {

        return coordTiles[transformeP_to_grid(pos.getX())][transformeP_to_grid(pos.getY())];

    }

    public static CoordGrid getCoordTile(int x, int y) {
        return coordTiles[x][y];
    }


}

