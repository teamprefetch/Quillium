package com.quillium;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Calendar;

public class RegisterActivity extends AppCompatActivity {
    Button date_of_birth, Verify;
    TextInputEditText studentEmail, HSC;
    TextView Login;
    DatePickerDialog datePickerDialog;
    private String selectedDate = ""; // Define a variable to store the selected date

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        date_of_birth = findViewById(R.id.date_of_birth_field);
        studentEmail = findViewById(R.id.student_id);
        HSC = findViewById(R.id.student_hsc_roll);
        Login = findViewById(R.id.textView_login);
        Verify = findViewById(R.id.button_verify);


        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH);
        int year = calendar.get(Calendar.YEAR);


        date_of_birth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedDate = openDatePicker(); // Update the selectedDate variable with the returned date
                // Now the selectedDate will contain the selected date after the DatePicker dialog is closed
                date_of_birth.setText("    " + selectedDate);
            }
        });

//        StudentID = studentIdEditText.getText().toString().trim();
//        HSCRoll = studentHscRollEditText.getText().toString().trim();

//===============================================================================================================================
        studentEmail.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    // Set the dynamic hint text inside the EditText when it gains focus
                    studentEmail.setHint("(e.g: B190305001)");

                    // Request focus programmatically to show the keyboard
                    studentEmail.requestFocus();

                    // Show the keyboard
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(studentEmail, InputMethodManager.SHOW_IMPLICIT);
                } else {
                    // Clear the hint text when the EditText loses focus
                    studentEmail.setHint("");
                }
            }
        });
        HSC.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    // Set the dynamic hint text inside the EditText when it gains focus
                    HSC.setHint("(e.g: 123456)");

                    // Request focus programmatically to show the    keyboard
                    HSC.requestFocus();

                    // Show the keyboard
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(HSC, InputMethodManager.SHOW_IMPLICIT);
                } else {
                    // Clear the hint text when the EditText loses focus
                    HSC.setHint("");
                }
            }
        });
        //===============================================================================================================================

        // open login activity
        Login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

        // open verified data overview and verify email activity
        Verify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Hide the "Verify" button
                Verify.setVisibility(View.INVISIBLE);

                // Show the circular progress indicator
                CircularProgressIndicator circularLoading = findViewById(R.id.circularLoading);
                circularLoading.setVisibility(View.VISIBLE);

                String email = studentEmail.getText().toString().trim();
                String roll = HSC.getText().toString().trim();
                String dob = date_of_birth.getText().toString().trim();

                if (!email.isEmpty() && !roll.isEmpty() && !selectedDate.isEmpty()) {

                    verifyEmailActivity(email, roll, selectedDate);

                } else {
                    Verify.setVisibility(View.VISIBLE);
                    circularLoading.setVisibility(View.INVISIBLE);
                    Toast.makeText(RegisterActivity.this, "Please fill out all the fields", Toast.LENGTH_SHORT).show();
                }

//                Intent intent = new Intent(RegisterActivity.this, VerifyEmailActivity.class);
//                startActivity(intent);

            }
        });
    }

    private void verifyEmailActivity(String email, String roll, String selectedDate) {

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
                RegisterActivity.this,
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
}