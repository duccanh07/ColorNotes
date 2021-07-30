package com.fivestars.colornotes.user;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.fivestars.colornotes.LoadScreen;
import com.fivestars.colornotes.MainActivity;
import com.fivestars.colornotes.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.core.Tag;
import com.google.firebase.firestore.FirebaseFirestore;

public class Register extends AppCompatActivity {
    EditText rUserName,rUserEmail,rUserPass,rUserConfPass;
    Button syncAccount;
    TextView loginAccount;
    ProgressBar spinner;
    FirebaseAuth fAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        getSupportActionBar().setTitle("Tạo tài khoản mới!");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        fAuth = FirebaseAuth.getInstance();

        rUserName = findViewById(R.id.userDisplayName);
        rUserEmail = findViewById(R.id.userDisplayEmail);
        rUserPass = findViewById(R.id.password);
        rUserConfPass = findViewById(R.id.passwordConfirm);
        syncAccount = findViewById(R.id.createAccount);
        loginAccount = findViewById(R.id.login);
        spinner = findViewById(R.id.progressBar4);

        loginAccount.setOnClickListener(view -> startActivity(new Intent(getApplicationContext(),Login.class)));

        syncAccount.setOnClickListener(view -> {
            String uUserName = rUserName.getText().toString();
            String uUserEmail = rUserEmail.getText().toString();
            String uUserPass = rUserPass.getText().toString();
            String uConfPass = rUserConfPass.getText().toString();
            spinner.setVisibility(View.VISIBLE);

            if(uUserEmail.isEmpty() || uUserName.isEmpty() || uUserPass.isEmpty() || uConfPass.isEmpty()){
                Toast.makeText(Register.this, "Phải nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
                spinner.setVisibility(View.GONE);
                return;
            }

            if(!uUserPass.equals(uConfPass)){
                rUserConfPass.setError("Mật khẩu không khớp!");
                spinner.setVisibility(View.GONE);
                return;
            }
            AuthCredential credential = EmailAuthProvider.getCredential(uUserEmail,uUserPass);
            fAuth.getCurrentUser().linkWithCredential(credential).addOnSuccessListener(authResult -> {
                FirebaseUser user = fAuth.getCurrentUser();
                user.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(Register.this, "Đã gửi email xác thực!", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(Register.this, "Email chưa được gửi!", Toast.LENGTH_SHORT).show();
                    }
                });

                Toast.makeText(Register.this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(getApplicationContext(), LoadScreen.class));

                FirebaseUser usr = fAuth.getCurrentUser();
                UserProfileChangeRequest request = new UserProfileChangeRequest.Builder()
                        .setDisplayName(uUserName)
                        .build();
                usr.updateProfile(request);
                finish();
            }).addOnFailureListener(e -> {
                Toast.makeText(Register.this, "Đăng ký không thành công!" + e.getMessage(), Toast.LENGTH_SHORT).show();
                spinner.setVisibility(View.GONE);
            });
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        startActivity(new Intent(this, MainActivity.class));
        finish();
        return super.onOptionsItemSelected(item);
    }
}