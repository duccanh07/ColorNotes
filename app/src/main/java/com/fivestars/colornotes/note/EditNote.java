package com.fivestars.colornotes.note;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.fivestars.colornotes.MainActivity;
import com.fivestars.colornotes.R;
import com.fivestars.colornotes.alarm.AlarmBroadcast;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class EditNote extends AppCompatActivity {
    Intent data;
    EditText editNoteTitle, editNoteContent;
    FirebaseFirestore fStore;
    ProgressBar spinner;
    FirebaseUser user;
    Button dateTimePicker, textVoice;
    TextView showDate,setDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_note);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fStore = FirebaseFirestore.getInstance();
        spinner = findViewById(R.id.progressBar2);
        user = FirebaseAuth.getInstance().getCurrentUser();

        data = getIntent();

        editNoteContent = findViewById(R.id.editNoteContent);
        editNoteTitle = findViewById(R.id.editNoteTitle);

        dateTimePicker = findViewById(R.id.dateTimePicker1);
        showDate = findViewById(R.id.showDate1);
        setDate = findViewById(R.id.setDate1);

        textVoice = findViewById(R.id.textVoice);

        String noteTitle =  data.getStringExtra("title");
        String noteContent =  data.getStringExtra("content");
        String noteExpiredDate = data.getStringExtra("expiredDate");
        String noteSetExpiredDate = data.getStringExtra("setExpiredDate");

        editNoteTitle.setText(noteTitle);
        editNoteContent.setText(noteContent);
        showDate.setText(noteExpiredDate);
        setDate.setText(noteSetExpiredDate);

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

                        new TimePickerDialog(EditNote.this,timeSetListener,calendar.get(Calendar.HOUR_OF_DAY),calendar.get(Calendar.MINUTE), true).show();
                    }
                };

                new DatePickerDialog(EditNote.this,dateSetListener,calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH),calendar.get(Calendar.DAY_OF_MONTH)).show();
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
                    Toast.makeText(EditNote.this, "Điện thoại của bạn không hỗ trợ thu âm!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        FloatingActionButton fab = findViewById(R.id.saveEdit);
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
                String nTitle = editNoteTitle.getText().toString();
                String nContent = editNoteContent.getText().toString();
                String nExpiredDate = setDate.getText().toString();
                String nExpiredDateTime = showDate.getText().toString();
                setAlarm(nTitle,nExpiredDateTime);
                if (nContent.isEmpty()) {
                    Toast.makeText(EditNote.this, "Không thể cập nhật ghi chú vì nội dung trống!", Toast.LENGTH_LONG).show();
                    return;
                }

                spinner.setVisibility(View.VISIBLE);

                DocumentReference docref = fStore.collection("notes").document(user.getUid()).collection("myNotes").document(data.getStringExtra("noteID"));
                Map<String, Object> note = new HashMap<>();
                note.put("title", nTitle);
                note.put("content", nContent);
                note.put("editDate", new Timestamp(new Date()));
                note.put("expiredDate",getDateFromString(nExpiredDate));

                docref.update(note).addOnSuccessListener(aVoid -> {
                    Toast.makeText(EditNote.this, "Cập nhật ghi chú thành công!", Toast.LENGTH_LONG).show();
                    EditNote.this.onBackPressed();
                    EditNote.this.finish();
                }).addOnFailureListener(e -> {
                    Toast.makeText(EditNote.this, "Cập nhật ghi chú không thành công, hãy thử lại!", Toast.LENGTH_LONG).show();
                    spinner.setVisibility(View.VISIBLE);
                });
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 101) {
            if (resultCode == RESULT_OK && data != null) {
                ArrayList<String> text = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                editNoteContent.setText(text.get(0));
            }
        }

    }

    private void setAlarm(String Title, String nExpiredDateTime) {
        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(getApplicationContext(), AlarmBroadcast.class);
        intent.putExtra("event", Title);
        intent.putExtra("dateTime", nExpiredDateTime);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, PendingIntent.FLAG_ONE_SHOT);
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
}