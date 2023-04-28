package br.com.ada.usuarioapi.domain;

import br.com.ada.usuarioapi.enums.TipoTransacao;

import java.math.BigDecimal;

public record Transacao(BigDecimal valorTransacao, TipoTransacao tipoTransacao) {
}
