package br.com.fecapccp.uber;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Passageiro extends AppCompatActivity {

    private Button btnvoltar;
    private Button btnentrar;

    private EditText etLogin;
    private EditText etSenha;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main2);


        btnentrar = findViewById(R.id.btnentrar);
        etLogin = findViewById(R.id.etLogin);
        etSenha = findViewById(R.id.etSenha);

        btnentrar.setOnClickListener(view -> {
            String login = etLogin.getText().toString();
            String senha = etSenha.getText().toString();

            if (login.equals("0000") && senha.equals("0000")) {
                Intent entrar = new Intent(this, Mapa.class);
                startActivity(entrar);
            } else {
                Toast.makeText(this, "Login ou senha errado", Toast.LENGTH_SHORT).show();
            }
        });
        btnvoltar = findViewById(R.id.voltar);

        btnvoltar.setOnClickListener(view ->{
            Intent voltar = new Intent(this, MainActivity.class);
            startActivity(voltar);
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}