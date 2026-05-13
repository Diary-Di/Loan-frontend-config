package com.example.myloan;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class PretAdapter extends ArrayAdapter<PretBancaire> {

    public interface OnItemActionListener {
        void onEdit(PretBancaire pret);
        void onDelete(PretBancaire pret);
    }

    private final OnItemActionListener listener;

    public PretAdapter(Context context, List<PretBancaire> list, OnItemActionListener listener) {
        super(context, 0, list);
        this.listener = listener;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_pret, parent, false);

            holder = new ViewHolder();
            holder.tvNomClient     = convertView.findViewById(R.id.tvNomClient);
            holder.tvNomBanque     = convertView.findViewById(R.id.tvNomBanque);
            holder.tvDatePret      = convertView.findViewById(R.id.tvDatePret);
            holder.tvMontant       = convertView.findViewById(R.id.tvMontant);
            holder.tvMontantAPayer = convertView.findViewById(R.id.tvMontantAPayer);
            holder.btnModifier     = convertView.findViewById(R.id.btnModifier);
            holder.btnSupprimer    = convertView.findViewById(R.id.btnSupprimer);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        // Marges verticales entre les éléments
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        int margin = (int) (12 * getContext().getResources().getDisplayMetrics().density);
        params.setMargins(0, margin, 0, margin);
        convertView.setLayoutParams(params);

        // Bordure de séparation
        MaterialCardView card = (MaterialCardView) convertView;
        card.setStrokeColor(Color.parseColor("#E0E0E0"));
        card.setStrokeWidth(1);

        // Coins arrondis uniquement sur le premier et dernier élément
        float radius = 16 * getContext().getResources().getDisplayMetrics().density;
        boolean isFirst = (position == 0);
        boolean isLast  = (position == getCount() - 1);

        card.setShapeAppearanceModel(card.getShapeAppearanceModel()
                .toBuilder()
                .setTopLeftCornerSize(isFirst ? radius : 0)
                .setTopRightCornerSize(isFirst ? radius : 0)
                .setBottomLeftCornerSize(isLast ? radius : 0)
                .setBottomRightCornerSize(isLast ? radius : 0)
                .build());

        PretBancaire pret = getItem(position);
        if (pret == null) return convertView;

        // Remplissage des données
        holder.tvNomClient.setText(pret.getNomClient());
        holder.tvNomBanque.setText(pret.getNomBanque());
        holder.tvDatePret.setText("Date : " + pret.getDatePret());
        holder.tvMontant.setText(String.format("Montant : %,.0f Ar", pret.getMontant()));
        holder.tvMontantAPayer.setText(String.format("%,.0f Ar", pret.getMontantAPayer()));

        // Couleurs des icônes
        holder.btnModifier.setColorFilter(Color.parseColor("#FB8C00"), PorterDuff.Mode.SRC_IN);
        holder.btnSupprimer.setColorFilter(Color.parseColor("#C62828"), PorterDuff.Mode.SRC_IN);

        // Boutons
        holder.btnModifier.setOnClickListener(v -> listener.onEdit(pret));
        holder.btnSupprimer.setOnClickListener(v -> listener.onDelete(pret));

        return convertView;
    }

    static class ViewHolder {
        TextView tvNomClient, tvNomBanque, tvDatePret, tvMontant, tvMontantAPayer;
        ImageView btnModifier, btnSupprimer;
    }
}