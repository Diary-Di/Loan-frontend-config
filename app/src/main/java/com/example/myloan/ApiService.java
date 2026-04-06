package com.example.myloan;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Query;

public interface ApiService {

    // --------------------------------------------------------
    //  GET — Récupérer tous les prêts
    //  GET — Récupérer un seul prêt par num_compte
    // --------------------------------------------------------
    @GET("api.php")
    Call<List<PretBancaire>> getAllPrets();

    @GET("api.php")
    Call<PretBancaire> getPretById(@Query("num_compte") int numCompte);

    // --------------------------------------------------------
    //  POST — Ajouter un nouveau prêt
    //  Body JSON : { nom_client, nom_banque, montant, date_pret, taux_pret }
    // --------------------------------------------------------
    @POST("api.php")
    Call<ResponseBody> addPret(@Body PretBancaire pret);

    // --------------------------------------------------------
    //  PUT — Modifier un prêt existant
    //  Body JSON : { num_compte, nom_client, nom_banque, montant, date_pret, taux_pret }
    // --------------------------------------------------------
    @PUT("api.php")
    Call<ResponseBody> updatePret(@Body PretBancaire pret);

    // --------------------------------------------------------
    //  DELETE — Supprimer un prêt par num_compte
    // --------------------------------------------------------
    @DELETE("api.php")
    Call<ResponseBody> deletePret(@Query("num_compte") int numCompte);
}