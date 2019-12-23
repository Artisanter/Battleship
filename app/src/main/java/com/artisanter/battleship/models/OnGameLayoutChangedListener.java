package com.artisanter.battleship.models;

import java.util.ArrayList;

public interface OnGameLayoutChangedListener {
    void onGameLayoutChanged(ArrayList<ArrayList<CellMask>> userMask
            , ArrayList<ArrayList<CellMask>> opponentMask);
}
