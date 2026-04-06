package com.example.myloan;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private ListView listViewPrets;
    private TextView tvTotal, tvMin, tvMax;
    private Button btnAjouter, btnChart;

    private List<PretBancaire> listePrets = new ArrayList<>();
    private PretAdapter adapter;
    private ApiService apiService;

    // Lance AddEditActivity et recharge la liste au retour
    private final ActivityResultLauncher<Intent> addEditLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK) {
                            chargerPrets();
                        }
                    }
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        apiService = RetrofitClient.getInstance(this).create(ApiService.class);

        initViews();
        setupAdapter();

        btnAjouter.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddEditActivity.class);
            addEditLauncher.launch(intent);
        });

        btnChart.setOnClickListener(v -> ouvrirGraphique());

        chargerPrets();
    }

    private void initViews() {
        listViewPrets = findViewById(R.id.listViewPrets);
        tvTotal       = findViewById(R.id.tvTotal);
        tvMin         = findViewById(R.id.tvMin);
        tvMax         = findViewById(R.id.tvMax);
        btnAjouter    = findViewById(R.id.btnAjouter);
        btnChart      = findViewById(R.id.btnChart);
    }

    private void setupAdapter() {
        adapter = new PretAdapter(this, listePrets, new PretAdapter.OnItemActionListener() {
            @Override
            public void onEdit(PretBancaire pret) {
                Intent intent = new Intent(MainActivity.this, AddEditActivity.class);
                intent.putExtra(AddEditActivity.EXTRA_PRET, (java.io.Serializable) pret);
                addEditLauncher.launch(intent);
            }

            @Override
            public void onDelete(PretBancaire pret) {
                confirmerSuppression(pret);
            }
        });
        listViewPrets.setAdapter(adapter);
    }

    // --------------------------------------------------------
    //  Chargement de tous les prêts depuis l'API
    // --------------------------------------------------------
    private void chargerPrets() {
        apiService.getAllPrets().enqueue(new Callback<List<PretBancaire>>() {
            @Override
            public void onResponse(Call<List<PretBancaire>> call, Response<List<PretBancaire>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listePrets.clear();
                    listePrets.addAll(response.body());
                    adapter.notifyDataSetChanged();
                    calculerStats();
                } else {
                    Toast.makeText(MainActivity.this,
                            "Erreur chargement : " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<PretBancaire>> call, Throwable t) {
                Toast.makeText(MainActivity.this,
                        "Erreur réseau : " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    // --------------------------------------------------------
    //  Calcul des statistiques (total, min, max)
    // --------------------------------------------------------
    private void calculerStats() {
        if (listePrets.isEmpty()) {
            tvTotal.setText("—");
            tvMin.setText("—");
            tvMax.setText("—");
            return;
        }

        double total = 0;
        double min   = Double.MAX_VALUE;
        double max   = Double.MIN_VALUE;

        for (PretBancaire p : listePrets) {
            double map = p.getMontantAPayer();
            total += map;
            if (map < min) min = map;
            if (map > max) max = map;
        }

        tvTotal.setText(String.format("%,.0f Ar", total));
        tvMin.setText(String.format("%,.0f Ar", min));
        tvMax.setText(String.format("%,.0f Ar", max));
    }

    // --------------------------------------------------------
    //  Dialogue de confirmation avant suppression
    // --------------------------------------------------------
    private void confirmerSuppression(PretBancaire pret) {
        new AlertDialog.Builder(this)
                .setTitle("Supprimer")
                .setMessage("Supprimer le prêt de " + pret.getNomClient() + " ?")
                .setPositiveButton("Supprimer", (dialog, which) -> supprimerPret(pret))
                .setNegativeButton("Annuler", null)
                .show();
    }

    private void supprimerPret(PretBancaire pret) {
        apiService.deletePret(pret.getNumCompte()).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(MainActivity.this, "Prêt supprimé", Toast.LENGTH_SHORT).show();
                    chargerPrets();
                } else {
                    Toast.makeText(MainActivity.this,
                            "Erreur suppression : " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(MainActivity.this,
                        "Erreur réseau : " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    // --------------------------------------------------------
    //  Ouvrir l'écran graphique avec les stats calculées
    // --------------------------------------------------------
    private void ouvrirGraphique() {
        if (listePrets.isEmpty()) {
            Toast.makeText(this, "Aucune donnée à afficher", Toast.LENGTH_SHORT).show();
            return;
        }

        double total = 0, min = Double.MAX_VALUE, max = Double.MIN_VALUE;
        for (PretBancaire p : listePrets) {
            double map = p.getMontantAPayer();
            total += map;
            if (map < min) min = map;
            if (map > max) max = map;
        }

        Intent intent = new Intent(this, ChartActivity.class);
        intent.putExtra(ChartActivity.EXTRA_TOTAL, total);
        intent.putExtra(ChartActivity.EXTRA_MIN, min);
        intent.putExtra(ChartActivity.EXTRA_MAX, max);
        startActivity(intent);
    }
}