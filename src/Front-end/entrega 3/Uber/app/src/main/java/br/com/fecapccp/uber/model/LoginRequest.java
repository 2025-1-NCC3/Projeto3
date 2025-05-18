package br.com.fecapccp.uber.model;

public class LoginRequest {
    private String email;
    private String senha;

    public LoginRequest(String email, String senha) {
        this.email = email;
        this.senha = senha;
    }

    // Getters (e Setters, se necessário)
    public String getEmail() {
        return email;
    }

    public String getSenha() {
        return senha;
    }
}
