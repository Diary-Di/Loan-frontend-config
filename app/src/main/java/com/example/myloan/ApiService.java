package com.example.myloan;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    @GET("api.php")
    Call<List<PretBancaire>> getAllPrets();

    @GET("api.php")
    Call<PretBancaire> getPretById(@Query("num_compte") int numCompte);

    @POST("api.php")
    Call<ResponseBody> addPret(@Body PretBancaire pret);

    @PUT("api.php")
    Call<ResponseBody> updatePret(@Body PretBancaire pret);

    @DELETE("api.php")                                          // ← simple
    Call<ResponseBody> deletePret(@Query("num_compte") int numCompte); // ← @Query
}