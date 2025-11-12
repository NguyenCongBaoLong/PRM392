package com.prm392.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.prm392.R;
import com.prm392.adapters.SharingHistoryAdapter;
import com.prm392.model.SharingHistory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SharingHistoryActivity extends AppCompatActivity {
    private static final String TAG = "SharingHistoryActivity";
    private RecyclerView recyclerView;
    private SharingHistoryAdapter adapter;
    private List<SharingHistory> historyList;
    private Spinner spinnerSort;
    private Button btnBack;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sharing_history);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        recyclerView = findViewById(R.id.recycler_view_history);
        spinnerSort = findViewById(R.id.spinner_sort);
        btnBack = findViewById(R.id.btn_back);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        historyList = new ArrayList<>();
        adapter = new SharingHistoryAdapter(historyList);
        recyclerView.setAdapter(adapter);

        setupSortSpinner();
        loadSharingHistory();

        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(SharingHistoryActivity.this, MyAccountActivity.class);
            startActivity(intent);
            finish();  // Optional: Closes SharingHistoryActivity to clean up the stack
        });
    }

    private void setupSortSpinner() {
        String[] sortOptions = {"Date (Newest First)", "Date (Oldest First)", "Status (Success First)", "Status (Failed First)"};
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, sortOptions);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSort.setAdapter(spinnerAdapter);

        spinnerSort.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                sortAndRefreshList(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                sortAndRefreshList(0);
            }
        });
    }

    private void loadSharingHistory() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Please log in to view history.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        String userId = currentUser.getUid();
        Log.d(TAG, "Current User UID: " + userId);  // Debug: Kiểm tra UID để so với Firestore console
        historyList.clear();

        db.collection("sharing_history")
                .whereEqualTo("userId", userId)  // Filter theo userId, tương tự MyCertificateActivity
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            SharingHistory history = document.toObject(SharingHistory.class);
                            history.setId(document.getId());
                            historyList.add(history);
                        }
                        Log.d(TAG, "Fetched " + historyList.size() + " history items");  // Debug: Kiểm tra số lượng fetch
                        if (historyList.isEmpty()) {
                            Toast.makeText(this, "No sharing history found for this user", Toast.LENGTH_SHORT).show();
                        }
                        sortAndRefreshList(spinnerSort.getSelectedItemPosition());
                    } else {
                        Log.w(TAG, "Error getting history: ", task.getException());
                        Toast.makeText(this, "Failed to load history: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void sortAndRefreshList(int sortMode) {
        // Null-safe comparator, tương tự fix trước
        Comparator<SharingHistory> nullSafeTimestampComparator = (h1, h2) -> {
            com.google.firebase.Timestamp t1 = h1.getTimestamp();
            com.google.firebase.Timestamp t2 = h2.getTimestamp();
            if (t1 == null && t2 == null) return 0;
            if (t1 == null) return 1;
            if (t2 == null) return -1;
            return t1.compareTo(t2);
        };

        try {
            switch (sortMode) {
                case 0: // Date Newest First
                    Collections.sort(historyList, (h1, h2) -> nullSafeTimestampComparator.compare(h2, h1));
                    break;
                case 1: // Date Oldest First
                    Collections.sort(historyList, nullSafeTimestampComparator::compare);
                    break;
                case 2: // Status Success First
                    Collections.sort(historyList, (h1, h2) -> {
                        String s1 = h1.getStatus() != null ? h1.getStatus() : "";
                        String s2 = h2.getStatus() != null ? h2.getStatus() : "";
                        return s2.compareToIgnoreCase(s1);
                    });
                    break;
                case 3: // Status Failed First
                    Collections.sort(historyList, (h1, h2) -> {
                        String s1 = h1.getStatus() != null ? h1.getStatus() : "";
                        String s2 = h2.getStatus() != null ? h2.getStatus() : "";
                        return s1.compareToIgnoreCase(s2);
                    });
                    break;
            }
            adapter.notifyDataSetChanged();
        } catch (Exception e) {
            Log.e(TAG, "Sorting failed: " + e.getMessage(), e);
            Toast.makeText(this, "Error sorting history", Toast.LENGTH_SHORT).show();
        }
    }
}