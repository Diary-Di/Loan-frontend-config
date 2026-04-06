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

import java.util.Calendar;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddEditActivity extends AppCompatActivity {

    // Clé pour passer un prêt existant via Intent (mode modification)
    public static final String EXTRA_PRET = "extra_pret";

    private EditText etNomClient, etNomBanque, etMontant, etDatePret, etTauxPret;
    private TextView tvTitreFormulaire, tvPreviewMAP;
    private Button btnEnregistrer, btnAnnuler;

    private PretBancaire pretAModifier = null; // null = mode ajout

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit);

        initViews();
        setupDatePicker();
        setupPreviewMAP();

        // Vérifier si on est en mode modification
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

    // Ouvre un DatePickerDialog au clic sur le champ date
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

    // Calcule et affiche le montant à payer en temps réel
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
            double montant = Double.parseDouble(etMontant.getText().toString());
            double taux    = Double.parseDouble(etTauxPret.getText().toString());
            double map     = montant + montant * (1 + taux);
            tvPreviewMAP.setText(String.format("%,.0f Ar", map));
        } catch (NumberFormatException e) {
            tvPreviewMAP.setText("—");
        }
    }

    // Pré-remplir les champs en mode modification
    private void remplirFormulaire(PretBancaire pret) {
        tvTitreFormulaire.setText("Modifier un prêt");
        btnEnregistrer.setText("Modifier");
        etNomClient.setText(pret.getNomClient());
        etNomBanque.setText(pret.getNomBanque());
        etMontant.setText(String.valueOf(pret.getMontant()));
        etDatePret.setText(pret.getDatePret());
        etTauxPret.setText(String.valueOf(pret.getTauxPret()));
    }

    private void enregistrer() {
        // Validation des champs
        String nomClient = etNomClient.getText().toString().trim();
        String nomBanque = etNomBanque.getText().toString().trim();
        String montantStr = etMontant.getText().toString().trim();
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
            taux    = Double.parseDouble(tauxStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Montant ou taux invalide", Toast.LENGTH_SHORT).show();
            return;
        }

        // Construction de l'objet
        PretBancaire pret = new PretBancaire(nomClient, nomBanque, montant, datePret, taux);

        ApiService api = RetrofitClient.getInstance(this).create(ApiService.class);

        if (pretAModifier == null) {
            // Mode ajout
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
                    Toast.makeText(AddEditActivity.this, "Erreur réseau : " + t.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        } else {
            // Mode modification
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
                    Toast.makeText(AddEditActivity.this, "Erreur réseau : " + t.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }
    }
}