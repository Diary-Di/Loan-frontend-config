package com.example.myloan;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class PretBancaire implements Serializable {

    @SerializedName("num_compte")
    private int numCompte;

    @SerializedName("nom_client")
    private String nomClient;

    @SerializedName("nom_banque")
    private String nomBanque;

    @SerializedName("montant")
    private double montant;

    @SerializedName("date_pret")
    private String datePret;

    @SerializedName("taux_pret")
    private double tauxPret;

    // --------------------------------------------------------
    //  Constructeurs
    // --------------------------------------------------------

    public PretBancaire() {}

    public PretBancaire(String nomClient, String nomBanque,
                        double montant, String datePret, double tauxPret) {
        this.nomClient = nomClient;
        this.nomBanque = nomBanque;
        this.montant   = montant;
        this.datePret  = datePret;
        this.tauxPret  = tauxPret;
    }

    // --------------------------------------------------------
    //  Formule métier : Montant à payer
    //  MAP = montant + montant * (1 + taux)
    // --------------------------------------------------------
    public double getMontantAPayer() {
        return montant * (1 + tauxPret);
    }

    // --------------------------------------------------------
    //  Getters
    // --------------------------------------------------------
    public int getNumCompte()    { return numCompte; }
    public String getNomClient() { return nomClient; }
    public String getNomBanque() { return nomBanque; }
    public double getMontant()   { return montant; }
    public String getDatePret()  { return datePret; }
    public double getTauxPret()  { return tauxPret; }

    // --------------------------------------------------------
    //  Setters
    // --------------------------------------------------------
    public void setNumCompte(int numCompte)       { this.numCompte = numCompte; }
    public void setNomClient(String nomClient)    { this.nomClient = nomClient; }
    public void setNomBanque(String nomBanque)    { this.nomBanque = nomBanque; }
    public void setMontant(double montant)        { this.montant = montant; }
    public void setDatePret(String datePret)      { this.datePret = datePret; }
    public void setTauxPret(double tauxPret)      { this.tauxPret = tauxPret; }
}