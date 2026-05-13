package com.example.myloan;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.material.button.MaterialButtonToggleGroup;

import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private ListView listViewPrets;
    private TextView tvTotal, tvMin, tvMax, tvListeTitre;
    private LinearLayout navList, navChart, sectionGraphes;
    private List<PretBancaire> listePrets = new ArrayList<>();
    private PretAdapter adapter;
    private ApiService apiService;
    private com.google.android.material.floatingactionbutton.FloatingActionButton fabAdd;

    // Graphes
    private BarChart barChart;
    private PieChart pieChart;
    private MaterialButtonToggleGroup toggleGroup;

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
        setupChartButtons();

        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddEditActivity.class);
            addEditLauncher.launch(intent);
        });

        setupAdapter();
        chargerPrets();
    }

    private void initViews() {
        listViewPrets  = findViewById(R.id.listViewPrets);
        tvTotal        = findViewById(R.id.tvTotal);
        tvMin          = findViewById(R.id.tvMin);
        tvMax          = findViewById(R.id.tvMax);
        tvListeTitre  = findViewById(R.id.tvListeTitre);
        fabAdd         = findViewById(R.id.fab_add);
        navList        = findViewById(R.id.nav_list);
        navChart       = findViewById(R.id.nav_chart);
        sectionGraphes = findViewById(R.id.sectionGraphes);
        barChart       = findViewById(R.id.barChart);
        pieChart       = findViewById(R.id.pieChart);
        toggleGroup    = findViewById(R.id.toggleGroup);
    }

    private void setupApiService() {
        apiService = RetrofitClient.getInstance(this).create(ApiService.class);
    }

    private void setupBottomNavigation() {
        navList.setOnClickListener(v -> afficherListe());
        navChart.setOnClickListener(v -> afficherGraphes());
    }

    private void afficherListe() {
        tvListeTitre.setText(R.string.title_mes_prets);
        listViewPrets.setVisibility(View.VISIBLE);
        sectionGraphes.setVisibility(View.GONE);
        fabAdd.setVisibility(View.VISIBLE);
    }

    private void afficherGraphes() {
        if (listePrets.isEmpty()) {
            Toast.makeText(this, "Aucune donnée à afficher", Toast.LENGTH_SHORT).show();
            return;
        }
        tvListeTitre.setText(R.string.title_representation_graphique);
        listViewPrets.setVisibility(View.GONE);
        sectionGraphes.setVisibility(View.VISIBLE);
        fabAdd.setVisibility(View.VISIBLE);
        setupHistogramme();
        setupCamembert();
        
        // Afficher le graphe correspondant au bouton sélectionné
        if (toggleGroup.getCheckedButtonId() == R.id.btnCamembert) {
            afficherCamembert();
        } else {
            afficherHistogramme();
        }
    }

    private void setupChartButtons() {
        toggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.btnHistogramme) {
                    afficherHistogramme();
                } else if (checkedId == R.id.btnCamembert) {
                    afficherCamembert();
                }
            }
        });
    }

    private void afficherHistogramme() {
        barChart.setVisibility(View.VISIBLE);
        pieChart.setVisibility(View.GONE);
    }

    private void afficherCamembert() {
        barChart.setVisibility(View.GONE);
        pieChart.setVisibility(View.VISIBLE);
    }

    private void setupHistogramme() {
        double total = 0, min = Double.MAX_VALUE, max = Double.MIN_VALUE;
        for (PretBancaire p : listePrets) {
            double map = p.getMontantAPayer();
            total += map;
            if (map < min) min = map;
            if (map > max) max = map;
        }

        List<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0f, (float) total));
        entries.add(new BarEntry(1f, (float) min));
        entries.add(new BarEntry(2f, (float) max));

        BarDataSet dataSet = new BarDataSet(entries, "Montant à payer (Ar)");
        dataSet.setColors(0xFF1565C0, 0xFF2E7D32, 0xFFC62828);
        dataSet.setValueTextSize(11f);

        BarData barData = new BarData(dataSet);
        barChart.setData(barData);

        String[] labels = {"Total", "Min", "Max"};
        barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        barChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        barChart.getXAxis().setGranularity(1f);
        barChart.getXAxis().setDrawGridLines(false);
        barChart.getAxisRight().setEnabled(false);
        barChart.getDescription().setEnabled(false);
        barChart.animateY(800);
        barChart.invalidate();
    }

    private void setupCamembert() {
        double total = 0, min = Double.MAX_VALUE, max = Double.MIN_VALUE;
        for (PretBancaire p : listePrets) {
            double map = p.getMontantAPayer();
            total += map;
            if (map < min) min = map;
            if (map > max) max = map;
        }

        List<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry((float) total, "Total"));
        entries.add(new PieEntry((float) min,   "Min"));
        entries.add(new PieEntry((float) max,   "Max"));

        PieDataSet dataSet = new PieDataSet(entries, "Montant à payer");
        dataSet.setColors(0xFF1565C0, 0xFF2E7D32, 0xFFC62828);
        dataSet.setValueTextSize(12f);
        dataSet.setSliceSpace(3f);

        PieData pieData = new PieData(dataSet);
        pieChart.setData(pieData);
        pieChart.setUsePercentValues(true);
        pieChart.getDescription().setEnabled(false);
        pieChart.setEntryLabelTextSize(12f);
        pieChart.animateY(800);
        pieChart.invalidate();
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
}
