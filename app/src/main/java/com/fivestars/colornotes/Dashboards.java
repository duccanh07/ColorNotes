package com.fivestars.colornotes;

import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fivestars.colornotes.note.Completed;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Objects;

public class Dashboards extends Fragment {
    FirebaseFirestore fStore;
    FirebaseUser user;
    FirebaseAuth fAuth;
    public Dashboards() {
        // Required empty public constructor
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboards, container, false);
        PieChart pieChart = view.findViewById(R.id.pieChart);

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        user = fAuth.getCurrentUser();

        Query completeTrue = fStore.collection("notes")
                .document(user.getUid())
                .collection("myNotes")
                .whereEqualTo("complete",true);

        Query completeFalse = fStore.collection("notes")
                .document(user.getUid())
                .collection("myNotes")
                .whereEqualTo("complete",false)
                .whereEqualTo("expired",false);

        Query expired = fStore.collection("notes")
                .document(user.getUid())
                .collection("myNotes")
                .whereEqualTo("expired",true);

        ArrayList<PieEntry> dashboard = new ArrayList<>();

        PieDataSet pieDataSet = new PieDataSet(dashboard, "");
        pieDataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        pieDataSet.setValueTextColor(Color.BLACK);
        pieDataSet.setValueTextSize(16f);
        pieDataSet.setSliceSpace(3f);
        pieDataSet.setSelectionShift(10f);

        PieData pieData = new PieData(pieDataSet);
        pieData.setValueFormatter(new PercentFormatter(pieChart));

        pieChart.setEntryLabelColor(Color.BLACK);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(Color.TRANSPARENT);
        pieChart.setHoleRadius(7f);
        pieChart.setTransparentCircleRadius(10f);
        pieChart.setUsePercentValues(true);
        pieChart.getDescription().setEnabled(false);
        pieChart.setRotationAngle(0f);
        pieChart.setRotationEnabled(true);
        pieChart.animate();
        pieChart.notifyDataSetChanged();

        Legend l = pieChart.getLegend();
        l.setDirection(Legend.LegendDirection.LEFT_TO_RIGHT);
        l.setTextSize(20f);
        l.setXEntrySpace(7f);
        l.setYEntrySpace(5f);
        l.setTextColor(Color.WHITE);

        completeTrue.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    dashboard.add(new PieEntry(calcNote(Objects.requireNonNull(task.getResult())),"Đã hoàn thành"));
                    pieChart.setData(pieData);
                }
            }
        });

        completeFalse.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    dashboard.add(new PieEntry(calcNote(Objects.requireNonNull(task.getResult())),"Chưa hoàn thành"));
                    pieChart.setData(pieData);
                }
            }
        });

        expired.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    dashboard.add(new PieEntry(calcNote(Objects.requireNonNull(task.getResult())),"Hết hạn"));
                    pieChart.setData(pieData);
                }
            }
        });
        return view;
    }

    private int calcNote (QuerySnapshot documents){
        int totalNote = 0;
        for(QueryDocumentSnapshot snapshots: documents){
            String sComplete = snapshots.getString("count");
            int xComplete = Integer.parseInt(sComplete);
            totalNote += xComplete;
        }
        return totalNote;
    }
}