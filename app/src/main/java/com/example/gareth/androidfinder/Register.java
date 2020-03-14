package com.example.gareth.androidfinder;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Register extends AppCompatActivity {


    protected EditText username;
    protected EditText email;
    protected EditText password;
    protected EditText confirmpassword;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        FirebaseApp.initializeApp(this);

        mAuth = FirebaseAuth.getInstance();


        Button registerbutton = (Button) findViewById(R.id.buttonRegister);
        username = (EditText) findViewById(R.id.EditTextNewUsername);
        email = (EditText) findViewById(R.id.editTextNewEmail);
        password = (EditText) findViewById(R.id.editTextNewPassword);
        confirmpassword = (EditText) findViewById(R.id.editTextConfirmPassword);

        registerbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(password.getText().toString().equals(confirmpassword.getText().toString()) && !username.getText().toString().matches("")){
                    mAuth.createUserWithEmailAndPassword(email.getText().toString(), password.getText().toString())
                            .addOnCompleteListener(Register.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        // Sign in success, update UI with the signed-in user's information
                                        Log.d("successful login", "createUserWithEmail:success");
                                        FirebaseUser user = mAuth.getCurrentUser();

                                        FirebaseDatabase database = FirebaseDatabase.getInstance();
                                        DatabaseReference usersRef = database.getReference("users");

                                        usersRef.child(user.getUid()).setValue(new User(username.getText().toString(),user.getUid()));

                                        startActivity(new Intent(Register.this, MapsActivity.class));

                                    } else {
                                        // If sign in fails, display a message to the user.
                                        Log.w("unsuccessful login", "createUserWithEmail:failure", task.getException());
                                        Toast.makeText(Register.this, "Authentication failed.",
                                                Toast.LENGTH_SHORT).show();
                                        //updateUI(null);
                                    }

                                    // ...
                                }
                            });
                }else{
                    Toast.makeText(Register.this, "Please make sure that your passwords match and you have entered a username",
                            Toast.LENGTH_LONG).show();
                }
            }
        });


        //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);

        /*FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        //updateUI(currentUser);
    }

}
