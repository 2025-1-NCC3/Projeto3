package br.com.fecapccp.uber;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Random;

public class Menu extends AppCompatActivity {
    private Button btnvoltar;
    private TextView textViewNome;
    private TextView textViewEmail;
    private TextView textViewTelefone;
    private Button btnAutenticar2FA;
    private EditText editTextCodigo;
    private Button btnVerificarCodigo;

    // Variável para armazenar o código de verificação
    private String codigoVerificacao;
    private String userTelefone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_menu);

        // Inicializar os componentes da UI
        btnvoltar = findViewById(R.id.voltar);
        textViewNome = findViewById(R.id.textView);
        textViewEmail = findViewById(R.id.textView2);
        textViewTelefone = findViewById(R.id.textView5);
        btnAutenticar2FA = findViewById(R.id.btnAutenticar2FA);
        editTextCodigo = findViewById(R.id.editTextCodigo);
        btnVerificarCodigo = findViewById(R.id.btnVerificarCodigo);

        // Recuperar os dados do usuário da Intent
        Intent intent = getIntent();
        if (intent != null) {
            String userName = intent.getStringExtra("USER_NAME");
            String userEmail = intent.getStringExtra("USER_EMAIL");
            userTelefone = intent.getStringExtra("USER_TELEFONE");

            // Exibir os dados do usuário nos TextViews
            if (userName != null && !userName.isEmpty()) {
                textViewNome.setText(userName);
            }

            if (userEmail != null && !userEmail.isEmpty()) {
                textViewEmail.setText("Email: " + userEmail);
            }

            if (userTelefone != null && !userTelefone.isEmpty()) {
                textViewTelefone.setText("Telefone: " + userTelefone);
            }
        }

        // Configurar o botão de autenticação de dois fatores
        btnAutenticar2FA.setOnClickListener(view -> {
            enviarCodigoSMS();
        });

        // Configurar o botão de verificação de código
        btnVerificarCodigo.setOnClickListener(view -> {
            verificarCodigo();
        });

        btnvoltar.setOnClickListener(view -> {
            Intent voltar = new Intent(this, Mapa.class);
            startActivity(voltar);
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    /**
     * Método para gerar e enviar o código de verificação via SMS
     */
    private void enviarCodigoSMS() {
        // Verificar se o telefone está disponível
        if (userTelefone == null || userTelefone.isEmpty()) {
            Toast.makeText(this, "Telefone não disponível para envio do código", Toast.LENGTH_LONG).show();
            return;
        }

        // Gerar um código aleatório de 6 dígitos
        Random random = new Random();
        int codigo = 100000 + random.nextInt(900000); // Gera um número entre 100000 e 999999
        codigoVerificacao = String.valueOf(codigo);

        // Exibir diálogo de progresso
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enviando SMS");
        builder.setMessage("Enviando código de verificação para " + userTelefone);
        builder.setCancelable(false);
        AlertDialog dialog = builder.create();
        dialog.show();

        // Simular o envio do SMS (em uma aplicação real, aqui seria a chamada para uma API de SMS)
        new Handler().postDelayed(() -> {
            dialog.dismiss();

            // Exibir mensagem de sucesso
            Toast.makeText(Menu.this, "Código enviado para " + userTelefone, Toast.LENGTH_LONG).show();

            // Mostrar o campo para digitar o código e o botão de verificação
            editTextCodigo.setVisibility(View.VISIBLE);
            btnVerificarCodigo.setVisibility(View.VISIBLE);

            // Em um ambiente de desenvolvimento/teste, mostrar o código no console
            System.out.println("Código de verificação: " + codigoVerificacao);

            // Opcional: Para facilitar o teste, mostrar o código em um Toast (remover em produção)
            Toast.makeText(Menu.this, "Código: " + codigoVerificacao, Toast.LENGTH_LONG).show();
        }, 2000); // Simula um atraso de 2 segundos para o envio do SMS
    }

    /**
     * Método para verificar o código digitado pelo usuário
     */
    private void verificarCodigo() {
        String codigoDigitado = editTextCodigo.getText().toString().trim();

        if (codigoDigitado.isEmpty()) {
            Toast.makeText(this, "Por favor, digite o código recebido", Toast.LENGTH_SHORT).show();
            return;
        }

        if (codigoDigitado.equals(codigoVerificacao)) {
            // Código correto
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Autenticação Bem-Sucedida");
            builder.setMessage("Sua identidade foi verificada com sucesso!");
            builder.setPositiveButton("OK", (dialog, which) -> {
                // Limpar o campo de código e esconder os elementos de verificação
                editTextCodigo.setText("");
                editTextCodigo.setVisibility(View.GONE);
                btnVerificarCodigo.setVisibility(View.GONE);

                // Atualizar o texto do botão para indicar que a autenticação foi concluída
                btnAutenticar2FA.setText("Autenticado ✓");
                btnAutenticar2FA.setEnabled(false);
            });
            builder.show();
        } else {
            // Código incorreto
            Toast.makeText(this, "Código incorreto. Tente novamente.", Toast.LENGTH_LONG).show();
        }
    }
}
