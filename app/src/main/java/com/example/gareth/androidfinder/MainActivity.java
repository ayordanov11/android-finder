package com.example.gareth.androidfinder;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Button button = (Button)findViewById(R.id.fakebutton);

        //button.setOnClickListener(new View.OnClickListener() {
           // @Override
           // public void onClick(View view) {
               // startTestActivity();
            //}
        //});

        FirebaseApp.initializeApp(this);

        mAuth = FirebaseAuth.getInstance();

        configureLoginButton();
        configureRegisterButton();
        configureLogoutButton();
        //configureMapButton();
    }

    private void configureLogoutButton() {
        Button logoutbutton = (Button) findViewById(R.id.btnLogout);

        logoutbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                try{
                    mAuth.signOut();

                    Toast.makeText(MainActivity.this, "You are now logged out",
                            Toast.LENGTH_SHORT).show();

                }catch(Exception ex){
                    Toast.makeText(MainActivity.this, "Logout failed",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void startTestActivity(){
        Intent intent = new Intent(this,LoginActivity.class);
        startActivity(intent);
    }

    private void configureLoginButton(){
        Button loginButton = (Button) findViewById(R.id.btnLogin);
        loginButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){

                if(mAuth.getCurrentUser() != null){
                    startActivity(new Intent(MainActivity.this, MapsActivity.class));
                }
                else
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
            }
        });
    }

    private void configureRegisterButton(){
        Button registerButton = (Button) findViewById(R.id.btnRegister);
        registerButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){

                if(mAuth.getCurrentUser() != null){
                    startActivity(new Intent(MainActivity.this, MapsActivity.class));

                }
                else
                    startActivity(new Intent(MainActivity.this, Register.class));
            }
        });
    }

    /*private void configureMapButton(){
        Button mapsButton = (Button) findViewById(R.id.btnMap);
        mapsButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                startActivity(new Intent(MainActivity.this, MapsActivity.class));
            }
        });
    }*/
}
