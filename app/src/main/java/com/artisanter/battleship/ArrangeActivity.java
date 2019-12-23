package com.artisanter.battleship;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import com.artisanter.battleship.game.Game;
import com.artisanter.battleship.models.Grid;

public class ArrangeActivity extends AppCompatActivity {
    private CheckBox mReadyButton;
    private Button[] mCounters;
    private GameGridLayout mGameGridLayout;
    private Grid mGrid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_arrange);
        Game mGame = Game.getInstance();
        mReadyButton = findViewById(R.id.ready_button);
        mReadyButton.setOnCheckedChangeListener((v, isChecked) -> {
            mGame.setReady(isChecked);
            if(isChecked){
                mGrid.cancelHighlight();
                mGame.setUserGrid(mGrid);
                mGame.start();
            }
        });

        mCounters = new Button[Grid.MAX_SHIP_SIZE];
        mCounters[0] = findViewById(R.id.x1_counter);
        mCounters[1] = findViewById(R.id.x2_counter);
        mCounters[2] = findViewById(R.id.x3_counter);
        mCounters[3] = findViewById(R.id.x4_counter);

        mGrid = new Grid();
        mGameGridLayout = findViewById(R.id.my_grid);
        mGameGridLayout.setOnCellClickListener((x, y)->{
            mGrid.putShipOrHighlight(x, y);
            updateLayout();
        });

        ((TextView)findViewById(R.id.game_id))
                .setText(String.format("id: %s", Game.getInstance().getId()));

        findViewById(R.id.clear).setOnClickListener(v -> {
            mGrid.clearShips();
            updateLayout();
        });

        findViewById(R.id.remove).setOnClickListener(v -> {
            mGrid.removeHighlightedShip();
            mGrid.cancelHighlight();
            updateLayout();
        });

        mGame.setOnStatusChanged(status -> {
            if(status == Game.Status.HostTurn || status == Game.Status.ClientTurn){
                mGame.setOnStatusChanged(null);
                startActivity(new Intent(getApplicationContext(), GameActivity.class));
                finish();
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        updateLayout();
    }

    private void updateLayout(){
        setCounters();
        mGameGridLayout.setGrid(mGrid.getMask());
        if(mGrid.getAvailableShips() == 0){
            mReadyButton.setEnabled(true);
        }
        else {
            mReadyButton.setEnabled(false);
            mReadyButton.setChecked(false);
            if(Game.getInstance().isReady())
                Game.getInstance().setReady(false);
        }
    }

    private void setCounters(){
        for(int i = 0; i < Grid.MAX_SHIP_SIZE; i++)
            mCounters[i].setText(String.valueOf(mGrid.getAvailableShips(i + 1)));
    }
}
