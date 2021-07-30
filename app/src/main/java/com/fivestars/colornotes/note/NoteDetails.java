package com.fivestars.colornotes.note;

import android.content.Intent;
import android.os.Bundle;

import com.fivestars.colornotes.MainActivity;
import com.fivestars.colornotes.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.text.method.ScrollingMovementMethod;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import javax.annotation.Nonnull;

public class NoteDetails extends AppCompatActivity {
    Intent data;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_details);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        data = getIntent();

        TextView content = findViewById(R.id.noteDetailsContent);
        TextView title = findViewById(R.id.noteDetailsTitle);
        TextView showDate = findViewById(R.id.showDate2);
        TextView setDate = findViewById(R.id.setDate2);
        content.setMovementMethod(new ScrollingMovementMethod());

        content.setText(data.getStringExtra("content"));
        title.setText(data.getStringExtra("title"));
        showDate.setText(data.getStringExtra("expiredDate"));
        setDate.setText(data.getStringExtra("setExpiredDate"));

        content.setBackgroundColor(getResources().getColor(data.getIntExtra("color",0),null));

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            Intent i = new Intent(view.getContext(),EditNote.class);
            i.putExtra("title", data.getStringExtra("title"));
            i.putExtra("content", data.getStringExtra("content"));
            i.putExtra("expiredDate",data.getStringExtra("expiredDate"));
            i.putExtra("setExpiredDate",data.getStringExtra("setExpiredDate"));
            i.putExtra("noteID",data.getStringExtra("noteID"));
            startActivity(i);
            finish();
        });
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        onBackPressed();
        return super.onOptionsItemSelected(item);
    }
}