package br.com.ada.apostaapi.client.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record UsuarioDTO(String usuarioId, BigDecimal saldo) {
}
