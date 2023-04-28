package br.com.ada.jogosapi.responses;


import br.com.ada.jogosapi.model.Status;

import java.time.LocalDateTime;

public record JogoResponse(String jogoId, String mandante, String visitante, Long golsPorMandante, Long golsPorVisitante, Status status,
                           LocalDateTime dataHoraJogo, String vencedor) {
}
