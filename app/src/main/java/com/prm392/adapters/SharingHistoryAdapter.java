package com.prm392.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.prm392.model.SharingHistory;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class SharingHistoryAdapter extends RecyclerView.Adapter<SharingHistoryAdapter.ViewHolder> {

    private List<SharingHistory> historyList;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.US);

    public SharingHistoryAdapter(List<SharingHistory> historyList) {
        this.historyList = historyList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_2, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SharingHistory history = historyList.get(position);
        holder.text1.setText("Recipient: " + history.getRecipient() + " (" + history.getStatus() + ")");
        holder.text2.setText("Date: " + dateFormat.format(history.getDate()));
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }

    public void updateList(List<SharingHistory> newList) {
        historyList = newList;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView text1, text2;

        ViewHolder(View itemView) {
            super(itemView);
            text1 = itemView.findViewById(android.R.id.text1);
            text2 = itemView.findViewById(android.R.id.text2);
        }
    }
}