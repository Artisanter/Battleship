package com.artisanter.battleship;

import android.app.Application;

import com.google.firebase.auth.FirebaseAuth;

public class App extends Application {
    private FirebaseAuth mAuth;
    static private App instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        mAuth = FirebaseAuth.getInstance();
    }

    static public App getInstance(){
        return instance;
    }

    public FirebaseAuth getAuth(){
        return mAuth;
    }

}
