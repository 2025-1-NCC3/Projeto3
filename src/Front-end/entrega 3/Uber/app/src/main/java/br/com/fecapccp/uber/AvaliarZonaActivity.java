package br.com.fecapccp.uber;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.ArrayAdapter;

import androidx.appcompat.app.AppCompatActivity;

import br.com.fecapccp.uber.model.AvaliacaoZona;
import br.com.fecapccp.uber.remote.ApiService;
import br.com.fecapccp.uber.remote.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AvaliarZonaActivity extends AppCompatActivity {

    private String zonaId;
    private String userId;
    private String tipoUsuario;

    private RatingBar ratingBar;
    private EditText editComentario;
    private Spinner spinnerTipoAlerta;
    private EditText editLocalizacao;
    private Button btnEnviar;
    private Button btnCancelar;

    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_avaliar_zona);

        // Inicializar o ApiService
        apiService = RetrofitClient.getApiService();

        // Recuperar dados da Intent
        zonaId = getIntent().getStringExtra("ZONA_ID");
        userId = getIntent().getStringExtra("USER_ID");
        tipoUsuario = getIntent().getStringExtra("USER_TIPO");

        if (zonaId == null || userId == null || tipoUsuario == null) {
            Toast.makeText(this, "Erro: Dados incompletos para avaliação", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        // No método onCreate da AvaliarZonaActivity
        userId = getIntent().getStringExtra("USER_ID");
        if (userId == null || userId.length() != 24) { // ObjectIds têm 24 caracteres
            userId = "507f191e810c19729de860ea"; // ID de teste válido
        }


        // Inicializar componentes da UI
        ratingBar = findViewById(R.id.ratingBarAvaliar);
        editComentario = findViewById(R.id.editComentario);
        spinnerTipoAlerta = findViewById(R.id.spinnerTipoAlerta);
        editLocalizacao = findViewById(R.id.editLocalizacao);
        btnEnviar = findViewById(R.id.btnEnviarAvaliacao);
        btnCancelar = findViewById(R.id.btnCancelarAvaliacao);

        // Configurar o spinner de tipos de alerta
        String[] tiposAlerta = {"Nenhum", "Acidente na via", "Trânsito intenso", "Veículo quebrado",
                "Obras na pista", "Nevoeiro denso", "Área perigosa", "Fiscalização"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, tiposAlerta);
        spinnerTipoAlerta.setAdapter(adapter);

        // Configurar listeners
        btnEnviar.setOnClickListener(v -> enviarAvaliacao());
        btnCancelar.setOnClickListener(v -> finish());
    }

    private void enviarAvaliacao() {
        // Validar classificação
        float classificacao = ratingBar.getRating();
        if (classificacao < 1) {
            Toast.makeText(this, "Por favor, dê pelo menos 1 estrela", Toast.LENGTH_SHORT).show();
            return;
        }

        // Obter outros dados
        String comentario = editComentario.getText().toString().trim();
        String tipoAlerta = spinnerTipoAlerta.getSelectedItemPosition() > 0 ?
                spinnerTipoAlerta.getSelectedItem().toString() : null;
        String localizacao = editLocalizacao.getText().toString().trim();

        // Criar objeto de avaliação
        AvaliacaoZona avaliacao = new AvaliacaoZona();
        avaliacao.setZonaId(zonaId);
        avaliacao.setUsuarioId(userId);
        avaliacao.setTipoUsuario(tipoUsuario);
        avaliacao.setClassificacaoEstrelas(classificacao);

        if (!comentario.isEmpty()) {
            avaliacao.setComentario(comentario);
        }

        if (tipoAlerta != null) {
            avaliacao.setTipoAlerta(tipoAlerta);
        }

        if (!localizacao.isEmpty()) {
            avaliacao.setLocalizacaoAproximada(localizacao);
        }

        // Desabilitar botão para evitar múltiplos envios
        btnEnviar.setEnabled(false);

        // Enviar para o backend
        Call<Object> call = apiService.enviarAvaliacaoZona(avaliacao);
        call.enqueue(new Callback<Object>() {
            @Override
            public void onResponse(Call<Object> call, Response<Object> response) {
                if (response.isSuccessful()) {
                    // código existente
                } else {
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "Sem detalhes";
                        Log.e("AvaliarZona", "Erro ao enviar avaliação: " + response.code() + " - " + errorBody);
                        Toast.makeText(AvaliarZonaActivity.this,
                                "Erro ao enviar avaliação: " + response.code() + " - " + errorBody,
                                Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Log.e("AvaliarZona", "Erro ao ler errorBody", e);
                    }
                    btnEnviar.setEnabled(true);
                }
            }


            @Override
            public void onFailure(Call<Object> call, Throwable t) {
                btnEnviar.setEnabled(true);
                Toast.makeText(AvaliarZonaActivity.this,
                        "Falha ao enviar avaliação: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("AvaliarZona", "Falha ao enviar avaliação", t);
            }
        });
    }
}
