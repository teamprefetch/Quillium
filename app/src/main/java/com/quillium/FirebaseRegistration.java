package com.quillium;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.quillium.utils.Constants;
import com.quillium.utils.PreferenceManager;

import java.util.Calendar;
import java.util.HashMap;

public class FirebaseRegistration extends AppCompatActivity {

    private EditText emailEditText, nameEditText, passwordEditText;
    private TextView firebaseLogin;
    DatePickerDialog datePickerDialog;
    private String selectedDate = ""; // Define a variable to store the selected date
    private Button registerButton, dobEditText;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;
    CircularProgressIndicator circularLoading;
    private PreferenceManager preferenceManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_firebase_registration);

        firebaseAuth = FirebaseAuth.getInstance();

        emailEditText = findViewById(R.id.student_email);
        nameEditText = findViewById(R.id.student_name);
        dobEditText = findViewById(R.id.date_of_birth_field);
        passwordEditText = findViewById(R.id.student_password_id);
        registerButton = findViewById(R.id.button_verify_firebase);
        firebaseLogin = findViewById(R.id.textView_login_firebase);



        preferenceManager = new PreferenceManager(getApplicationContext());

        dobEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedDate = openDatePicker(); // Update the selectedDate variable with the returned date
                // Now the selectedDate will contain the selected date after the DatePicker dialog is closed
                dobEditText.setText("    " + selectedDate);
            }
        });

        // open login activity
        firebaseLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(FirebaseRegistration.this, MainActivity.class);
                    startActivity(intent);
            }
        });


        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerButton.setVisibility(View.INVISIBLE);

                // Show the circular progress indicator
                circularLoading = findViewById(R.id.circularLoading);
                circularLoading.setVisibility(View.VISIBLE);

                String email = emailEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();
                String fullname = nameEditText.getText().toString().trim();
//                String dob = openDatePicker();

                if (!email.isEmpty() && !fullname.isEmpty() && !selectedDate.isEmpty() && !password.isEmpty()) {

                    registerUser(email, password, fullname, selectedDate);

                    addUserRegisterToFirestore(email, password, fullname);
                } else {
                    registerButton.setVisibility(View.VISIBLE);
                    circularLoading.setVisibility(View.INVISIBLE);
                    Toast.makeText(FirebaseRegistration.this, "Please fill out all the fields", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void registerUser(String email, String password, String fullname, String dob) {
        firebaseAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(this, new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if (task.isSuccessful()) {
//                    Intent intent = new Intent(FirebaseRegistration.this, MainActivity.class);
//                    startActivity(intent);

                    // Registration successful
                    databaseReference = FirebaseDatabase.getInstance().getReference().child("users");

                    String userId = firebaseAuth.getCurrentUser().getUid();
                    User user = new User(fullname, email, dob);

                    // Save user data to Realtime Database
                    if (!TextUtils.isEmpty(fullname)) {
                        // Save user data to Realtime Database
                        databaseReference.child(userId).setValue(user);
                    }

//                    Toast.makeText(FirebaseRegistration.this, "Registration successful", Toast.LENGTH_SHORT).show();

                    Toast.makeText(FirebaseRegistration.this, "Registration is successful", Toast.LENGTH_SHORT).show();

                    // Add your logic to navigate to the next activity or perform other actions

                    registerButton.setVisibility(View.VISIBLE);
                    circularLoading.setVisibility(View.INVISIBLE);
                } else {
                    // If registration fails, display a message to the user.
                    if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                        registerButton.setVisibility(View.VISIBLE);
                        circularLoading.setVisibility(View.INVISIBLE);
                        Toast.makeText(getApplicationContext(), "User is already registered. Try with different credentials.", Toast.LENGTH_LONG).show();

                    } else {
                        registerButton.setVisibility(View.VISIBLE);
                        circularLoading.setVisibility(View.INVISIBLE);
                        Toast.makeText(getApplicationContext(), "Sorry! Registration is unsuccessful. Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
    }


    // Define the openDatePicker method to open the date picker dialog
    private String openDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        int currentMonth = calendar.get(Calendar.MONTH);
        int currentDay = calendar.get(Calendar.DAY_OF_MONTH);

        // Set the minimum date to January 1, 1980
        int minYear = 1980;
        int minMonth = 0; // January (months are 0-indexed)
        int minDay = 1;

        datePickerDialog = new DatePickerDialog(
                FirebaseRegistration.this,
                R.style.CustomDatePickerDialogTheme, // Apply the custom theme here
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {
                        selectedDate = dayOfMonth + "/" + (month + 1) + "/" + year;
                    }
                },
                currentYear, currentMonth, currentDay
        );

        Calendar minDate = Calendar.getInstance();
        minDate.set(minYear, minMonth, minDay);

        // Set the minimum date
        datePickerDialog.getDatePicker().setMinDate(minDate.getTimeInMillis());

        // Set the maximum date to the current date
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());

        datePickerDialog.show();

        // Return the selected date after the dialog is closed
        return selectedDate;
    }

    private void addUserRegisterToFirestore(String email, String password, String fullname){
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        HashMap<String, Object> user = new HashMap<>();
        user.put(Constants.KEY_NAME, fullname);
        user.put(Constants.KEY_EMAIL, email);
        user.put(Constants.KEY_PASSWORD, password);
        firestore.collection(Constants.KEY_COLLECTION_USERS)
                .add(user)
                .addOnSuccessListener(documentReference -> {
                    preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);
                    preferenceManager.putString(Constants.KEY_USER_ID, documentReference.getId());
                    preferenceManager.putString(Constants.KEY_NAME, fullname);
                    Intent intent = new Intent(FirebaseRegistration.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    Toast.makeText(FirebaseRegistration.this, "Data Inserted to Firestore", Toast.LENGTH_LONG).show();
                })
                .addOnFailureListener(exception -> {
                    Toast.makeText(FirebaseRegistration.this, exception.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}