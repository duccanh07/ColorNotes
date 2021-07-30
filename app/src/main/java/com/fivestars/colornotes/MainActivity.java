package com.fivestars.colornotes;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;

import androidx.core.view.GravityCompat;
import androidx.core.view.MenuItemCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.app.AlertDialog;
import android.content.Intent;

import android.net.Uri;
import android.os.Bundle;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;

import android.widget.ImageView;

import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.fivestars.colornotes.note.AddNote;

import com.fivestars.colornotes.note.AllNotes;
import com.fivestars.colornotes.note.Completed;
import com.fivestars.colornotes.note.Expired;
import com.fivestars.colornotes.note.Priority;
import com.fivestars.colornotes.note.UnCompleted;
import com.fivestars.colornotes.user.Login;
import com.fivestars.colornotes.user.Profile;
import com.fivestars.colornotes.user.Register;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.google.firebase.firestore.FirebaseFirestore;

import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private long pressedTime;
    DrawerLayout drawerLayout;
    ActionBarDrawerToggle toggle;
    NavigationView nav_menu_view;
    FirebaseFirestore fStore;
    FirebaseUser user;
    FirebaseAuth fAuth;
    StorageReference storageReference;
    BottomNavigationView bottomView;

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fAuth = FirebaseAuth.getInstance();
        user = fAuth.getCurrentUser();
        fStore = FirebaseFirestore.getInstance();
        getFragment(new AllNotes());
        bottomView = findViewById(R.id.bottomMenu);
        bottomView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.allNotes:
                        getSupportActionBar().setTitle("Tất cả ghi chú");
                        getFragment(new AllNotes());
                        break;
                    case R.id.unCompleted:
                        getSupportActionBar().setTitle("Ghi chú chưa hoàn thành");
                        getFragment(new UnCompleted());
                        break;
                    case R.id.completed:
                        getSupportActionBar().setTitle("Ghi chú đã hoàn thành");
                        getFragment(new Completed());
                        break;
                    case R.id.priority:
                        getSupportActionBar().setTitle("Ghi chú quan trọng");
                        getFragment(new Priority());
                        break;
                    case R.id.expired:
                        getSupportActionBar().setTitle("Ghi chú hết hạn");
                        getFragment(new Expired());
                        break;
                }
                return true;
            }

        });

        drawerLayout = findViewById(R.id.drawer);
        nav_menu_view = findViewById(R.id.nav_menu_view);
        nav_menu_view.setNavigationItemSelectedListener(this);

        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open,R.string.close );
        drawerLayout.addDrawerListener(toggle);
        toggle.setDrawerIndicatorEnabled(true);
        toggle.syncState();

        View headerView = nav_menu_view.getHeaderView(0);
        TextView userName = headerView.findViewById(R.id.userDisplayName);
        TextView userEmail = headerView.findViewById(R.id.userDisplayEmail);
        ImageView userImage = headerView.findViewById(R.id.userDisplayImage);

        userImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, Profile.class));
                finish();
            }
        });

        if(user.isAnonymous()){
            userEmail.setVisibility(View.INVISIBLE);
            userImage.setVisibility(View.INVISIBLE);
            userName.setText("Tài khoản tạm thời");
        }
        else {
            userEmail.setText(user.getEmail());
            userName.setText(user.getDisplayName());

            storageReference = FirebaseStorage.getInstance().getReference();

            StorageReference profileRef = storageReference.child("users/"+fAuth.getCurrentUser().getUid()+"/profile.jpg");
            profileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Picasso.get().load(uri).into(userImage);
                }
            });
        }

        FloatingActionButton fab = findViewById(R.id.addNoteFloat);
        fab.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                view.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent event) {
                        switch (event.getActionMasked()) {
                            case MotionEvent.ACTION_MOVE:
                                view.setX(event.getRawX());
                                view.setY(event.getRawY() - 100);
                                break;
                            case MotionEvent.ACTION_UP:
                                view.setOnTouchListener(null);
                                break;
                            default:
                                break;
                        }
                        return true;
                    }
                });
                return true;
            }
        });
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this,AddNote.class));
            }
        });
    }
    private void getFragment(Fragment fragment) {
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = manager.beginTransaction();
        fragmentTransaction.replace(R.id.container,fragment);
        fragmentTransaction.commit();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        drawerLayout.closeDrawer(GravityCompat.START);
        switch (item.getItemId()){
            case R.id.notes:
                startActivity(new Intent(this, MainActivity.class));
                finish();
                break;
            case R.id.addNote:
                startActivity(new Intent(this, AddNote.class));
                break;
            case R.id.dashboard:
                getSupportActionBar().setTitle("Thống kê ghi chú");
                getFragment(new Dashboards());
                break;
            case R.id.viewProfile:
                if(!user.isAnonymous()){
                    startActivity(new Intent(this, Profile.class));
                }
                else{
                    Toast.makeText(this, "Bạn là tài khoản tạm thời!", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.sync:
                if (user.isAnonymous()){
                    startActivity(new Intent(this, Register.class));
                    finish();
                }
                else {
                    Toast.makeText(this, "Bạn đã và đang đăng nhập, đăng xuất để tạo tài khoản mới!", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.loginmain:
                if (user.isAnonymous()){
                    startActivity(new Intent(this, Login.class));
                    finish();
                }
                else {
                    Toast.makeText(this, "Bạn đã và đang đăng nhập!", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.logout:
                checkUser();
                break;
            default:
                Toast.makeText(this,"Ghi chú sắp diễn ra...",Toast.LENGTH_LONG).show();
        }
        return false;
    }

    private void checkUser() {
        if(user.isAnonymous()){
            displayAlert();
        }
        else{
            FirebaseAuth.getInstance().signOut();
            GoogleSignIn.getClient(getApplicationContext(),new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build())
                    .signOut();
            startActivity(new Intent(getApplicationContext(),LoadScreen.class));
            finish();
        }
    }
    private void displayAlert(){
        AlertDialog.Builder warning = new AlertDialog.Builder(this)
                .setTitle("Bạn có muốn đăng xuất không?")
                .setMessage("Nếu không dùng tài khoản, mọi ghi chú sẽ bị xóa!")
                .setPositiveButton("Đến đăng ký!", (dialogInterface, i) -> startActivity(new Intent(getApplicationContext(), Register.class)))
                .setNegativeButton("Đăng xuất", (dialogInterface, i) -> {
                    user = fAuth.getCurrentUser();
                    fStore.collection("notes").document(user.getUid()).delete();
                    user.delete().addOnSuccessListener(aVoid -> {
                        startActivity(new Intent(getApplicationContext(), LoadScreen.class));
                        finish();
                    });
                });
        warning.show();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.option_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()){
            case R.id.settings:
                Toast.makeText(this, "Settings Menu is Clicked.", Toast.LENGTH_SHORT).show();
            break;
            case R.id.dashboard:
                getSupportActionBar().setTitle("Thống kê ghi chú");
                getFragment(new Dashboards());
        }
        return true;
    }

    @Override
    public void onBackPressed() {

        if (pressedTime + 2000 > System.currentTimeMillis()) {
            super.onBackPressed();
            finish();
        } else {
            Toast.makeText(getBaseContext(), "Nhấn nút quay lại lần nữa để thoát.", Toast.LENGTH_SHORT).show();
        }
        pressedTime = System.currentTimeMillis();
    }
}