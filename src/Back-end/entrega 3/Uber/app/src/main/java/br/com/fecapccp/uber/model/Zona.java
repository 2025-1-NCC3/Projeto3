package br.com.fecapccp.uber.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Modelo para representar uma zona geográfica
 */
public class Zona {
    @SerializedName("_id")
    private String _id;

    @SerializedName("nome")
    private String nome;

    @SerializedName("cidade")
    private String cidade;

    @SerializedName("poligono")
    private Poligono poligono;

    @SerializedName("dataCriacao")
    private String dataCriacao;

    @SerializedName("dataAtualizacao")
    private String dataAtualizacao;

    // Classe interna para representar o polígono GeoJSON
    public static class Poligono {
        @SerializedName("type")
        private String type;

        @SerializedName("coordinates")
        private List<List<List<Double>>> coordinates;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public List<List<List<Double>>> getCoordinates() {
            return coordinates;
        }

        public void setCoordinates(List<List<List<Double>>> coordinates) {
            this.coordinates = coordinates;
        }
    }

    // Getters e Setters
    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCidade() {
        return cidade;
    }

    public void setCidade(String cidade) {
        this.cidade = cidade;
    }

    public Poligono getPoligono() {
        return poligono;
    }

    public void setPoligono(Poligono poligono) {
        this.poligono = poligono;
    }

    public String getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(String dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    public String getDataAtualizacao() {
        return dataAtualizacao;
    }

    public void setDataAtualizacao(String dataAtualizacao) {
        this.dataAtualizacao = dataAtualizacao;
    }
}
