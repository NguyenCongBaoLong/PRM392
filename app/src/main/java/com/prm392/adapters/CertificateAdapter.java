package com.prm392.adapters;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
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
import java.util.Objects; // Thêm import Objects

// implements Filterable để hỗ trợ tìm kiếm
public class CertificateAdapter extends RecyclerView.Adapter<CertificateAdapter.CertificateViewHolder> implements Filterable {

    private final List<Certificate> originalCertificateList;
    private List<Certificate> filteredCertificateList;
    private final OnItemClickListener listener;

    // Màu mặc định
    private static final int DEFAULT_DATE_COLOR = Color.BLUE; // Hoặc Color.GRAY nếu muốn
    private static final int PERMANENT_COLOR = Color.parseColor("#336633"); // Xanh lá đậm
    private static final int NEAR_EXPIRY_COLOR = Color.parseColor("#FF9800"); // Cam
    private static final int EXPIRED_COLOR = Color.RED; // Đỏ

    public interface OnItemClickListener {
        void onItemClick(Certificate certificate);
    }

    public CertificateAdapter(List<Certificate> certificateList, OnItemClickListener listener) {
        // Sử dụng Objects.requireNonNull để đảm bảo list đầu vào không null
        this.originalCertificateList = Objects.requireNonNull(certificateList, "Certificate list must not be null");
        this.filteredCertificateList = new ArrayList<>(originalCertificateList);
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
            // Đảm bảo R.id khớp với layout item_certificate
            nameTextView = itemView.findViewById(R.id.tv_certificate_name);
            issuerTextView = itemView.findViewById(R.id.tv_issuer);
            expiryDateTextView = itemView.findViewById(R.id.tv_expiration_date);
            detailButton = itemView.findViewById(R.id.btn_view_detail);
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
        if (position >= filteredCertificateList.size()) return; // Kiểm tra an toàn

        Certificate currentItem = filteredCertificateList.get(position);

        // 1. Hiển thị dữ liệu cơ bản, xử lý null an toàn
        // (Giả định getCertificateName và getIssuingOrganization không trả về null)
        holder.nameTextView.setText(currentItem.getCertificateName());
        holder.issuerTextView.setText("Tổ chức: " + currentItem.getIssuingOrganization());

        // Reset màu về mặc định trước khi kiểm tra logic hạn sử dụng
        holder.expiryDateTextView.setTextColor(DEFAULT_DATE_COLOR);

        // 2. Logic cảnh báo và hạn sử dụng
        Date expiryDateObj = currentItem.getExpirationDate();

        if (expiryDateObj != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            String expiryDate = sdf.format(expiryDateObj);

            long diff = expiryDateObj.getTime() - System.currentTimeMillis();
            long diffDays = diff / (24 * 60 * 60 * 1000);

            if (diffDays < 0) {
                // Đã hết hạn
                holder.expiryDateTextView.setText("Hạn: " + expiryDate + " (Đã hết hạn)");
                holder.expiryDateTextView.setTextColor(EXPIRED_COLOR);
            } else if (diffDays <= 30) {
                // Sắp hết hạn (trong vòng 30 ngày)
                holder.expiryDateTextView.setText("Hạn: " + expiryDate + " (Sắp hết hạn)");
                holder.expiryDateTextView.setTextColor(NEAR_EXPIRY_COLOR);
            } else {
                holder.expiryDateTextView.setText("Hạn: " + expiryDate);
                holder.expiryDateTextView.setTextColor(DEFAULT_DATE_COLOR);
            }
        } else {
            holder.expiryDateTextView.setText("Vĩnh Viễn");
            holder.expiryDateTextView.setTextColor(PERMANENT_COLOR);
        }

        holder.detailButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(currentItem);
            }
        });
    }

    @Override
    public int getItemCount() {
        return filteredCertificateList.size();
    }

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
                String filterPattern = normalizeVietnamese(constraint.toString());

                for (Certificate certificate : originalCertificateList) {

                    if (certificate == null) continue;

                    String normalizedName = normalizeVietnamese(certificate.getCertificateName());
                    String normalizedIssuer = normalizeVietnamese(certificate.getIssuingOrganization());

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
                // Ép kiểu an toàn hơn bằng cách kiểm tra
                if (results.values instanceof List) {
                    filteredCertificateList.addAll((List<Certificate>) results.values);
                } else {
                    Log.e("CertificateAdapter", "FilterResults.values không phải là List<Certificate>");
                }
            }
            notifyDataSetChanged();
        }
    };
}