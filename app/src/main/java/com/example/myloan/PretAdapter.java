package com.example.myloan;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

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
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        PretBancaire pret = getItem(position);
        if (pret == null) return convertView;

        // Remplissage des champs
        holder.tvNomClient.setText(pret.getNomClient());
        holder.tvNomBanque.setText(pret.getNomBanque());
        holder.tvDatePret.setText("Date : " + pret.getDatePret());
        holder.tvMontant.setText(String.format("Montant : %,.0f Ar", pret.getMontant()));
        holder.tvMontantAPayer.setText(String.format("%,.0f Ar", pret.getMontantAPayer()));

        // Boutons action
        holder.btnModifier.setOnClickListener(v -> listener.onEdit(pret));
        holder.btnSupprimer.setOnClickListener(v -> listener.onDelete(pret));

        return convertView;
    }

    // ViewHolder pour éviter les findViewById répétitifs
    static class ViewHolder {
        TextView tvNomClient, tvNomBanque, tvDatePret, tvMontant, tvMontantAPayer;
        Button btnModifier, btnSupprimer;

        ViewHolder(View view) {
            tvNomClient     = view.findViewById(R.id.tvNomClient);
            tvNomBanque     = view.findViewById(R.id.tvNomBanque);
            tvDatePret      = view.findViewById(R.id.tvDatePret);
            tvMontant       = view.findViewById(R.id.tvMontant);
            tvMontantAPayer = view.findViewById(R.id.tvMontantAPayer);
            btnModifier     = view.findViewById(R.id.btnModifier);
            btnSupprimer    = view.findViewById(R.id.btnSupprimer);
        }
    }
}