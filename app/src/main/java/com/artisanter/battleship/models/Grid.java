package com.artisanter.battleship.models;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Grid implements Serializable {
    private Cell[][] cells;
    private ArrayList<Ship> ships;
    private Ship highlightedShip;
    private Cell highlightedCell;
    private int destroyedShips = 0;

    public Grid(){
        ships = new ArrayList<>();
        cells = new Cell[SIZE][SIZE];
        for (int i = 0; i < SIZE; i++)
            for (int j = 0; j < SIZE; j++) {
                cells[i][j] = new Cell();
                cells[i][j].x = j;
                cells[i][j].y = i;
            }
    }

    public boolean hit(int x, int y){
        Cell cell = cells[y][x];

        if(cell.isHit)
            return false;

        cell.isHit = true;
        if(cell.ship == null)
            return false;

        if(cell.ship.isDestroyed()){
            destroyedShips++;
            Cell[] ship_cells = cell.ship.getCells();

            int x1 = ship_cells[0].x;
            int y1 = ship_cells[0].y;
            int x2 = ship_cells[ship_cells.length - 1].x;
            int y2 = ship_cells[ship_cells.length - 1].y;

            int x_left = x1 == 0 ? 0 : x1 - 1;
            int x_right = x2 == SIZE - 1 ? SIZE - 1 : x2 + 1;
            int y_top = y1 == 0 ? 0 : y1 - 1;
            int y_bottom = y2 == SIZE - 1 ? SIZE - 1 : y2 + 1;

            for(int i = y_top; i <= y_bottom; i++)
                for(int j = x_left; j <= x_right; j++)
                    cells[i][j].isHit = true;
        }
        return true;
    }

    public boolean putShip(ShipMask mask){
        return putShip(mask.x1, mask.y1, mask.x2, mask.y2);
    }

    public void putShipOrHighlight(int x, int y){
        if(highlightedCell == null || !putShip(x, y))
            highlight(x, y, true);
    }

    public boolean putShip(int x, int y){
        if(highlightedCell == null)
            return false;

        int x1 = Math.min(x, highlightedCell.x);
        int x2 = Math.max(x, highlightedCell.x);
        int y1 = Math.min(y, highlightedCell.y);
        int y2 = Math.max(y, highlightedCell.y);
        return putShip(x1, y1, x2, y2);
    }

    public boolean putShip(int x1, int y1, int x2, int y2){
        if(x1 > x2 || y1 > y2 || (x1 != x2 && y1 != y2))
            return false;
        int size = Math.max(x2 - x1, y2 - y1) + 1;

        if(size > MAX_SHIP_SIZE || ship_count[size] >= SHIP_MAX_COUNT[size])
            return false;


        int y_top = y1 == 0 ? 0 : y1 - 1;
        int y_bottom = y2 == SIZE - 1 ? SIZE - 1 : y2 + 1;
        int x_left = x1 == 0 ? 0 : x1 - 1;
        int x_right = x2 == SIZE - 1 ? SIZE - 1 : x2 + 1;

        for(int x = x_left; x <= x_right; x++) {
            for(int y = y_top; y <= y_bottom; y++) {
                if (cells[y][x].ship != null)
                    return false;
            }
        }

        cancelHighlight();

        if(y1 == y2){
            highlightedShip = new Ship(Arrays.copyOfRange(cells[y1], x1, x2 + 1));
        }
        else {
            Cell[] ship_cells = new Cell[size];
            for(int i = 0; i < size; i++)
                ship_cells[i] = cells[y1 + i][x1];
            highlightedShip = new Ship(ship_cells);
        }
        highlightedShip.setHighlight(true);
        ships.add(highlightedShip);
        ship_count[size]++;
        return true;
    }

    public void cancelHighlight(){
        if(highlightedShip != null) {
            highlightedShip.setHighlight(false);
            highlightedShip = null;
        }
        if(highlightedCell != null) {
            highlightedCell.isHighlighted = false;
            highlightedCell = null;
        }
    }

    public void highlight(int x, int y, boolean highlight){
        cancelHighlight();

        if(cells[y][x].ship == null) {
            cells[y][x].isHighlighted = highlight;
            if(highlight)
                highlightedCell = cells[y][x];
        }
        else {
            cells[y][x].ship.setHighlight(highlight);
            if(highlight)
                highlightedShip = cells[y][x].ship;
        }
    }

    public boolean removeHighlightedShip(){
        if(highlightedShip == null)
            return false;
        for(Cell cell: highlightedShip.getCells()) {
            cell.ship = null;
            cell.isHighlighted = false;
        }

        ship_count[highlightedShip.getSize()]--;
        return ships.remove(highlightedShip);
    }

    public void clearShips(){
        for (int i = 0; i < SIZE; i++)
            for (int j = 0; j < SIZE; j++) {
                cells[i][j].ship = null;
                cells[i][j].isHighlighted = false;
            }
        ship_count = new int[]{0, 0, 0, 0, 0};
        ships.clear();
    }

    public int getRemainingShips(){
        return ships.size() - destroyedShips;
    }

    public int getAvailableShips(){
        int sum = 0;
        for (int i = 1; i <= MAX_SHIP_SIZE; i++)
            sum += getAvailableShips(i);
        return sum;
    }

    public int getAvailableShips(int size){
        return SHIP_MAX_COUNT[size] - ship_count[size];
    }

    public ArrayList<ArrayList<CellMask>>  getMask(){
        return getMask(true);
    }

    public ArrayList<ArrayList<CellMask>>  getMask(boolean includeShips){
        ArrayList<ArrayList<CellMask>> gridMask = new ArrayList<>(SIZE);
        for (int i = 0; i < SIZE; i++) {
            gridMask.add(new ArrayList<>(SIZE));
            for (int j = 0; j < SIZE; j++) {
                CellMask mask = new CellMask();
                mask.hasShip = includeShips && cells[i][j].ship != null;
                mask.isHighlighted = cells[i][j].isHighlighted;
                mask.isHit = cells[i][j].isHit;
                gridMask.get(i).add(mask);
            }
        }
        return gridMask;
    }

    public List<ShipMask> getShipMasks(){
        List<ShipMask> masks = new ArrayList<>();
        for(Ship ship : ships)
            masks.add(ship.getMask());
        return masks;
    }

    private OnGridChangeListener onGridChangeListener;

    public OnGridChangeListener getOnGridChangeListener() {
        return onGridChangeListener;
    }

    public void setOnGridChangeListener(OnGridChangeListener onGridChangeListener) {
        this.onGridChangeListener = onGridChangeListener;
    }


    public static final int SIZE = 10;
    public static final int MAX_SHIP_SIZE = 4;
    private static final int[] SHIP_MAX_COUNT = new int[]{10, 4, 3, 2, 1};
    private int[] ship_count = new int[]{0, 0, 0, 0, 0};
}
