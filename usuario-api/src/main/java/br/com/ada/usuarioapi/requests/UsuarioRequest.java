package br.com.ada.usuarioapi.requests;

public record UsuarioRequest(String nome, String email, String senha, String documento) {
}
