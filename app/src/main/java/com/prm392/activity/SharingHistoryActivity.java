package com.prm392.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.prm392.R;
import com.prm392.adapters.SharingHistoryAdapter;
import com.prm392.model.SharingHistory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SharingHistoryActivity extends AppCompatActivity {

    private RecyclerView rvHistory;
    private Button btnSortDate, btnSortStatus;
    private SharingHistoryAdapter adapter;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String userUid;
    private List<SharingHistory> historyList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sharing_history);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        userUid = mAuth.getCurrentUser().getUid();

        rvHistory = findViewById(R.id.rv_sharing_history);
        btnSortDate = findViewById(R.id.btn_sort_date);
        btnSortStatus = findViewById(R.id.btn_sort_status);

        rvHistory.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SharingHistoryAdapter(historyList);
        rvHistory.setAdapter(adapter);

        fetchSharingHistory();

        btnSortDate.setOnClickListener(v -> sortByDate());
        btnSortStatus.setOnClickListener(v -> sortByStatus());
    }

    private void fetchSharingHistory() {
        db.collection("sharing_history")
                .whereEqualTo("userUid", userUid)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot snapshot = task.getResult();
                        historyList = snapshot.toObjects(SharingHistory.class);
                        adapter.updateList(historyList);
                    } else {
                        Toast.makeText(this, "Error fetching history: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void sortByDate() {
        Collections.sort(historyList, (h1, h2) -> h2.getDate().compareTo(h1.getDate()));  // Newest first
        adapter.updateList(historyList);
    }

    private void sortByStatus() {
        Collections.sort(historyList, (h1, h2) -> h1.getStatus().compareTo(h2.getStatus()));  // Alphabetical by status
        adapter.updateList(historyList);
    }
}