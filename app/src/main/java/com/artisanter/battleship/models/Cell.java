package com.artisanter.battleship.models;

import java.io.Serializable;

public class Cell implements Serializable {
    public Ship ship = null;
    public boolean isHit = false;
    public boolean isHighlighted = false;

    int x;
    int y;
}

