package com.artisanter.battleship;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.artisanter.battleship.game.Game;
import com.artisanter.battleship.models.CellMask;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class GameActivity extends AppCompatActivity {
    private GameGridLayout mUserGrid;
    private GameGridLayout mOpponentGrid;
    private TextView mStatusView;
    private Game mGame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        mStatusView = findViewById(R.id.status_text);
        mUserGrid = findViewById(R.id.my_grid);
        mOpponentGrid = findViewById(R.id.opponent_grid);

        mUserGrid.setEnabled(false);
        mOpponentGrid.setOnCellClickListener((x, y) -> {
            CellMask mask = mGame.getOpponentCells().get(y).get(x);
            if(mask.isHit)
                return;
            if(mGame.isMyTurn()) {
                mOpponentGrid.setEnabled(false);
                mGame.hitOpponent(x, y);

                ImageView cellView = mOpponentGrid.getCell(y, x);
                if (mask.hasShip){
                    cellView.setBackgroundResource(R.drawable.ship_cell);
                    cellView.setImageResource(R.drawable.ic_blast);
                }
                else
                    cellView.setImageResource(R.drawable.ic_cross);


            }
        });

        mGame = Game.getInstance();
        mGame.setLayoutChangedListener(this::refresh);

        mGame.setOnStatusChanged((status) -> {
            if(mGame.isMyTurn()) {
                mStatusView.setText("Ваш ход");
                mOpponentGrid.setEnabled(true);
            }
            else if(mGame.isOpponentTurn()){
                mStatusView.setText("Ход противника");
                mOpponentGrid.setEnabled(false);
            }
            else {
                mStatusView.setText("...");
                mOpponentGrid.setEnabled(false);
            }
        });


        mGame.setOnFinishedListener(won -> {
            TextView textView = findViewById(R.id.endgame_text);
            String result;
            if(won){
                result = "ПОБЕДА!!!";
            }
            else{
                result = "ПОРАЖЕНИЕ";
            }

            textView.setText(result);
            textView.setVisibility(View.VISIBLE);

            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if(user == null)
                return;
            FirebaseDatabase.getInstance()
                    .getReference("users/" + user.getUid() + "/history")
                    .push().setValue(mGame.getId() + "| "
                    + SimpleDateFormat.getDateInstance().format("dd.MM.yy HH:mm") + " "
                    + result);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh(mGame.getUserCells(), mGame.getOpponentCells());
        mGame.getOnStatusChanged().onStatusChanged(mGame.getStatus());
    }

    private void refresh(ArrayList<ArrayList<CellMask>> userMask
            , ArrayList<ArrayList<CellMask>> opponentMask){
        mUserGrid.setGrid(userMask);
        mOpponentGrid.setGrid(opponentMask, false);
    }


}
