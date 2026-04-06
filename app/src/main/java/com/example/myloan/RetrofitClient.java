package com.example.myloan;

import android.content.Context;

import com.example.myloan.R;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static Retrofit retrofit = null;

    public static Retrofit getInstance(Context context) {
        if (retrofit == null) {

            // Intercepteur qui ajoute l'en-tête ngrok
            // (évite l'erreur 429 sur les appels API depuis Android)
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(chain -> {
                        Request request = chain.request().newBuilder()
                                .addHeader("ngrok-skip-browser-warning", "true")
                                .build();
                        return chain.proceed(request);
                    })
                    .build();

            // L'URL est lue depuis res/values/strings.xml
            String baseUrl = context.getString(R.string.base_url);

            retrofit = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    // Empêcher l'instanciation directe
    private RetrofitClient() {}
}