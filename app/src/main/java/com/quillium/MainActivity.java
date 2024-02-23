package com.quillium;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.quillium.utils.Constants;
import com.quillium.utils.PreferenceManager;

public class MainActivity extends AppCompatActivity {

    private EditText emailEditText, passwordEditText;
    private Button loginButton;
    private FirebaseAuth firebaseAuth;
    private PreferenceManager preferenceManager;


    // SharedPreferences key constants
    private static final String PREF_NAME = "LoginPrefs";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PASSWORD = "password";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        firebaseAuth = FirebaseAuth.getInstance();

        emailEditText = findViewById(R.id.student_email);
        passwordEditText = findViewById(R.id.password_id);
        loginButton = findViewById(R.id.button_login);

        preferenceManager = new PreferenceManager(getApplicationContext());


        // open register activity
        TextView Register = findViewById(R.id.textView_register);
        TextView FRegister = findViewById(R.id.textView_firebase_register);


        // Check if the user is already authenticated
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            // User is already logged in, navigate to HomePage
            startActivity(new Intent(MainActivity.this, HomePage.class));
            finish(); // Finish the MainActivity to prevent going back when pressing back button
        }

        // Retrieve saved login information
        SharedPreferences preferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        String savedEmail = preferences.getString(KEY_EMAIL, "");
        String savedPassword = preferences.getString(KEY_PASSWORD, "");

        // Set saved values to EditText fields
        emailEditText.setText(savedEmail);
        passwordEditText.setText(savedPassword);

        FRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, FirebaseRegistration.class);
                startActivity(intent);
            }
        });

        Register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String email = emailEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();

                if (!email.isEmpty() && !password.isEmpty()) {
                    // Save login information
//                    saveLoginInfo(email, password);

                    // Login user
                    loginUser(email, password);

                    addDataToFirestore(email, password);
                } else {
                    Toast.makeText(MainActivity.this, "Please enter email and password", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void saveLoginInfo(String email, String password) {
        // Save login information using SharedPreferences
        SharedPreferences preferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_PASSWORD, password);
        editor.apply();
    }

    private void loginUser(String email, String password) {
        firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if (task.isSuccessful()) {
                    // Login successful
                    Toast.makeText(MainActivity.this, "Login successful", Toast.LENGTH_SHORT).show();

                    // Add your logic to navigate to the next activity or perform other actions
//                    Intent intent = new Intent(MainActivity.this, HomePage.class);
//                    startActivity(intent);
//                    finish();
                    Intent intent = new Intent(getApplicationContext(), HomePage.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    // If login fails, display a message to the user.
                    Toast.makeText(MainActivity.this, "Login failed! Check your credentials.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void addDataToFirestore(String email, String password){
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.collection(Constants.KEY_COLLECTION_USERS)
                .whereEqualTo(Constants.KEY_EMAIL, email)
                .whereEqualTo(Constants.KEY_PASSWORD, password)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful() && task.getResult() !=null && task.getResult().getDocuments().size()>0){
                            DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                            preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);
                            preferenceManager.putString(Constants.KEY_USER_ID, documentSnapshot.getId());
                            preferenceManager.putString(Constants.KEY_NAME, documentSnapshot.getString(Constants.KEY_NAME));
//                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
//                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                            startActivity(intent);
//                            finish();
                            Toast.makeText(MainActivity.this, "Token User Id: "+documentSnapshot.getId(), Toast.LENGTH_SHORT).show();

                        }
                    }
                });
    }
}