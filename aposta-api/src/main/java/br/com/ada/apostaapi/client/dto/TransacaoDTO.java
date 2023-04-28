package br.com.ada.apostaapi.client.dto;

import br.com.ada.apostaapi.enums.TipoTransacao;

import java.math.BigDecimal;

public record TransacaoDTO(BigDecimal valorTransacao, TipoTransacao tipoTransacao) {
}
