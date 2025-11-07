package com.prm392.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.prm392.R;
import com.prm392.model.Certificate;

import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

// implements Filterable để hỗ trợ tìm kiếm
public class CertificateAdapter extends RecyclerView.Adapter<CertificateAdapter.CertificateViewHolder> implements Filterable {

    private final List<Certificate> originalCertificateList;
    private List<Certificate> filteredCertificateList;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Certificate certificate);
    }

    public CertificateAdapter(List<Certificate> certificateList, OnItemClickListener listener) {
        this.originalCertificateList = certificateList;
        this.filteredCertificateList = new ArrayList<>(certificateList);
        this.listener = listener;
    }

    private String normalizeVietnamese(String s) {
        if (s == null) return "";
        s = Normalizer.normalize(s, Normalizer.Form.NFD);
        s = s.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        s = s.replaceAll("\\s", "");
        return s.toLowerCase(Locale.getDefault());
    }

    // --- ViewHolder ---
    public static class CertificateViewHolder extends RecyclerView.ViewHolder {
        public TextView nameTextView;
        public TextView issuerTextView;
        public TextView expiryDateTextView;
        public Button detailButton;

        public CertificateViewHolder(View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.tv_certificate_name);
            issuerTextView = itemView.findViewById(R.id.tv_issuer);
            expiryDateTextView = itemView.findViewById(R.id.tv_expiration_date);
            detailButton = itemView.findViewById(R.id.btn_view_detail); // Nút Xem Detail/Edit
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
        Certificate currentItem = filteredCertificateList.get(position);
        Context context = holder.itemView.getContext();

        // 1. Hiển thị dữ liệu
        holder.nameTextView.setText(currentItem.getCertificateName());
        holder.issuerTextView.setText("Tổ chức: " + currentItem.getIssuingOrganization());

        Date expiryDateObj = currentItem.getExpirationDate();

        // 2. Logic cảnh báo và khắc phục lỗi NPE
        if (expiryDateObj != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            String expiryDate = sdf.format(expiryDateObj);


            long diff = expiryDateObj.getTime() - System.currentTimeMillis();
            long diffDays = diff / (24 * 60 * 60 * 1000);

            if (diffDays <= 30 && diffDays >= 0) {
                holder.expiryDateTextView.setText("Hạn: " + expiryDate + " (Sắp hết hạn)");
                holder.expiryDateTextView.setTextColor(Color.parseColor("#FF9800")); // Cam (Sắp hết hạn)
            } else if (diffDays < 0) {
                holder.expiryDateTextView.setText("Hạn: " + expiryDate + " (Đã hết hạn)");
                holder.expiryDateTextView.setTextColor(Color.RED); // Đỏ (Đã hết hạn)
            } else {
                holder.expiryDateTextView.setText("Hạn: " + expiryDate);
                holder.expiryDateTextView.setTextColor(Color.BLUE);
            }
        } else {
            // Chứng chỉ Vĩnh Viễn (NULL Date)
            holder.expiryDateTextView.setText("Vĩnh Viễn");
            holder.expiryDateTextView.setTextColor(Color.parseColor("#336633"));

        // 3. Xử lý sự kiện click (mở màn hình Detail)

        }
        holder.detailButton.setOnClickListener(v -> {
            listener.onItemClick(currentItem);
        });
    }

    @Override
    public int getItemCount() {
        return filteredCertificateList.size();
    }

    // --- Filterable implementation ---
    @Override
    public Filter getFilter() {
        return certificateFilter;
    }

    private final Filter certificateFilter = new Filter() {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();
            List<Certificate> filteredList = new ArrayList<>();

            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(originalCertificateList);
            } else {

                // Chuẩn hóa chuỗi tìm kiếm
                String filterPattern = normalizeVietnamese(constraint.toString());

                for (Certificate certificate : originalCertificateList) {

                    // Chuẩn hóa dữ liệu để so sánh
                    String normalizedName = normalizeVietnamese(certificate.getCertificateName());
                    String normalizedIssuer = normalizeVietnamese(certificate.getIssuingOrganization());

                    // So sánh chuỗi đã được chuẩn hóa (Tìm kiếm chính)
                    if (normalizedName.contains(filterPattern) || normalizedIssuer.contains(filterPattern)) {
                        filteredList.add(certificate);
                    }
                }
            }

            results.values = filteredList;
            results.count = filteredList.size();
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            filteredCertificateList.clear();
            if (results.values != null) {
                filteredCertificateList.addAll((List<Certificate>) results.values);
            }
            notifyDataSetChanged();
        }
    };
}