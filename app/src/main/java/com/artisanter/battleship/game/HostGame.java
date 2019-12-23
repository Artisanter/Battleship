package com.artisanter.battleship.game;

import androidx.annotation.NonNull;

import com.artisanter.battleship.models.CellMask;
import com.artisanter.battleship.models.Grid;
import com.artisanter.battleship.models.ShipMask;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Objects;

public class HostGame extends Game {
    private DatabaseReference gameDb;
    private Grid userGrid;
    private Grid opponentGrid;
    private DatabaseReference db;
    private int lastHit = -1;

    public HostGame(){
        db = FirebaseDatabase.getInstance().getReference();
        userGrid = new Grid();
        opponentGrid = new Grid();
    }

    @Override
    public boolean isMyTurn() {
        return getStatus() == Status.HostTurn;
    }

    @Override
    public boolean isOpponentTurn() {
        return getStatus() == Status.ClientTurn;
    }

    private void hitUser(int x, int y){
        if(userGrid.hit(x, y)){
            if(userGrid.getRemainingShips() == 0)
                gameDb.child(STATUS).setValue(Status.ClientWon.toString());
            else
                gameDb.child(STATUS).setValue(Status.ClientTurn.toString());
        } else{
            gameDb.child(STATUS).setValue(Status.HostTurn.toString());
        }
        gameDb.child(HOST_GRID).setValue(userGrid.getMask());
        onLayoutChanged();


    }

    public void hitOpponent(int x, int y){
        if(opponentGrid.hit(x, y)){
            if(opponentGrid.getRemainingShips() == 0)
                gameDb.child(STATUS).setValue(Status.HostWon.toString());
            else
                gameDb.child(STATUS).setValue(Status.HostTurn.toString());
        } else{
            gameDb.child(STATUS).setValue(Status.ClientTurn.toString());
        }
        gameDb.child(CLIENT_GRID).setValue(opponentGrid.getMask());
        onLayoutChanged();
    }

    public void setUserGrid(Grid grid){
        userGrid = grid;
    }

    static private boolean setShips(Collection<ShipMask> ship_masks, Grid grid){
        grid.clearShips();
        for(ShipMask mask : ship_masks){
            if(!grid.putShip(mask)){
                grid.clearShips();
                return false;
            }
        }
        return true;
    }

    public void createGame(){
        id = String.valueOf(new Date().getTime() % 10000);
        gameDb = db.child(HOSTED_GAMES).child(getId());
        gameDb.child(STATUS).setValue(Game.Status.Created.toString());

        gameDb.child(STATUS).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String value = dataSnapshot.getValue(String.class);
                if(value == null)
                    return;
                setStatus(Status.valueOf(value));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                if(getOnErrorListener() != null)
                    getOnErrorListener().onError("Отмена");
            }
        });

        gameDb.child(IS_CLIENT_READY).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Object value = dataSnapshot.getValue();
                setOpponentReady(value != null && (boolean)value);
                if(isOpponentReady()){
                    gameDb.child(CLIENT_SHIPS).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            opponentGrid.clearShips();
                            for(DataSnapshot snapshot: dataSnapshot.getChildren())
                                opponentGrid.putShip(Objects.requireNonNull(snapshot.getValue(ShipMask.class)));
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            if(getOnErrorListener() != null)
                                getOnErrorListener().onError("Отмена");
                        }
                    });
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });

        gameDb.child(LAST_HIT).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String value = dataSnapshot.getValue(String.class);
                if(value == null)
                    return;
                int hit = Integer.valueOf(value);
                if(hit == lastHit)
                    return;
                lastHit = hit;
                hitUser(hit % Grid.SIZE, hit / Grid.SIZE);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

    @Override
    public ArrayList<ArrayList<CellMask>> getUserCells() {
        return userGrid.getMask();
    }

    @Override
    public ArrayList<ArrayList<CellMask>> getOpponentCells() {
        return opponentGrid.getMask();
    }

    @Override
    public void start()
    {
        userGrid.cancelHighlight();
        opponentGrid.cancelHighlight();
        if(!isReady())
            return;
        if(isOpponentReady())
            gameDb.child(STATUS).setValue(Status.HostTurn.toString());
        else
            gameDb.child(STATUS).setValue(Status.Waiting.toString());

    }

    @Override
    public boolean isWinner() {
        return getStatus() == Status.HostWon;
    }

}
