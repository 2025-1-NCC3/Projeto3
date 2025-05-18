package br.com.fecapccp.uber.remote;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    // ATENÇÃO: Este URL deve ser o teu URL do ngrok ou o URL público do teu servidor
    // Vou usar o que me forneceste: https://98c6-2804-1b3-bd71-70a6-d74f-8cb-574.ngrk-free.app
    // Lembra-te de adicionar "/api/" ao final, pois as rotas no servidor estão prefixadas com /api
    private static final String BASE_URL = "\n" + "https://6a86-2804-1b3-abc2-64bf-39b4-6d18-bed5-d86e.ngrok-free.app/api/";

    private static Retrofit retrofit = null;

    public static Retrofit getClient() {
        if (retrofit == null) {
            // Interceptor para logs (útil para debug)
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(loggingInterceptor)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    public static ApiService getApiService() {
        return getClient().create(ApiService.class);
    }
}

