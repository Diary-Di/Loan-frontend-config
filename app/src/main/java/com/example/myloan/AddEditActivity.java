package com.example.myloan;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Calendar;
import java.util.Locale;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddEditActivity extends AppCompatActivity {

    public static final String EXTRA_PRET = "extra_pret";

    private EditText etNomClient, etNomBanque, etMontant, etDatePret, etTauxPret;
    private TextView tvTitreFormulaire, tvPreviewMAP;
    private Button btnEnregistrer, btnAnnuler;

    private PretBancaire pretAModifier = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit);

        initViews();
        setupDatePicker();
        setupPreviewMAP();
        setupAmountFormatting();

        if (getIntent().hasExtra(EXTRA_PRET)) {
            pretAModifier = (PretBancaire) getIntent().getSerializableExtra(EXTRA_PRET);
            remplirFormulaire(pretAModifier);
        }

        btnEnregistrer.setOnClickListener(v -> enregistrer());
        btnAnnuler.setOnClickListener(v -> finish());
    }

    private void initViews() {
        tvTitreFormulaire = findViewById(R.id.tvTitreFormulaire);
        etNomClient       = findViewById(R.id.etNomClient);
        etNomBanque       = findViewById(R.id.etNomBanque);
        etMontant         = findViewById(R.id.etMontant);
        etDatePret        = findViewById(R.id.etDatePret);
        etTauxPret        = findViewById(R.id.etTauxPret);
        tvPreviewMAP      = findViewById(R.id.tvPreviewMAP);
        btnEnregistrer    = findViewById(R.id.btnEnregistrer);
        btnAnnuler        = findViewById(R.id.btnAnnuler);
    }

    private void setupDatePicker() {
        etDatePret.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            new DatePickerDialog(this,
                    (view, year, month, day) -> {
                        String date = String.format("%04d-%02d-%02d", year, month + 1, day);
                        etDatePret.setText(date);
                    },
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)
            ).show();
        });
    }

    private void setupAmountFormatting() {
        etMontant.addTextChangedListener(new TextWatcher() {
            private String current = "";
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().equals(current)) {
                    etMontant.removeTextChangedListener(this);

                    String cleanString = s.toString().replaceAll("[\\s,]", "");
                    if (!cleanString.isEmpty()) {
                        try {
                            double parsed = Double.parseDouble(cleanString);
                            DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.FRENCH);
                            symbols.setGroupingSeparator(' ');
                            DecimalFormat df = new DecimalFormat("#,###", symbols);
                            String formatted = df.format(parsed);
                            
                            current = formatted;
                            etMontant.setText(formatted);
                            etMontant.setSelection(formatted.length());
                        } catch (NumberFormatException e) {
                            // ignore
                        }
                    } else {
                        current = "";
                    }
                    etMontant.addTextChangedListener(this);
                }
            }
        });
    }

    private void setupPreviewMAP() {
        TextWatcher watcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                calculerPreview();
            }
        };
        etMontant.addTextChangedListener(watcher);
        etTauxPret.addTextChangedListener(watcher);
    }

    private void calculerPreview() {
        try {
            String montantStr = etMontant.getText().toString().replaceAll("[\\s,]", "");
            double montant = Double.parseDouble(montantStr);
            double taux    = Double.parseDouble(etTauxPret.getText().toString()) / 100.0;
            double map     = montant * (1 + taux);
            
            DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.FRENCH);
            symbols.setGroupingSeparator(' ');
            DecimalFormat df = new DecimalFormat("#,###", symbols);
            tvPreviewMAP.setText(df.format(map) + " Ar");
        } catch (NumberFormatException e) {
            tvPreviewMAP.setText("—");
        }
    }

    private void remplirFormulaire(PretBancaire pret) {
        tvTitreFormulaire.setText("Modifier un prêt");
        btnEnregistrer.setText("Modifier");
        etNomClient.setText(pret.getNomClient());
        etNomBanque.setText(pret.getNomBanque());
        
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.FRENCH);
        symbols.setGroupingSeparator(' ');
        DecimalFormat df = new DecimalFormat("#,###", symbols);
        etMontant.setText(df.format(pret.getMontant()));
        
        etDatePret.setText(pret.getDatePret());
        etTauxPret.setText(String.valueOf(pret.getTauxPret() * 100.0));
    }

    private void enregistrer() {
        String nomClient = etNomClient.getText().toString().trim();
        String nomBanque = etNomBanque.getText().toString().trim();
        String montantStr = etMontant.getText().toString().replaceAll("[\\s,]", "").trim();
        String datePret  = etDatePret.getText().toString().trim();
        String tauxStr   = etTauxPret.getText().toString().trim();

        if (nomClient.isEmpty() || nomBanque.isEmpty() ||
                montantStr.isEmpty() || datePret.isEmpty() || tauxStr.isEmpty()) {
            Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
            return;
        }

        double montant, taux;
        try {
            montant = Double.parseDouble(montantStr);
            taux    = Double.parseDouble(tauxStr) / 100.0;
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Montant ou taux invalide", Toast.LENGTH_SHORT).show();
            return;
        }

        PretBancaire pret = new PretBancaire(nomClient, nomBanque, montant, datePret, taux);
        ApiService api = RetrofitClient.getInstance(this).create(ApiService.class);

        if (pretAModifier == null) {
            api.addPret(pret).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(AddEditActivity.this, "Prêt ajouté !", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        Toast.makeText(AddEditActivity.this, "Erreur : " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }
                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Toast.makeText(AddEditActivity.this, "Erreur réseau", Toast.LENGTH_LONG).show();
                }
            });
        } else {
            pret.setNumCompte(pretAModifier.getNumCompte());
            api.updatePret(pret).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(AddEditActivity.this, "Prêt modifié !", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        Toast.makeText(AddEditActivity.this, "Erreur : " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }
                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Toast.makeText(AddEditActivity.this, "Erreur réseau", Toast.LENGTH_LONG).show();
                }
            });
        }
    }
}