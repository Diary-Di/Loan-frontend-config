package com.example.myloan;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private ListView listViewPrets;
    private TextView tvTotal, tvMin, tvMax;
    private BottomNavigationView bottomNavigation;

    private List<PretBancaire> listePrets = new ArrayList<>();
    private PretAdapter adapter;
    private ApiService apiService;

    private final ActivityResultLauncher<Intent> addEditLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK) {
                            chargerPrets();
                        }
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupApiService();
        setupBottomNavigation();
        setupAdapter();

        chargerPrets();
    }

    private void initViews() {
        listViewPrets = findViewById(R.id.listViewPrets);
        tvTotal = findViewById(R.id.tvTotal);
        tvMin = findViewById(R.id.tvMin);
        tvMax = findViewById(R.id.tvMax);
        bottomNavigation = findViewById(R.id.bottom_navigation);
    }

    private void setupApiService() {
        apiService = RetrofitClient.getInstance(this).create(ApiService.class);
    }

    private void setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_add) {
                // Bouton Ajouter au milieu
                Intent intent = new Intent(MainActivity.this, AddEditActivity.class);
                addEditLauncher.launch(intent);
                return true;

            } else if (id == R.id.nav_chart) {
                ouvrirGraphique();
                // Revenir automatiquement sur l'onglet Liste
                bottomNavigation.setSelectedItemId(R.id.nav_list);
                return true;
            }
            return true;
        });
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

    private void chargerPrets() {
        if (apiService == null) return;

        apiService.getAllPrets().enqueue(new Callback<List<PretBancaire>>() {
            @Override
            public void onResponse(Call<List<PretBancaire>> call, Response<List<PretBancaire>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listePrets.clear();
                    listePrets.addAll(response.body());
                    adapter.notifyDataSetChanged();
                    calculerStats();

                    // Toast temporaire pour debug
                    // Toast.makeText(MainActivity.this, listePrets.size() + " prêts chargés", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Erreur chargement", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<PretBancaire>> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Erreur réseau", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void calculerStats() {
        if (listePrets.isEmpty()) {
            tvTotal.setText("0 Ar");
            tvMin.setText("0 Ar");
            tvMax.setText("0 Ar");
            return;
        }

        double total = 0, min = Double.MAX_VALUE, max = Double.MIN_VALUE;

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
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Erreur suppression", Toast.LENGTH_SHORT).show();
            }
        });
    }

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