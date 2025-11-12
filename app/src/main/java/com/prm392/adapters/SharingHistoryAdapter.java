package com.prm392.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.prm392.R;
import com.prm392.model.SharingHistory;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class SharingHistoryAdapter extends RecyclerView.Adapter<SharingHistoryAdapter.ViewHolder> {
    private final List<SharingHistory> historyList;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

    public SharingHistoryAdapter(List<SharingHistory> historyList) {
        this.historyList = historyList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sharing_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SharingHistory history = historyList.get(position);

        // Tham khảo CertificateDisplayAdapter: Sử dụng getter và null-check chặt chẽ
        holder.tvShareType.setText("Type: " + (history.getShareType() != null ? history.getShareType() : "N/A"));
        holder.tvCertificateId.setText("Cert ID: " + (history.getCertificateId() != null ? history.getCertificateId() : "N/A"));
        holder.tvStatus.setText("Status: " + (history.getStatus() != null ? history.getStatus() : "N/A"));

        if (history.getTimestamp() != null) {
            String formattedDate = dateFormat.format(history.getTimestamp().toDate());
            holder.tvTimestamp.setText("Date: " + formattedDate);
        } else {
            holder.tvTimestamp.setText("Date: N/A");  // Tương tự "Hết hạn: Vĩnh viễn" trong CertificateDisplayAdapter
        }
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvShareType;
        final TextView tvCertificateId;
        final TextView tvStatus;
        final TextView tvTimestamp;

        ViewHolder(View itemView) {
            super(itemView);
            tvShareType = itemView.findViewById(R.id.tv_share_type);
            tvCertificateId = itemView.findViewById(R.id.tv_certificate_id);
            tvStatus = itemView.findViewById(R.id.tv_status);
            tvTimestamp = itemView.findViewById(R.id.tv_timestamp);
        }
    }
}