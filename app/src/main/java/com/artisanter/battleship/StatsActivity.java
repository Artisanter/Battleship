package com.artisanter.battleship;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class StatsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        FirebaseUser user = App.getInstance().getAuth().getCurrentUser();

        ImageView imageView = findViewById(R.id.user_image);
        TextView textView = findViewById(R.id.user_name);
        ListView listView = findViewById(R.id.stats);

        final ArrayList<String> games = new ArrayList<>();


        assert user != null;
        FirebaseDatabase.getInstance()
                .getReference("users/" + user.getUid() + "/history")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for(DataSnapshot snapshot: dataSnapshot.getChildren())
                            games.add(snapshot.getValue(String.class));
                        listView.setAdapter(new ArrayAdapter<>(getApplicationContext()
                                , R.layout.support_simple_spinner_dropdown_item
                                , games
                        ));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
        Picasso.get().load(user.getPhotoUrl()).into(imageView);
        textView.setText(user.getEmail());
    }
}
