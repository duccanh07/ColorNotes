package com.fivestars.colornotes.note;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.fivestars.colornotes.alarm.AlarmBroadcast;
import com.fivestars.colornotes.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.speech.RecognizerIntent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class AddNote extends AppCompatActivity {
    FirebaseFirestore fStore;
    EditText noteTitle, noteContent;
    ProgressBar progressBarSave;
    FirebaseUser user;
    Button dateTimePicker, textVoice;
    TextView showDate,setDate;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_note);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fStore = FirebaseFirestore.getInstance();
        noteContent = findViewById(R.id.addNoteContent);
        noteTitle = findViewById(R.id.addNoteTitle);
        user = FirebaseAuth.getInstance().getCurrentUser();
        dateTimePicker = findViewById(R.id.dateTimePicker);
        showDate = findViewById(R.id.showDate);
        setDate = findViewById(R.id.setDate);
        textVoice = findViewById(R.id.textVoice);

        progressBarSave = findViewById(R.id.progressBar);

        dateTimePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDateTimeDialog(dateTimePicker);
            }
            private void showDateTimeDialog(Button dateTimePicker) {
                Calendar calendar= Calendar.getInstance();
                DatePickerDialog.OnDateSetListener dateSetListener=new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        calendar.set(Calendar.YEAR,year);
                        calendar.set(Calendar.MONTH,month);
                        calendar.set(Calendar.DAY_OF_MONTH,dayOfMonth);

                        TimePickerDialog.OnTimeSetListener timeSetListener=new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                calendar.set(Calendar.HOUR_OF_DAY,hourOfDay);
                                calendar.set(Calendar.MINUTE,minute);

                                SimpleDateFormat showDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:00");
                                SimpleDateFormat setDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:00'Z'");


                                showDate.setText(showDateFormat.format(calendar.getTime()));
                                setDate.setText(setDateFormat.format(calendar.getTime()));
                            }

                        };
                        new TimePickerDialog(AddNote.this,timeSetListener,calendar.get(Calendar.HOUR_OF_DAY),calendar.get(Calendar.MINUTE), true).show();
                    }
                };
                new DatePickerDialog(AddNote.this,dateSetListener,calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH),calendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        textVoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "vi-VN");
                try {
                    startActivityForResult(intent, 101);
                } catch (Exception e) {
                    Toast.makeText(AddNote.this, "Điện thoại của bạn không hỗ trợ thu âm!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:00'Z'");
            public Date getDateFromString(String datetoSaved) {
                try {
                    Date date = simpleDateFormat.parse(datetoSaved);
                    return date;
                } catch (ParseException e) {
                    return null;
                }
            }
            @Override
            public void onClick(View view) {
                String nTitle = noteTitle.getText().toString();
                String nContent = noteContent.getText().toString();
                String nExpiredDate = setDate.getText().toString();
                String nExpiredDateTime = showDate.getText().toString();
                if (nContent.isEmpty() || nExpiredDate.isEmpty()) {
                    Toast.makeText(AddNote.this, "Không thể thêm ghi chú vì nội dung trống hoặc không có ngày đến hạn!", Toast.LENGTH_LONG).show();
                    return;
                }
                progressBarSave.setVisibility(View.VISIBLE);

                DocumentReference docref = fStore.collection("notes").document(user.getUid()).collection("myNotes").document();
                Map<String, Object> note = new HashMap<>();
                note.put("title", nTitle);
                note.put("content", nContent);
                note.put("createDate", new Timestamp(new Date()));
                note.put("editDate", new Timestamp(new Date()));
                note.put("complete", false);
                note.put("priority", false);
                note.put("expiredDate", getDateFromString(nExpiredDate));
                note.put("expired", false);
                note.put("count","1");

                docref.set(note).addOnSuccessListener(aVoid -> {
                    Toast.makeText(AddNote.this, "Thêm ghi chú thành công!", Toast.LENGTH_LONG).show();
                    AddNote.this.onBackPressed();
                }).addOnFailureListener(e -> {
                    Toast.makeText(AddNote.this, "Thêm ghi chú không thành công, hãy thử lại!", Toast.LENGTH_LONG).show();
                    progressBarSave.setVisibility(View.VISIBLE);
                });
                setAlarm(nTitle, nExpiredDateTime);
            }
        });
    }
    private void setAlarm(String Title, String nExpiredDateTime) {
        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(getApplicationContext(), AlarmBroadcast.class);
        intent.putExtra("event", Title);
        intent.putExtra("dateTime", nExpiredDateTime);

        final int id = (int) System.currentTimeMillis();
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), id, intent, PendingIntent.FLAG_ONE_SHOT);
        String dateandtime = nExpiredDateTime;
        DateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:00");
        try {
            Date date1 = formatter.parse(dateandtime);
            am.set(AlarmManager.RTC_WAKEUP, date1.getTime(), pendingIntent);
        } catch (ParseException e) {
            e.printStackTrace();

        }

        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 101) {
            if (resultCode == RESULT_OK && data != null) {
                ArrayList<String> text = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                noteContent.setText(text.get(0));
            }
        }

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.close_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.close){
            Toast.makeText(this,"Đã hủy!", Toast.LENGTH_LONG).show();
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }
}