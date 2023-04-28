package br.com.ada.apostaapi.model;
import br.com.ada.apostaapi.enums.Premiacao;
import br.com.ada.apostaapi.enums.Status;
import br.com.ada.apostaapi.responses.ApostaResponse;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import java.math.BigDecimal;
import java.time.Instant;


@Builder
@Data
@Document("apostas")
public class Aposta {
    @Id
    private String apostaId;
    @Field("usuarioId")
    private String usuarioId;
    @Field("jogoId")
    private String jogoId;
    @Field("coeficiente")
    private Double coeficiente;
    @Field("valorApostado")
    private BigDecimal valorApostado;
    @Field("valorPremiacao")
    private BigDecimal valorPremiacao;
    @Field("timeApostado")
    private String timeApostado;
    @Field("status")
    private Status status;
    @Field("criacao")
    private Instant criacao;
    @Field("modificacao")
    private Instant modificacao;
    @Field("premiacao")
    private Premiacao premiacao;

    public ApostaResponse toResponse(){
        return  new ApostaResponse(this.apostaId, this.usuarioId, this.jogoId, this.coeficiente,
                this.valorApostado, this.valorPremiacao, this.timeApostado,
                this.status, this.premiacao);
    }

}
