package com.fivestars.colornotes.user;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
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
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

public class Login extends AppCompatActivity {
    public static final int GOOGLE_SIGN_IN_CODE = 10005;
    EditText lEmail,lPassword;
    Button loginNow;
    TextView forgetPass,createAcc;
    FirebaseAuth fAuth;
    ProgressBar spinner;
    FirebaseFirestore fStore;
    FirebaseUser user;
    SignInButton signIn;
    GoogleSignInOptions gso;
    GoogleSignInClient signInClient;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getSupportActionBar().setTitle("Đăng Nhập");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        signIn = findViewById(R.id.signIn);
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("443836838971-d71bkthnparehjvf31vsf8io6nrf0tdq.apps.googleusercontent.com")
                .requestEmail()
                .build();

        signInClient = GoogleSignIn.getClient(this, gso);

        GoogleSignInAccount signInAccount = GoogleSignIn.getLastSignedInAccount(this);


        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent sign = signInClient.getSignInIntent();
                startActivityForResult(sign, GOOGLE_SIGN_IN_CODE);
            }
        });

        lEmail = findViewById(R.id.email);
        lPassword = findViewById(R.id.lPassword);
        loginNow = findViewById(R.id.loginBtn);

        spinner = findViewById(R.id.progressBar3);

        forgetPass = findViewById(R.id.forgotPasword);
        createAcc = findViewById(R.id.createAccount);

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();

        showWarning();

        createAcc.setOnClickListener(view -> {
            startActivity(new Intent(getApplicationContext(),Register.class));
            finish();
        });

        loginNow.setOnClickListener(view -> {
            String mEmail = lEmail.getText().toString();
            String mPassword = lPassword.getText().toString();
            spinner.setVisibility(View.VISIBLE);

            if(mEmail.isEmpty()){
                lEmail.setError("Phải nhập email đăng nhập!");
                spinner.setVisibility(View.GONE);
                return;
            }

            if(mPassword.isEmpty()){
                lPassword.setError("Phải nhập mật khẩu đăng nhập!");
                spinner.setVisibility(View.GONE);
                return;
            }
            fAuth.signInWithEmailAndPassword(mEmail,mPassword).addOnSuccessListener(authResult -> {
                Toast.makeText(Login.this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                //Xóa toàn bộ note của tài khoản ẩn danh trước
                if(fAuth.getCurrentUser().isAnonymous()){
                    user = fAuth.getCurrentUser();
                    fStore.collection("notes").document(user.getUid()).delete();
                    //Xóa user ẩn danh
                    user.delete();
                }

                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                finish();
            }).addOnFailureListener(e -> {
                Toast.makeText(Login.this, "Email hoặc mật khẩu không đúng!", Toast.LENGTH_SHORT).show();
                spinner.setVisibility(View.GONE);
            });
        });

        forgetPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText resetEmail = new EditText(view.getContext());
                AlertDialog.Builder passwordResetDialog = new AlertDialog.Builder(view.getContext());
                passwordResetDialog.setTitle("Khôi phục mật khẩu?");
                passwordResetDialog.setMessage("Nhập email của bạn để nhận liên kết khôi phục mật khẩu");
                passwordResetDialog.setView(resetEmail);
                passwordResetDialog.setPositiveButton("OK!", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //Tiến hành thực thi
                        String mail = resetEmail.getText().toString();
                        fAuth.sendPasswordResetEmail(mail).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(Login.this, "Đã gửi email khôi phục!", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(Login.this, "Email không đúng hoặc không tồn tại!", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }).setNegativeButton("Hủy", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //Đóng thông báo
                    }
                });
                passwordResetDialog.create().show();
            }
        });
    }

    private void showWarning() {
        AlertDialog.Builder warning = new AlertDialog.Builder(this)
                .setTitle("Bạn có muốn đăng nhập không?")
                .setMessage("Đăng nhập tài khoản sẽ xóa toàn bộ ghi chú hiện tại, tạo tài khoản mới để lưu ghi chú.")
                .setPositiveButton("Đến đăng ký!", (dialogInterface, i) -> {
                    startActivity(new Intent(getApplicationContext(), Register.class));
                    finish();
                })
                .setNegativeButton("OK", (dialogInterface, i) -> {
                    //do nothing
                });
        warning.show();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        startActivity(new Intent(this, MainActivity.class));
        finish();
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == GOOGLE_SIGN_IN_CODE){
            Task<GoogleSignInAccount> signInTask = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount signInAcc = signInTask.getResult(ApiException.class);
                AuthCredential authCredential = GoogleAuthProvider.getCredential(signInAcc.getIdToken(),null);
                fAuth.signInWithCredential(authCredential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        startActivity(new Intent(getApplicationContext(), LoadScreen.class));
                        finish();
                        Toast.makeText(getApplicationContext(), "Tài khoản Google của bạn đã được kết nối đến Color Notes.", Toast.LENGTH_SHORT).show();
                        if(fAuth.getCurrentUser().isAnonymous()){
                            user = fAuth.getCurrentUser();
                            fStore.collection("notes").document(user.getUid()).delete();
                            //Xóa user ẩn danh
                            user.delete();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });

            } catch (ApiException e) {
                e.printStackTrace();
            }
        }
    }
}