package br.com.fecapccp.uber;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import br.com.fecapccp.uber.model.PassageiroCadastroRequest;
import br.com.fecapccp.uber.remote.ApiService;
import br.com.fecapccp.uber.remote.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CadastroP extends AppCompatActivity {
    private Button btnvoltar;
    private Button btnConcluirCadastroP;

    // Campos de input para cadastro do passageiro
    private EditText editTextNomeP;
    private EditText editTextEmailP;
    private EditText editTextTelefoneP;
    private EditText editTextSenhaP; // Assumindo que há um campo para senha

    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cadastro_p);

        // Inicializar ApiService
        apiService = RetrofitClient.getApiService();

        // Referenciar os campos de input (os IDs devem corresponder ao seu layout activity_cadastro_p.xml)
        // Se os IDs forem diferentes, por favor, ajuste-os aqui.
        editTextNomeP = findViewById(R.id.editTextNome); // Exemplo de ID, ajuste conforme seu XML
        editTextEmailP = findViewById(R.id.editTextEmail); // Exemplo de ID, ajuste conforme seu XML
        editTextTelefoneP = findViewById(R.id.editTextTelefone); // Exemplo de ID, ajuste conforme seu XML
        editTextSenhaP = findViewById(R.id.editTextSenha); // Exemplo de ID, ajuste conforme seu XML

        btnvoltar = findViewById(R.id.voltar);
        btnvoltar.setOnClickListener(view -> {
            Intent voltarIntent = new Intent(this, MainActivity.class); // Corrigido para voltar para MainActivity
            startActivity(voltarIntent);
        });

        btnConcluirCadastroP = findViewById(R.id.concluir); // Mantendo o ID original do seu botão
        btnConcluirCadastroP.setOnClickListener(view -> {
            cadastrarPassageiro();
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void cadastrarPassageiro() {
        String nome = editTextNomeP.getText().toString().trim();
        String email = editTextEmailP.getText().toString().trim();
        String telefone = editTextTelefoneP.getText().toString().trim();
        String senha = editTextSenhaP.getText().toString().trim();

        if (nome.isEmpty() || email.isEmpty() || senha.isEmpty()) {
            Toast.makeText(this, "Nome, email e senha são obrigatórios", Toast.LENGTH_SHORT).show();
            return;
        }

        PassageiroCadastroRequest request = new PassageiroCadastroRequest(nome, email, telefone, senha);

        apiService.cadastrarPassageiro(request).enqueue(new Callback<Object>() {
            @Override
            public void onResponse(Call<Object> call, Response<Object> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(CadastroP.this, "Passageiro cadastrado com sucesso!", Toast.LENGTH_LONG).show();
                    // Navegar para a tela de login do passageiro ou tela principal após o cadastro
                    // Intent intent = new Intent(CadastroP.this, Passageiro.class); // Ou MainActivity, ou tela de login específica
                    // startActivity(intent);
                    // finish(); // Finaliza a activity de cadastro
                } else {
                    // Tentar obter mensagem de erro do corpo da resposta
                    String errorMessage = "Erro ao cadastrar passageiro. Código: " + response.code();
                    if (response.errorBody() != null) {
                        try {
                            // TODO: Idealmente, parsear o JSON de erro do seu backend se ele enviar um
                            errorMessage += " - " + response.errorBody().string();
                        } catch (Exception e) {
                            Log.e("CadastroP_ErrorBody", "Erro ao ler errorBody", e);
                        }
                    }
                    Toast.makeText(CadastroP.this, errorMessage, Toast.LENGTH_LONG).show();
                    Log.e("CadastroP", "Erro: " + response.code() + " " + response.message());
                }
            }

            @Override
            public void onFailure(Call<Object> call, Throwable t) {
                Toast.makeText(CadastroP.this, "Falha na comunicação: " + t.getMessage(), Toast.LENGTH_LONG).show();
                Log.e("CadastroP", "Falha: " + t.getMessage(), t);
            }
        });
    }
}
