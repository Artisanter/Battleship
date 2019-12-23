package com.artisanter.battleship.models;

import java.io.Serializable;

public class CellMask implements Serializable {
    public boolean hasShip = false;
    public boolean isHit = false;
    public boolean isHighlighted = false;
}
