package br.com.ada.usuarioapi.responses;

import java.math.BigDecimal;

public record UsuarioResponse(String usuarioId, String email, String nome, BigDecimal saldo) {
}
