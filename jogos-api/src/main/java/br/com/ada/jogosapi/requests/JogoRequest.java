package br.com.ada.jogosapi.requests;

import java.time.LocalDateTime;

public record JogoRequest(String mandante, String visitante, String dataHoraJogo) {
}
