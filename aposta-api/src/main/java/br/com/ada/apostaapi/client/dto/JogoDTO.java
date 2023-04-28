package br.com.ada.apostaapi.client.dto;

import br.com.ada.apostaapi.enums.Status;

import java.util.UUID;

public record JogoDTO(String jogoId, String mandante, String visitante, Status status, Long saldoGols, String vencedor) {
}
