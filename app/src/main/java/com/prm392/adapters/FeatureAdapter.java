package com.prm392.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.prm392.R;
import com.prm392.model.Feature;

import java.util.List;

public class FeatureAdapter extends RecyclerView.Adapter<FeatureAdapter.FeatureViewHolder> {

    private final List<Feature> featureList;

    public FeatureAdapter(List<Feature> featureList) {
        this.featureList = featureList;
    }

    public static class FeatureViewHolder extends RecyclerView.ViewHolder {
        public TextView titleTextView;

        public FeatureViewHolder(View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.tv_feature_title);
        }
    }

    @NonNull
    @Override
    public FeatureViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_feature, parent, false);
        return new FeatureViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull FeatureViewHolder holder, int position) {
        Feature currentItem = featureList.get(position);

        holder.titleTextView.setText(currentItem.getTitle());


        holder.itemView.setOnClickListener(v -> {
            Context context = v.getContext();
            Class<?> target = currentItem.getTargetActivity();

            Intent intent = new Intent(context, target);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return featureList.size();
    }
}