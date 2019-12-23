package com.artisanter.battleship.game;

import android.widget.Toast;

import androidx.annotation.NonNull;

import com.artisanter.battleship.App;
import com.artisanter.battleship.models.CellMask;
import com.artisanter.battleship.models.Grid;
import com.artisanter.battleship.models.ShipMask;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class ClientGame extends Game {
    private DatabaseReference gameDb;
    private ArrayList<ArrayList<CellMask>> userMask;
    private ArrayList<ArrayList<CellMask>> hostMask;
    private List<ShipMask> shipMasks;
    private DatabaseReference db;

    @Override
    public ArrayList<ArrayList<CellMask>> getUserCells() {
        return userMask;
    }

    @Override
    public ArrayList<ArrayList<CellMask>> getOpponentCells() {
        return hostMask;
    }

    @Override
    public void start() {

        if(isReady())
            gameDb.child(CLIENT_SHIPS).setValue(shipMasks).addOnCompleteListener(task -> {
                if(task.isSuccessful())
                    gameDb.child(IS_CLIENT_READY).setValue(true).addOnCompleteListener((l_task)-> {
                                if (!l_task.isSuccessful() && getOnErrorListener() != null) {
                                    getOnErrorListener().onError("Client cant write");
                                }

                                if (getStatus() == Status.Waiting)
                                    gameDb.child(STATUS).setValue(Status.HostTurn.toString());
                            }
                    );
            });
    }

    @Override
    public void setUserGrid(Grid grid) {
        grid.cancelHighlight();
        userMask = grid.getMask();
        shipMasks = grid.getShipMasks();
    }

    @Override
    public void hitOpponent(int x, int y) {
        gameDb.child(LAST_HIT).setValue(String.valueOf(y * Grid.SIZE + x))
                .addOnCompleteListener(task ->{
            if(!task.isSuccessful())
                getOnErrorListener().onError("Ошибка соединения");
        });
    }

    @Override
    public boolean isMyTurn() {
        return getStatus() == Status.ClientTurn;
    }

    @Override
    public boolean isOpponentTurn() {
        return getStatus() == Status.HostTurn;
    }

    @Override
    public boolean isWinner() {
        return getStatus() == Status.ClientWon;
    }

    public ClientGame(){
        db = FirebaseDatabase.getInstance().getReference();
        userMask = new Grid().getMask();
        hostMask = new Grid().getMask();
    }

    public void joinGame(String id){
        this.id = id;

        gameDb = db.child(HOSTED_GAMES).child(id);
        gameDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String value = dataSnapshot.child(STATUS).getValue(String.class);
                if(!dataSnapshot.exists()){
                    setStatus(Status.OnError);
                    return;
                }

                if(value == null || (Status.valueOf(value) != Status.Created)){
                    if(getOnErrorListener() != null)
                        getOnErrorListener().onError("Status = " + value);
                    return;

                }

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

                gameDb.child(HOST_GRID).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        ArrayList<ArrayList<CellMask>> value = dataSnapshot.getValue(
                                new GenericTypeIndicator<ArrayList<ArrayList<CellMask>>>() {});
                        if(value != null) {
                            hostMask = value;
                            onLayoutChanged();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        if(getOnErrorListener() != null)
                            getOnErrorListener().onError("Отмена");
                    }
                });


                gameDb.child(CLIENT_GRID).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        ArrayList<ArrayList<CellMask>> value = dataSnapshot.getValue(
                                new GenericTypeIndicator<ArrayList<ArrayList<CellMask>>>() {});
                        if(value != null) {
                            userMask = value;
                            onLayoutChanged();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        if(getOnErrorListener() != null)
                            getOnErrorListener().onError("Отмена");
                    }
                });

                gameDb.child(STATUS).setValue(Status.Connected.toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                if(getOnErrorListener() != null)
                    getOnErrorListener().onError("Отмена");
            }
        });
    }
}
