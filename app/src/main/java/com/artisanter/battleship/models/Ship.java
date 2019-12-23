package com.artisanter.battleship.models;

import java.io.Serializable;

public class Ship implements Serializable {
    private Cell[] cells;


    Ship(Cell[] cells){
        for(Cell cell : cells)
            cell.ship = this;

        this.cells = cells;

    }

    Cell[] getCells(){
        return cells;
    }

    public int getSize(){
        return cells.length;
    }

    public boolean isDestroyed(){
        for (Cell cell : cells)
            if(!cell.isHit)
                return false;
        return true;
    }

    public void setHighlight(boolean highlight) {
        for (Cell cell : cells)
            cell.isHighlighted = highlight;
    }

    public ShipMask getMask(){
        ShipMask mask = new ShipMask();
        mask.x1 = cells[0].x;
        mask.y1 = cells[0].y;
        mask.x2 = cells[cells.length - 1].x;
        mask.y2 = cells[cells.length - 1].y;
        return mask;
    }
}
