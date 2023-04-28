package br.com.ada.apostaapi.client;

import br.com.ada.apostaapi.client.dto.UsuarioDTO;
import br.com.ada.apostaapi.client.dto.TransacaoDTO;
import br.com.ada.apostaapi.exceptions.ClientErrorException;
import br.com.ada.apostaapi.exceptions.UserNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class UsuarioClient {
    private static final String USUARIOS_API_URL = "http://localhost:8083";

    private final WebClient client;

    public UsuarioClient(WebClient.Builder builder) {
        this.client = builder.baseUrl(USUARIOS_API_URL).build();
    }

    public Mono<UsuarioDTO> buscarUsuarioPorId(String usuarioId) {
        return client
                .get()
                .uri("/usuario/" + usuarioId)
                .exchangeToMono(result -> {
                    if (result.statusCode().is2xxSuccessful()) {
                        return result.bodyToMono(UsuarioDTO.class)
                                .switchIfEmpty(Mono.error(new UserNotFoundException("Usuario inexistente!")));
                    } else {
                        return Mono.error(new ClientErrorException("Erro na chamada"));
                    }
                });
    }

    public Mono<TransacaoDTO> transacao(String usuarioId, TransacaoDTO transacaoDTO){
        return client
                .patch()
                .uri("/usuario/" + usuarioId + "/transacao")
                .bodyValue(transacaoDTO)
                .exchangeToMono(result -> {
                    if (result.statusCode().is2xxSuccessful()) {
                        return result.bodyToMono(TransacaoDTO.class)
                                .switchIfEmpty(Mono.error(new UserNotFoundException("Usuario inexistente!")));
                    } else {
                        return Mono.error(new ClientErrorException("Erro na chamada, verifique os dados inseidos"));
                    }
                });
    }
}

