package com.prm392.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.prm392.R;
import com.prm392.model.Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;

public class CertificateSelectionAdapter extends RecyclerView.Adapter<CertificateSelectionAdapter.CertViewHolder> {

    private final List<Certificate> certificateList;
    private final Set<String> selectedCertificateIds = new HashSet<>();

    public CertificateSelectionAdapter(List<Certificate> certificateList) {
        this.certificateList = certificateList;
    }

    @NonNull
    @Override
    public CertViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_certificate_selection, parent, false);
        return new CertViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CertViewHolder holder, int position) {
        Certificate certificate = certificateList.get(position);
        holder.tvCertName.setText(certificate.getCertificateName());
        holder.tvIssuer.setText("Tổ chức: " + certificate.getIssuingOrganization());

        final String certId = certificate.getId();
        holder.checkBox.setChecked(selectedCertificateIds.contains(certId));

        holder.checkBox.setOnCheckedChangeListener(null); // Xóa listener cũ
        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                selectedCertificateIds.add(certId);
            } else {
                selectedCertificateIds.remove(certId);
            }
        });

        // Xử lý click trên toàn bộ item để toggle CheckBox
        holder.itemView.setOnClickListener(v -> holder.checkBox.performClick());
    }

    @Override
    public int getItemCount() {
        return certificateList.size();
    }

    public List<String> getSelectedCertificateIds() {
        return new ArrayList<>(selectedCertificateIds);
    }

    static class CertViewHolder extends RecyclerView.ViewHolder {
        final CheckBox checkBox;
        final TextView tvCertName;
        final TextView tvIssuer;

        public CertViewHolder(@NonNull View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.checkbox_select);
            tvCertName = itemView.findViewById(R.id.tv_cert_name_selection);
            tvIssuer = itemView.findViewById(R.id.tv_issuer_selection);
        }
    }
}