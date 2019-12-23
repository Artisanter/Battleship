package com.artisanter.battleship.models;

import com.artisanter.battleship.game.Game;

public interface OnStatusChangedListener {
    void onStatusChanged(Game.Status status);
}
