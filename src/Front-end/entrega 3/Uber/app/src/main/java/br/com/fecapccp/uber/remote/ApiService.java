package br.com.fecapccp.uber.remote;

import java.util.List;

import br.com.fecapccp.uber.model.AvaliacaoZona;
import br.com.fecapccp.uber.model.LoginRequest;
import br.com.fecapccp.uber.model.LoginResponse;
import br.com.fecapccp.uber.model.PassageiroCadastroRequest;
import br.com.fecapccp.uber.model.Zona;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ApiService {
    // Métodos existentes para passageiros, login, etc.

    @POST("passageiros")
    Call<Object> cadastrarPassageiro(@Body PassageiroCadastroRequest request);

    // Método para login de passageiro
    @POST("passageiros/login")
    Call<LoginResponse> loginPassageiro(@Body LoginRequest loginRequest);

    // Métodos para zonas
    @GET("zonas")
    Call<List<Zona>> getZonas();

    @GET("zonas/{id}")
    Call<Zona> getZonaById(@Path("id") String id);

    // Métodos para avaliações de zonas
    @GET("avaliacoes_zona/zona/{zonaId}")
    Call<List<AvaliacaoZona>> getAvaliacoesByZonaId(@Path("zonaId") String zonaId);

    @POST("avaliacoes_zona")
    Call<Object> enviarAvaliacaoZona(@Body AvaliacaoZona avaliacao);
}
