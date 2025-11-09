package com.prm392.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.os.Bundle;

import com.prm392.adapters.FeatureAdapter;
import com.prm392.R;
import com.prm392.model.Feature;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        recyclerView = findViewById(R.id.recycler_view_features);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        List<Feature> featureList = createFeatureList();

        FeatureAdapter adapter = new FeatureAdapter(featureList);
        recyclerView.setAdapter(adapter);
    }


    private List<Feature> createFeatureList() {
        List<Feature> list = new ArrayList<>();

        list.add(new Feature("My Certificates", MyCertificateActivity.class));
        list.add(new Feature("Public Showcase Profile", PublicProfileSetupActivity.class));

        list.add(new Feature("User Profile", LoginActivity.class));
        list.add(new Feature("Upload New Certificate", UploadCertificateActivity.class));

        return list;
    }
}