package br.com.fecapccp.uber.model;

import com.google.gson.annotations.SerializedName;

/**
 * Modelo para representar uma avaliação de zona
 */
public class AvaliacaoZona {
    @SerializedName("_id")
    private String id;

    @SerializedName("zonaId")
    private String zonaId;

    @SerializedName("usuarioId")
    private String usuarioId;

    @SerializedName("tipoUsuario")
    private String tipoUsuario;

    @SerializedName("classificacaoEstrelas")
    private float classificacaoEstrelas;

    @SerializedName("comentario")
    private String comentario;

    @SerializedName("tipoAlerta")
    private String tipoAlerta;

    @SerializedName("localizacaoAproximada")
    private String localizacaoAproximada;

    @SerializedName("dataAvaliacao")
    private String dataAvaliacao;

    @SerializedName("ativa")
    private boolean ativa;

    // Getters e Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getZonaId() {
        return zonaId;
    }

    public void setZonaId(String zonaId) {
        this.zonaId = zonaId;
    }

    public String getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(String usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getTipoUsuario() {
        return tipoUsuario;
    }

    public void setTipoUsuario(String tipoUsuario) {
        this.tipoUsuario = tipoUsuario;
    }

    public float getClassificacaoEstrelas() {
        return classificacaoEstrelas;
    }

    public void setClassificacaoEstrelas(float classificacaoEstrelas) {
        this.classificacaoEstrelas = classificacaoEstrelas;
    }

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }

    public String getTipoAlerta() {
        return tipoAlerta;
    }

    public void setTipoAlerta(String tipoAlerta) {
        this.tipoAlerta = tipoAlerta;
    }

    public String getLocalizacaoAproximada() {
        return localizacaoAproximada;
    }

    public void setLocalizacaoAproximada(String localizacaoAproximada) {
        this.localizacaoAproximada = localizacaoAproximada;
    }

    public String getDataAvaliacao() {
        return dataAvaliacao;
    }

    public void setDataAvaliacao(String dataAvaliacao) {
        this.dataAvaliacao = dataAvaliacao;
    }

    public boolean isAtiva() {
        return ativa;
    }

    public void setAtiva(boolean ativa) {
        this.ativa = ativa;
    }
}
