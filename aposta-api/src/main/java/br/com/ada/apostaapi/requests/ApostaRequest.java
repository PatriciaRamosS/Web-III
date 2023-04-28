package br.com.ada.apostaapi.requests;

import br.com.ada.apostaapi.enums.Time;

import java.math.BigDecimal;

public record ApostaRequest(String userId, String jogoId, BigDecimal valorAposta, String timeApostado) {
}
