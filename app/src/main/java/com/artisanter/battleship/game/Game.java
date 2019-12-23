package com.artisanter.battleship.game;

import androidx.annotation.Nullable;

import com.artisanter.battleship.models.CellMask;
import com.artisanter.battleship.models.Grid;
import com.artisanter.battleship.models.OnErrorListener;
import com.artisanter.battleship.models.OnFinishedListener;
import com.artisanter.battleship.models.OnGameLayoutChangedListener;
import com.artisanter.battleship.models.OnStatusChangedListener;

import java.util.ArrayList;


public abstract class Game {
    protected String id;
    private boolean isReady;
    private boolean isOpponentReady = false;
    private Status status;
    private OnStatusChangedListener onStatusChanged;
    private OnErrorListener onErrorListener;
    private OnGameLayoutChangedListener layoutChangedListener;
    private OnFinishedListener onFinishedListener;

    public abstract ArrayList<ArrayList<CellMask>> getUserCells();
    public abstract ArrayList<ArrayList<CellMask>> getOpponentCells();
    public abstract void start();
    public abstract void setUserGrid(Grid grid);
    public abstract void hitOpponent(int x, int y);
    public abstract boolean isMyTurn();
    public abstract boolean isOpponentTurn();
    public abstract boolean isWinner();

    static private Game instance;

    protected Game(){
        status = Status.NotExist;
    }

    public static Game getInstance() {
        return instance;
    }

    public static void setInstance(Game instance) {
        Game.instance = instance;
    }


    public String getId(){
        return id;
    }

    public boolean isReady() {
        return isReady;
    }

    public void setReady(boolean ready) {
        isReady = ready;
    }

    public Status getStatus() {
        return status;
    }

    protected void setStatus(Status status) {

        this.status = status;
        if(onStatusChanged != null)
            onStatusChanged.onStatusChanged(getStatus());
        if((status == Status.ClientWon || status == Status.HostWon)
                && onFinishedListener != null)
            onFinishedListener.onFinish(isWinner());
    }

    public boolean isOpponentReady() {
        return isOpponentReady;
    }

    protected void setOpponentReady(boolean opponentReady) {
        isOpponentReady = opponentReady;
        if(isReady && isOpponentReady)
            start();
    }

    public OnStatusChangedListener getOnStatusChanged() {
        return onStatusChanged;
    }

    public void setOnStatusChanged(OnStatusChangedListener onStatusChanged) {
        this.onStatusChanged = onStatusChanged;
    }

    public void setOnErrorListener(OnErrorListener onErrorListener) {
        this.onErrorListener = onErrorListener;
    }

    public OnErrorListener getOnErrorListener() {
        return onErrorListener;
    }

    public void setLayoutChangedListener(OnGameLayoutChangedListener layoutChangedListener) {
        this.layoutChangedListener = layoutChangedListener;
    }

    public void setOnFinishedListener(OnFinishedListener onFinishedListener) {
        this.onFinishedListener = onFinishedListener;
    }

    public enum Status{
        OnError,
        NotExist,
        Created,
        Connected,
        Waiting,
        Started,
        HostTurn,
        ClientTurn,
        HostWon,
        ClientWon
    }

    protected void onLayoutChanged(){
        if(layoutChangedListener != null)
            layoutChangedListener.onGameLayoutChanged(getUserCells(), getOpponentCells());
    }

    final static String LAST_HIT = "last_hit";
    final static String IS_CLIENT_READY = "is_opponent_ready";
    final static String HOST_GRID = "hist_grid";
    final static String CLIENT_GRID = "client_grid";
    final static String CLIENT_SHIPS = "client_ships";
    final static String HOSTED_GAMES = "hosted_games";
    final static String STATUS = "status";
}
