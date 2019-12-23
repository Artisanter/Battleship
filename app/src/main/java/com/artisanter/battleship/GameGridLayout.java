package com.artisanter.battleship;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridLayout;
import android.widget.ImageView;

import com.artisanter.battleship.models.*;

import java.util.ArrayList;

import static com.artisanter.battleship.models.Grid.SIZE;

public class GameGridLayout extends GridLayout {
    public GameGridLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        setup();
    }

    public void setup(){
        setColumnCount(SIZE);
        setRowCount(SIZE);
        removeAllViews();


        for (int i = 0; i < SIZE; i++)
            for (int j = 0; j < SIZE; j++) {
                ImageView view = new ImageView(getContext(), null, R.style.cell);
                LayoutParams lp = new GridLayout.LayoutParams();
                lp.height = 0;
                lp.width = 0;
                lp.columnSpec = GridLayout.spec(i, GridLayout.FILL, 1f);
                lp.rowSpec = GridLayout.spec(j, GridLayout.FILL, 1f);
                view.setLayoutParams(lp);

                final int x = i;
                final int y = j;
                view.setOnClickListener(v -> {
                    if(onCellClickListener != null)
                        onCellClickListener.onCellClick(x, y);
                });
                addView(view);
            }
    }

    public void setGrid(ArrayList<ArrayList<CellMask>>  mask){
        setGrid(mask, true);
    }

    public void setGrid(ArrayList<ArrayList<CellMask>>  mask, boolean showShips){
        if(mask == null)
            return;
        for (int i = 0; i < SIZE; i++)
            for (int j = 0; j < SIZE; j++) {
                CellMask cellMask = mask.get(i).get(j);

                ImageView view = getCell(i, j);
                if(cellMask.isHighlighted)
                    view.setImageResource(R.drawable.ic_checked);
                else
                    view.setImageResource(0);
                if(showShips) {
                    if (cellMask.hasShip) {
                        view.setBackgroundResource(R.drawable.ship_cell);
                        if (cellMask.isHit)
                            view.setImageResource(R.drawable.ic_blast);
                    } else {
                        view.setBackgroundResource(R.drawable.sea_cell);
                        if (cellMask.isHit)
                            view.setImageResource(R.drawable.ic_cross);
                    }
                }else{
                    if (cellMask.hasShip && cellMask.isHit) {
                        view.setBackgroundResource(R.drawable.ship_cell);
                        view.setImageResource(R.drawable.ic_blast);
                    } else {
                        view.setBackgroundResource(R.drawable.sea_cell);
                        if (cellMask.isHit)
                            view.setImageResource(R.drawable.ic_cross);
                    }
                }
            }

    }

    public ImageView getCell(int x, int y){
        return (ImageView) getChildAt(y * SIZE + x);
    }

    private OnCellClickListener onCellClickListener;

    public void setOnCellClickListener(OnCellClickListener onCellClickListener) {
        this.onCellClickListener = onCellClickListener;
    }
}
