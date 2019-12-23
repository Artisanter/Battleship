package com.artisanter.battleship;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.util.Strings;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.squareup.picasso.Picasso;

import java.util.Objects;

public class UserActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    private ImageView mUserImage;
    private TextView mUserName;
    private EditText mEditPassword;
    private EditText mEditEmail;
    private LinearLayout mLoginLayout;
    private LinearLayout mUserLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        mAuth = App.getInstance().getAuth();

        mUserName = findViewById(R.id.user_name);
        mUserImage = findViewById(R.id.user_image);
        mEditEmail = findViewById(R.id.email);
        mEditPassword = findViewById(R.id.password);

        mLoginLayout = findViewById(R.id.login);
        mUserLayout = findViewById(R.id.user_layout);

        GoogleSignInOptions gso = new GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        findViewById(R.id.register_button).setOnClickListener((v)->{
            String email = mEditEmail.getText().toString();
            String password = mEditPassword.getText().toString();
            if(Strings.isEmptyOrWhitespace(email) || Strings.isEmptyOrWhitespace(password))
                return;

            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener((task)->{
                if(task.isSuccessful()){
                    setUser(mAuth.getCurrentUser());
                }else {
                    Toast.makeText(this, "Ошибка регистрации", Toast.LENGTH_SHORT)
                            .show();
                }
            });
        });

        findViewById(R.id.login_button).setOnClickListener((v)->{
            String email = mEditEmail.getText().toString();
            String password = mEditPassword.getText().toString();
            if(Strings.isEmptyOrWhitespace(email) || Strings.isEmptyOrWhitespace(password))
                return;

            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener((task)->{
                if(task.isSuccessful()){
                    setUser(mAuth.getCurrentUser());
                }else {
                    Toast.makeText(this, "Ошибка входа", Toast.LENGTH_SHORT)
                            .show();
                }
            });
        });

        findViewById(R.id.logout_button).setOnClickListener((v) -> {
            mAuth.signOut();
            mLoginLayout.setVisibility(View.VISIBLE);
            mUserLayout.setVisibility(View.GONE);
        });

        findViewById(R.id.google_signin).setOnClickListener((v) -> {
            Intent signInIntent = GoogleSignIn.getClient(this, gso).getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        });
        setUser(mAuth.getCurrentUser());
    }

    private void setUser(FirebaseUser user) {
        if (user == null)
            return;
        mUserName.setText(user.getEmail());
        Uri uri = user.getPhotoUrl();
        if (uri != null)
            Picasso.get().load(uri).into(mUserImage);
        mLoginLayout.setVisibility(View.GONE);
        mUserLayout.setVisibility(View.VISIBLE);
    }

    private final int RC_SIGN_IN = 123;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(Objects.requireNonNull(account));
            } catch (ApiException e) {
                Toast.makeText(this, "Ошибка входа", Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }


    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        setUser(user);
                    } else {
                        Toast.makeText(this, "Ошибка входа", Toast.LENGTH_SHORT)
                                .show();
                    }
                });
    }

}
