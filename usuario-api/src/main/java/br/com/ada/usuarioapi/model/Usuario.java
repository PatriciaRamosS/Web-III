package br.com.ada.usuarioapi.model;

import br.com.ada.usuarioapi.responses.UsuarioResponse;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.math.BigDecimal;
import java.util.UUID;

@Builder
@Data
@Document(collection = "usuarios")
public class Usuario {
    @Id
    private String usuarioId;
    @Field("name")
    private String nome;
    @Field("personalDocuments")
    private String documento;

    @Field("email")
    private String email;
    @Field("senha")
    private String senha;
    @Field("balance")
    private BigDecimal saldo;

    public UsuarioResponse toResponse() {
        return new UsuarioResponse(this.usuarioId, this.email, this.nome, this.saldo);
    }


}
