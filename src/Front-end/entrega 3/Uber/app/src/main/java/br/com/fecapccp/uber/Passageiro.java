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

// Imports adicionados para a funcionalidade de login com API
import br.com.fecapccp.uber.model.LoginRequest;
import br.com.fecapccp.uber.model.LoginResponse;
import br.com.fecapccp.uber.remote.ApiService;
import br.com.fecapccp.uber.remote.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Passageiro extends AppCompatActivity {

    // Variáveis de UI
    private Button btnvoltar;
    private Button btnentrar;
    private Button cadastreP;
    private EditText etLogin; // Este campo será usado para o EMAIL
    private EditText etSenha;

    // Variável para o serviço da API
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main2); // Certifique-se que este é o layout correto para a tela de login do passageiro

        // Inicializar o ApiService
        apiService = RetrofitClient.getApiService();

        // Referenciar os componentes da UI
        btnvoltar = findViewById(R.id.voltar);
        btnentrar = findViewById(R.id.btnentrar);
        cadastreP = findViewById(R.id.cadastreM); // Verifique se o ID está correto (cadastreP ou cadastreM)
        etLogin = findViewById(R.id.etLogin); // Este EditText deve ser para o EMAIL
        etSenha = findViewById(R.id.etSenha);

        // Listener para o botão ENTRAR
        btnentrar.setOnClickListener(view -> {
            String email = etLogin.getText().toString().trim();
            String senha = etSenha.getText().toString().trim();

            if (email.isEmpty() || senha.isEmpty()) {
                Toast.makeText(Passageiro.this, "Email e senha são obrigatórios", Toast.LENGTH_SHORT).show();
                return;
            }
            // Chama o método para realizar o login via API
            realizarLogin(email, senha);
        });

        // Listener para o botão VOLTAR
        btnvoltar.setOnClickListener(view -> {
            Intent voltarIntent = new Intent(Passageiro.this, MainActivity.class);
            startActivity(voltarIntent);
            finish(); // Opcional: finalizar esta activity ao voltar
        });

        // Listener para o botão CADASTRE-SE
        cadastreP.setOnClickListener(view -> {
            Intent cadastroIntent = new Intent(Passageiro.this, CadastroP.class);
            startActivity(cadastroIntent);
        });

        // Listener para ajustes de interface com o sistema (geralmente gerado automaticamente)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    // Novo método para realizar o login chamando a API
    private void realizarLogin(String email, String senha) {
        LoginRequest loginRequest = new LoginRequest(email, senha);

        apiService.loginPassageiro(loginRequest).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginResponse = response.body();
                    // Sucesso no login
                    Toast.makeText(Passageiro.this, "Login bem-sucedido: " + loginResponse.getPassageiro().getNome(), Toast.LENGTH_LONG).show();

                    // Navegar para a próxima tela (Mapa.class)
                    Intent entrarIntent = new Intent(Passageiro.this, Mapa.class);
                    // Passando dados do usuário para a próxima Activity
                    entrarIntent.putExtra("USER_NAME", loginResponse.getPassageiro().getNome());
                    entrarIntent.putExtra("USER_EMAIL", loginResponse.getPassageiro().getEmail());
                    // Se o telefone estiver disponível na resposta, também o passaríamos aqui
                    // Como não está explicitamente na LoginResponse, usamos o email de login como telefone para demonstração
                    entrarIntent.putExtra("USER_TELEFONE", etLogin.getText().toString().trim());

                    startActivity(entrarIntent);
                    finish(); // Finaliza a activity de login para que o usuário não volte para ela pressionando "voltar"
                } else {
                    // Falha no login (credenciais inválidas, erro no servidor, etc.)
                    String errorMessage = "Falha no login.";
                    if (response.errorBody() != null) {
                        try {
                            // Tenta ler a mensagem de erro específica do backend
                            errorMessage += " Código: " + response.code() + " - " + response.errorBody().string();
                        } catch (Exception e) {
                            Log.e("LoginPassageiro_ErrorBody", "Erro ao ler errorBody", e);
                            errorMessage += " Código: " + response.code();
                        }
                    } else if (response.message() != null && !response.message().isEmpty()){
                        errorMessage += " Código: " + response.code() + " - " + response.message();
                    } else {
                        errorMessage += " Código: " + response.code();
                    }
                    Toast.makeText(Passageiro.this, errorMessage, Toast.LENGTH_LONG).show();
                    Log.e("LoginPassageiro", "Erro no login: " + errorMessage);
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                // Falha na comunicação com a API (sem internet, servidor offline, etc.)
                Toast.makeText(Passageiro.this, "Falha na comunicação: " + t.getMessage(), Toast.LENGTH_LONG).show();
                Log.e("LoginPassageiro_Failure", "Falha na chamada da API: " + t.getMessage(), t);
            }
        });
    }
}
