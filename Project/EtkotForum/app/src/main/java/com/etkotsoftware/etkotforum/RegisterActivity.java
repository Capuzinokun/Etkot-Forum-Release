package com.etkotsoftware.etkotforum;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity {

    private EditText registerEmailText;
    private EditText registerPasswordText;
    private EditText registerPasswordConfirmText;
    private Button registerButton;
    private Button loginActivityButton;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        registerEmailText = (EditText) findViewById(R.id.register_email);
        registerPasswordText = (EditText) findViewById(R.id.register_password);
        registerPasswordConfirmText = (EditText) findViewById(R.id.register_password_confirm);
        registerButton = (Button) findViewById(R.id.register_button);
        loginActivityButton = (Button) findViewById(R.id.login_activity_button);

        registerButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                checkCriteria();
            }
        });

        loginActivityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent loginIntent = new Intent(RegisterActivity.this,
                        LoginActivity.class);
                startActivity(loginIntent);
                finish();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {

            Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
            startActivity(mainIntent);
            finish();
        }
    }

    private void checkCriteria() {

        String password_not_valid = "Password needs to contain lower, uppercase letters and numbers!";
        String email_not_valid = "That's not a proper email!";
        String passwords_dont_match = "The passwords don't match!";
        String empty_fields = "Please fill in all the information!";

        String email = registerEmailText.getText().toString();
        String password = registerPasswordText.getText().toString();
        String confirm_password = registerPasswordConfirmText.getText().toString();

        if (!email.trim().isEmpty() && !password.trim().isEmpty()
                && !confirm_password.trim().isEmpty()) {

            if (password.equals(confirm_password)) {

                if (email.contains("@") && email.contains(".")) {

                    if (checkPasswordSeverity(password)) {

                        mAuth.createUserWithEmailAndPassword(email, password)
                                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {

                                if (task.isSuccessful()) {

                                    Intent mainIntent = new Intent(
                                            RegisterActivity.this, MainActivity.class);
                                    startActivity(mainIntent);
                                    finish();
                                }
                                else {
                                    Toast.makeText(RegisterActivity.this,
                                            "Error: " + task.getException().getMessage(),
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                    else {
                        Toast.makeText(RegisterActivity.this, password_not_valid, Toast.LENGTH_LONG).show();
                    }
                }
                else {
                    Toast.makeText(RegisterActivity.this, email_not_valid, Toast.LENGTH_LONG).show();
                }
            }
            else {
                Toast.makeText(RegisterActivity.this, passwords_dont_match, Toast.LENGTH_LONG).show();
            }

        }
        else {
            Toast.makeText(RegisterActivity.this, empty_fields, Toast.LENGTH_LONG).show();
        }
    }

    private boolean checkPasswordSeverity(String password) {

        boolean hasLowercase = !password.equals(password.toUpperCase());
        boolean hasUppercase = !password.equals(password.toLowerCase());
        boolean hasNumbers = password.matches(".*\\d.*");

        if ((hasLowercase? 1:0) + (hasUppercase? 1:0) + (hasNumbers? 1:0) == 3) {
            return true;
        }
        return false;
    }
}