package br.com.ada.apostaapi.responses;
import br.com.ada.apostaapi.enums.Premiacao;
import br.com.ada.apostaapi.enums.Status;
import java.math.BigDecimal;

public record ApostaResponse(String apostaId, String usuarioId, String jogoId, Double coeficiente,
                             BigDecimal valorApostado, BigDecimal valorPremiacao, String timeApostado,
                             Status status, Premiacao premiacao) {
}
