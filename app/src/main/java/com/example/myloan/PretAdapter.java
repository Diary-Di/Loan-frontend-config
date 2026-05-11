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

        PretBancaire pret = getItem(position);
        if (pret == null) return convertView;

        // Remplissage des données
        holder.tvNomClient.setText(pret.getNomClient());
        holder.tvNomBanque.setText(pret.getNomBanque());
        holder.tvDatePret.setText("Date : " + pret.getDatePret());
        holder.tvMontant.setText(String.format("Montant : %,.0f Ar", pret.getMontant()));
        holder.tvMontantAPayer.setText(String.format("%,.0f Ar", pret.getMontantAPayer()));

        // Boutons
        holder.btnModifier.setOnClickListener(v -> listener.onEdit(pret));
        holder.btnSupprimer.setOnClickListener(v -> listener.onDelete(pret));

        return convertView;
    }

    // ViewHolder
    static class ViewHolder {
        TextView tvNomClient, tvNomBanque, tvDatePret, tvMontant, tvMontantAPayer;
        Button btnModifier, btnSupprimer;
    }
}