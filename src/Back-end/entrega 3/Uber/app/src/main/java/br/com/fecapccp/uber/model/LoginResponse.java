package br.com.fecapccp.uber.model;

// Este é um exemplo. Adapte conforme a resposta real do seu backend.
public class LoginResponse {
    private String msg;
    private PassageiroInfo passageiro; // Você pode criar uma classe PassageiroInfo
    // private String token; // Se o backend retornar um token JWT

    // Getters e Setters
    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public PassageiroInfo getPassageiro() {
        return passageiro;
    }

    public void setPassageiro(PassageiroInfo passageiro) {
        this.passageiro = passageiro;
    }

    // public String getToken() { return token; }
    // public void setToken(String token) { this.token = token; }

    public static class PassageiroInfo {
        private String id;
        private String nome;
        private String email;

        // Getters e Setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getNome() { return nome; }
        public void setNome(String nome) { this.nome = nome; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }
}
