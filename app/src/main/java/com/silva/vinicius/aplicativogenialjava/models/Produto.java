package com.silva.vinicius.aplicativogenialjava.models;

import android.support.annotation.Nullable;

public class Produto {

    private Integer id;
    private String nome;
    private String categoria;
    private String usuario_id;

    public Produto() {
    }

    public Produto(Integer id, String nome, String categoria, @Nullable String usuario_id) {
        this.id = id;
        this.nome = nome;
        this.categoria = categoria;
        this.usuario_id = usuario_id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getUsuario_id() {
        return usuario_id;
    }

    public void setUsuario_id(String usuario_id) {
        this.usuario_id = usuario_id;
    }
}
