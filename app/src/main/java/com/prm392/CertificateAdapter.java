package com.prm392;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.prm392.activity.DashboardActivity.OnItemClickListener;
import com.prm392.model.Certificate;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class CertificateAdapter extends RecyclerView.Adapter<CertificateAdapter.CertificateViewHolder> implements Filterable {

    private List<Certificate> certificateList;
    private List<Certificate> certificateListFull;
    private final OnItemClickListener listener;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private static final long WARNING_THRESHOLD_DAYS = 60;

    public CertificateAdapter(List<Certificate> certificateList, OnItemClickListener listener) {
        this.certificateList = certificateList;
        this.certificateListFull = new ArrayList<>(certificateList);
        this.listener = listener;
    }

    public static class CertificateViewHolder extends RecyclerView.ViewHolder {
        public TextView nameTextView;
        public TextView issuerTextView;
        public TextView expiryDateTextView;

        public CertificateViewHolder(View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.tv_certificate_name);
            issuerTextView = itemView.findViewById(R.id.tv_issuer);
            expiryDateTextView = itemView.findViewById(R.id.tv_expiration_date);
        }
    }

    @NonNull
    @Override
    public CertificateViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_certificate, parent, false);
        return new CertificateViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull CertificateViewHolder holder, int position) {
        Certificate currentItem = certificateList.get(position);

        holder.nameTextView.setText(currentItem.getCertificateName());
        holder.issuerTextView.setText("Tổ chức: " + currentItem.getIssuingOrganization());

        Date expiryDate = currentItem.getExpirationDate();

        if (expiryDate != null) {
            holder.expiryDateTextView.setText("Hết hạn: " + dateFormat.format(expiryDate));

            long diffInMillies = expiryDate.getTime() - new Date().getTime();
            long diffInDays = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);

            if (diffInDays <= 0) {
                holder.expiryDateTextView.setTextColor(Color.RED);
                holder.expiryDateTextView.setText("ĐÃ HẾT HẠN: " + dateFormat.format(expiryDate));
            } else if (diffInDays <= WARNING_THRESHOLD_DAYS) {
                holder.expiryDateTextView.setTextColor(Color.parseColor("#FFA500")); // Màu Cam
            } else {
                holder.expiryDateTextView.setTextColor(Color.BLACK);
            }
        } else {
            holder.expiryDateTextView.setText("Trạng thái: Vĩnh viễn");
            holder.expiryDateTextView.setTextColor(Color.parseColor("#006400"));
        }


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onItemClick(currentItem);
            }
        });
    }

    @Override
    public int getItemCount() {
        return certificateList.size();
    }


    @Override
    public Filter getFilter() {
        return certificateFilter;
    }

    private Filter certificateFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<Certificate> filteredList = new ArrayList<>();

            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(certificateListFull);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();

                for (Certificate item : certificateListFull) {
                    if (item.getCertificateName().toLowerCase().contains(filterPattern) ||
                            item.getIssuingOrganization().toLowerCase().contains(filterPattern)) {
                        filteredList.add(item);
                    }
                }
            }

            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            certificateList.clear();
            certificateList.addAll((List<Certificate>) results.values);
            notifyDataSetChanged();
        }
    };
}