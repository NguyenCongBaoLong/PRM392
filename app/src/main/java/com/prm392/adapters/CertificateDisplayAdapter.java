package com.prm392.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.prm392.R;
import com.prm392.model.Certificate;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class CertificateDisplayAdapter extends RecyclerView.Adapter<CertificateDisplayAdapter.CertificateViewHolder> {

    private final List<Certificate> certificateList;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    public CertificateDisplayAdapter(List<Certificate> certificateList) {
        this.certificateList = certificateList;
    }

    @NonNull
    @Override
    public CertificateViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Sử dụng layout mới để hiển thị chi tiết (ví dụ: item_certificate_display)
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_certificate_display, parent, false);
        return new CertificateViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CertificateViewHolder holder, int position) {
        Certificate cert = certificateList.get(position);

        // Sử dụng các Getter/Setter đã chuẩn hóa (getName, getOrganization)
        holder.tvCertName.setText(cert.getCertificateName());
        holder.tvCertOrganization.setText("Tổ chức: " + cert.getIssuingOrganization());
        holder.tvCertCredentialId.setText("Mã: " + (cert.getCredentialId() != null ? cert.getCredentialId() : "N/A"));

        if (cert.getExpirationDate() != null) {
            String expiryDate = dateFormat.format(cert.getExpirationDate());
            holder.tvCertExpiry.setText("Hết hạn: " + expiryDate);
        } else {
            holder.tvCertExpiry.setText("Hết hạn: Vĩnh viễn");
        }
    }

    @Override
    public int getItemCount() {
        return certificateList.size();
    }

    public static class CertificateViewHolder extends RecyclerView.ViewHolder {
        final TextView tvCertName;
        final TextView tvCertOrganization;
        final TextView tvCertCredentialId;
        final TextView tvCertExpiry;

        public CertificateViewHolder(@NonNull View itemView) {
            super(itemView);
            // Ánh xạ các thành phần từ item_certificate_display.xml
            tvCertName = itemView.findViewById(R.id.tv_display_cert_name);
            tvCertOrganization = itemView.findViewById(R.id.tv_display_cert_organization);
            tvCertCredentialId = itemView.findViewById(R.id.tv_display_cert_credential_id);
            tvCertExpiry = itemView.findViewById(R.id.tv_display_cert_expiry);
        }
    }
}