package com.example.myloan;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

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
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.List;

public class ChartActivity extends AppCompatActivity {

    public static final String EXTRA_TOTAL = "extra_total";
    public static final String EXTRA_MIN   = "extra_min";
    public static final String EXTRA_MAX   = "extra_max";

    private BarChart barChart;
    private PieChart pieChart;
    private Button btnHistogramme, btnCamembert, btnRetour;
    private TextView tvChartTotal, tvChartMin, tvChartMax;

    private double total, min, max;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart);

        // Récupération des valeurs passées par MainActivity
        total = getIntent().getDoubleExtra(EXTRA_TOTAL, 0);
        min   = getIntent().getDoubleExtra(EXTRA_MIN, 0);
        max   = getIntent().getDoubleExtra(EXTRA_MAX, 0);

        initViews();
        afficherStats();
        setupHistogramme();
        setupCamembert();

        // Afficher l'histogramme par défaut
        afficherHistogramme();

        btnHistogramme.setOnClickListener(v -> afficherHistogramme());
        btnCamembert.setOnClickListener(v -> afficherCamembert());
        btnRetour.setOnClickListener(v -> finish());
    }

    private void initViews() {
        barChart       = findViewById(R.id.barChart);
        pieChart       = findViewById(R.id.pieChart);
        btnHistogramme = findViewById(R.id.btnHistogramme);
        btnCamembert   = findViewById(R.id.btnCamembert);
        btnRetour      = findViewById(R.id.btnRetour);
        tvChartTotal   = findViewById(R.id.tvChartTotal);
        tvChartMin     = findViewById(R.id.tvChartMin);
        tvChartMax     = findViewById(R.id.tvChartMax);
    }

    private void afficherStats() {
        tvChartTotal.setText(String.format("%,.0f Ar", total));
        tvChartMin.setText(String.format("%,.0f Ar", min));
        tvChartMax.setText(String.format("%,.0f Ar", max));
    }

    private void setupHistogramme() {
        List<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0f, (float) total));
        entries.add(new BarEntry(1f, (float) min));
        entries.add(new BarEntry(2f, (float) max));

        BarDataSet dataSet = new BarDataSet(entries, "Montant à payer (Ar)");
        dataSet.setColors(
                0xFF1565C0,  // bleu pour total
                0xFF2E7D32,  // vert pour min
                0xFFC62828   // rouge pour max
        );
        dataSet.setValueTextSize(11f);

        BarData barData = new BarData(dataSet);
        barChart.setData(barData);

        // Étiquettes de l'axe X
        String[] labels = {"Total", "Min", "Max"};
        barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        barChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        barChart.getXAxis().setGranularity(1f);
        barChart.getXAxis().setDrawGridLines(false);

        barChart.getAxisRight().setEnabled(false);
        barChart.getDescription().setEnabled(false);
        barChart.getLegend().setEnabled(true);
        barChart.animateY(800);
        barChart.invalidate();
    }

    private void setupCamembert() {
        List<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry((float) total, "Total"));
        entries.add(new PieEntry((float) min,   "Min"));
        entries.add(new PieEntry((float) max,   "Max"));

        PieDataSet dataSet = new PieDataSet(entries, "Montant à payer");
        dataSet.setColors(
                0xFF1565C0,
                0xFF2E7D32,
                0xFFC62828
        );
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

    private void afficherHistogramme() {
        barChart.setVisibility(View.VISIBLE);
        pieChart.setVisibility(View.GONE);
        btnHistogramme.setBackgroundTintList(
                getColorStateList(android.R.color.holo_blue_dark));
        btnCamembert.setBackgroundTintList(
                getColorStateList(android.R.color.holo_blue_light));
    }

    private void afficherCamembert() {
        barChart.setVisibility(View.GONE);
        pieChart.setVisibility(View.VISIBLE);
        btnCamembert.setBackgroundTintList(
                getColorStateList(android.R.color.holo_blue_dark));
        btnHistogramme.setBackgroundTintList(
                getColorStateList(android.R.color.holo_blue_light));
    }
}