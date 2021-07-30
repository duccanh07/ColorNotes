package com.fivestars.colornotes.note;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Canvas;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.LayoutInflater;

import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;

import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.fivestars.colornotes.R;
import com.fivestars.colornotes.model.Note;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;

import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;


import java.sql.Time;
import java.util.ArrayList;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.Timer;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

import static com.google.firebase.Timestamp.now;

public class Expired extends Fragment {
    RecyclerView noteLists;
    FirestoreRecyclerAdapter<Note, NoteViewHolder> noteAdapter;
    ImageView noNotes;
    TextView noText;

    public Expired() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        Query query = FirebaseFirestore.getInstance().collection("notes")
                .document(user.getUid())
                .collection("myNotes")
                .whereEqualTo("expired",true);


        FirestoreRecyclerOptions<Note> allNotes = new FirestoreRecyclerOptions.Builder<Note>()
                .setQuery(query,Note.class)
                .build();

        noteAdapter = new FirestoreRecyclerAdapter<Note, NoteViewHolder>(allNotes) {
            @Override
            protected void onBindViewHolder(@NonNull NoteViewHolder noteViewHolder, int i, Note note) {
                noteViewHolder.noteTitle.setText(note.getTitle());
                noteViewHolder.noteContent.setText(note.getContent());
                CharSequence dateCharSeq = DateFormat.format("EEEE dd-MM-yyyy HH:mm:ss", note.getCreateDate().toDate());
                CharSequence editdateCharSeq = DateFormat.format("EEEE dd-MM-yyyy HH:mm:ss", note.getEditDate().toDate());
                CharSequence expireddateCharSeq = DateFormat.format("dd-MM-yyyy HH:mm", note.getExpiredDate().toDate());
                CharSequence setexpireddateCharSeq = DateFormat.format("yyyy-MM-dd'T'HH:mm:00'Z'", note.getExpiredDate().toDate());
                noteViewHolder.noteDate.setText(String.format("Ngày tạo: %s\nLần sửa đổi cuối: %s\nNgày đến hạn: %s", dateCharSeq, editdateCharSeq,expireddateCharSeq));
                noteViewHolder.noteCheckBox.setChecked(note.getComplete());
                noteViewHolder.notePriority.setChecked(note.getPriority());
                Integer color = getRandomColor();
                noNotes.setVisibility(View.GONE);
                noText.setVisibility(View.GONE);
                noteViewHolder.mCardView.setCardBackgroundColor(noteViewHolder.view.getResources().getColor(color,null));
                String docID = noteAdapter.getSnapshots().getSnapshot(i).getId();
                noteViewHolder.view.setOnClickListener(v -> {
                    Intent ii = new Intent(v.getContext(), NoteDetails.class);
                    ii.putExtra("title", note.getTitle());
                    ii.putExtra("content", note.getContent());
                    ii.putExtra("expiredDate",expireddateCharSeq);
                    ii.putExtra("setExpiredDate",setexpireddateCharSeq);
                    ii.putExtra("color", color);
                    ii.putExtra("noteID", docID);
                    v.getContext().startActivity(ii);
                });
                //Nhấn giữ vào ghi chú show ra menu xóa sửa
                noteViewHolder.view.setOnLongClickListener(view -> {
                    PopupMenu menu = new PopupMenu(view.getContext(),view);
                    menu.getMenu().add("Sửa").setOnMenuItemClickListener(menuItem -> {
                        Intent i14 = new Intent(view.getContext(), EditNote.class);
                        i14.putExtra("title", note.getTitle());
                        i14.putExtra("content", note.getContent());
                        i14.putExtra("expiredDate",expireddateCharSeq);
                        i14.putExtra("setExpiredDate",setexpireddateCharSeq);
                        i14.putExtra("noteID",docID);
                        startActivity(i14);
                        return false;
                    });
                    menu.getMenu().add("Xóa").setOnMenuItemClickListener(menuItem -> {
                        AlertDialog.Builder message = new AlertDialog.Builder(getActivity());
                        message.setTitle(R.string.message_title);
                        message.setMessage(R.string.message_content);
                        message.setNegativeButton(R.string.yes, (dialog, which) -> {
                            DocumentReference docref = FirebaseFirestore.getInstance().collection("notes").document(user.getUid()).collection("myNotes").document(docID);
                            docref.delete().addOnSuccessListener(aVoid -> {
                                //Xóa
                            }).addOnFailureListener(e -> Toast.makeText(getActivity(),"Không thể xóa ghi chú",Toast.LENGTH_LONG));
                        }).setPositiveButton(R.string.no, (dialogInterface, i13) -> {

                        }).show();
                        return false;
                    });
                    menu.show();
                    return false;
                });

                //Nhấn vào icon show ra menu xóa sửa
                ImageView menuIcon = noteViewHolder.view.findViewById(R.id.menuIcon);
                menuIcon.setOnClickListener(view -> {
                    PopupMenu menu = new PopupMenu(view.getContext(),view);
                    menu.setGravity(Gravity.END);
                    menu.getMenu().add("Sửa").setOnMenuItemClickListener(menuItem -> {
                        Intent i12 = new Intent(view.getContext(),EditNote.class);
                        i12.putExtra("title", note.getTitle());
                        i12.putExtra("content", note.getContent());
                        i12.putExtra("expiredDate",expireddateCharSeq);
                        i12.putExtra("setExpiredDate",setexpireddateCharSeq);
                        i12.putExtra("noteID",docID);
                        startActivity(i12);
                        return false;
                    });
                    menu.getMenu().add("Xóa").setOnMenuItemClickListener(menuItem -> {
                        AlertDialog.Builder message = new AlertDialog.Builder(getActivity());
                        message.setTitle(R.string.message_title);
                        message.setMessage(R.string.message_content);
                        message.setNegativeButton(R.string.yes, (dialog, which) -> {
                            DocumentReference docref = FirebaseFirestore.getInstance().collection("notes").document(user.getUid()).collection("myNotes").document(docID);
                            docref.delete().addOnSuccessListener(aVoid -> {
                                //Xóa
                            }).addOnFailureListener(e -> Toast.makeText(getActivity(),"Không thể xóa ghi chú",Toast.LENGTH_LONG));
                        }).setPositiveButton(R.string.no, (dialogInterface, i1) -> {
                        }).show();
                        return false;
                    });
                    menu.show();
                });

                long ts1 = new Timestamp(new Date()).getSeconds();
                long ts2 = note.getExpiredDate().getSeconds();
                DocumentSnapshot snapshot = noteAdapter.getSnapshots().getSnapshot(i);
                if(ts1 >= ts2 && !note.getComplete()){
                    snapshot.getReference().update("expired",true);

                }
                else {
                    snapshot.getReference().update("expired",false);

                }
                if(note.getExpired() && !note.getComplete()){
                    noteViewHolder.expired.setVisibility(View.VISIBLE);
                }
                else {
                    noteViewHolder.expired.setVisibility(View.GONE);
                }
            }

            @NonNull
            @Override
            public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.note_view,parent,false);
                return new NoteViewHolder(view);
            }
        };
    }

    public class NoteViewHolder extends RecyclerView.ViewHolder{
        TextView noteTitle, noteContent, noteDate;
        View view;
        CardView mCardView;
        CheckBox noteCheckBox, notePriority;
        ImageView doneIV, contentDone,expired;
        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            noteTitle = itemView.findViewById(R.id.titles);
            noteContent = itemView.findViewById(R.id.content);
            noteDate = itemView.findViewById(R.id.dateCreate);
            noteCheckBox = itemView.findViewById(R.id.doneCheckBox);
            mCardView = itemView.findViewById(R.id.noteCard);
            doneIV = itemView.findViewById(R.id.doneIV);
            contentDone = itemView.findViewById(R.id.contentDone);
            view = itemView;
            notePriority = itemView.findViewById(R.id.priority);
            expired = itemView.findViewById(R.id.expired);
            noteCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                    DocumentSnapshot snapshot = noteAdapter.getSnapshots().getSnapshot(getAdapterPosition());
                    Note note = noteAdapter.getItem(getAdapterPosition());
                    if (note.getComplete() != isChecked) {
                        snapshot.getReference().update("complete", isChecked);
                    }
                    if (isChecked){
                        doneIV.setVisibility(View.VISIBLE);
                        contentDone.setVisibility(View.VISIBLE);
                        expired.setVisibility(View.GONE);
                        snapshot.getReference().update("expired", false);
                    }
                    else {
                        doneIV.setVisibility(View.GONE);
                        contentDone.setVisibility(View.GONE);
                    }
                }
            });

            notePriority.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                    DocumentSnapshot snapshot = noteAdapter.getSnapshots().getSnapshot(getAdapterPosition());
                    Note note = noteAdapter.getItem(getAdapterPosition());
                    if (note.getPriority() != isChecked) {
                        snapshot.getReference().update("priority", isChecked);
                    }
                }
            });
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_all_notes, container, false);
        noteLists = view.findViewById(R.id.noteList);
        noteLists.setLayoutManager(new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL));
        noNotes = view.findViewById(R.id.noNotes);
        noText = view.findViewById(R.id.noText);
        noteLists.setAdapter(noteAdapter);
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT  ) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }
            public void deleteItem(int position) {
                noteAdapter.getSnapshots().getSnapshot(position).getReference().delete();
                DocumentReference documentReference = noteAdapter.getSnapshots().getSnapshot(position).getReference();
                Note note = noteAdapter.getSnapshots().getSnapshot(position).toObject(Note.class);
                Snackbar.make(noteLists, "Ghi chú đã bị xóa", Snackbar.LENGTH_LONG)
                        .setActionTextColor(ContextCompat.getColor(getActivity(), R.color.red))
                        .setAction("Hoàn Tác", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                documentReference.set(note);
                            }
                        }).show();
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                deleteItem(viewHolder.getAdapterPosition());
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                        .addBackgroundColor(ContextCompat.getColor(getActivity(), R.color.red))
                        .addActionIcon(R.drawable.ic_baseline_delete_sweep_24)
                        .addSwipeLeftLabel("Xóa")
                        .addSwipeRightLabel("Xóa")
                        .setSwipeLeftLabelColor(ContextCompat.getColor(getActivity(), R.color.white))
                        .setSwipeRightLabelColor(ContextCompat.getColor(getActivity(), R.color.white))
                        .create()
                        .decorate();
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        }).attachToRecyclerView(noteLists);
        return view;
    }

    private int getRandomColor(){
        List<Integer> colorCode = new ArrayList<>();
        colorCode.add(R.color.blue);
        colorCode.add(R.color.yellow);
        colorCode.add(R.color.skyblue);
        colorCode.add(R.color.lightPurple);
        colorCode.add(R.color.lightGreen);
        colorCode.add(R.color.gray);
        colorCode.add(R.color.pink);
        colorCode.add(R.color.red);
        colorCode.add(R.color.greenlight);
        colorCode.add(R.color.colorPrimary);
        colorCode.add(R.color.colorAccent);
        colorCode.add(R.color.light_blue_A200);
        colorCode.add(R.color.light_blue_A400);

        Random randomColor = new Random();
        int number = randomColor.nextInt(colorCode.size());
        return colorCode.get(number);
    }

    @Override
    public void onStart() {
        super.onStart();
        noteAdapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        if(noteAdapter != null){
            noteAdapter.startListening();
        }
    }
}