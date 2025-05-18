package br.com.fecapccp.uber.model;

public class PassageiroCadastroRequest {
    private String nome;
    private String email;
    private String telefone;
    private String senha; // Adicionando senha, pois é comum em cadastros

    public PassageiroCadastroRequest(String nome, String email, String telefone, String senha) {
        this.nome = nome;
        this.email = email;
        this.telefone = telefone;
        this.senha = senha;
    }

    // Getters e Setters podem ser adicionados se necessário, mas para Gson geralmente não são obrigatórios para serialização simples.
    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }
}

