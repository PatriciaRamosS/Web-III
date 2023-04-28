package br.com.ada.usuarioapi.services;

import br.com.ada.usuarioapi.enums.TipoTransacao;
import br.com.ada.usuarioapi.domain.Transacao;
import br.com.ada.usuarioapi.exceptions.DuplicatedUserException;
import br.com.ada.usuarioapi.exceptions.UnauthorizedBalanceTransactionException;
import br.com.ada.usuarioapi.exceptions.UserNotFoundException;
import br.com.ada.usuarioapi.model.Usuario;
import br.com.ada.usuarioapi.requests.UsuarioRequest;
import br.com.ada.usuarioapi.repositories.UsarioReactiveRepository;
import br.com.ada.usuarioapi.responses.UsuarioResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class UsuarioService {
    private final UsarioReactiveRepository repository;



    public Mono<UsuarioResponse> save(UsuarioRequest usuarioRequest) {
        return Mono.defer(() -> {
            var documento = usuarioRequest.documento();
            return repository.existsByDocumento(documento)
                    .flatMap(exists -> {
                        if (exists) {
                            return Mono.error(new DuplicatedUserException("Usuário já registrado"));
                        } else {
                            var usuario = Usuario.builder()
                                    .documento(documento)
                                    .email(usuarioRequest.email())
                                    .nome(usuarioRequest.nome())
                                    .senha(usuarioRequest.senha())
                                    .saldo(BigDecimal.TEN)
                                    .build();
                            log.info("Salvando usuário - {}", usuario);
                            return repository.save(usuario).map(Usuario::toResponse);
                        }
                    });
        }).subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<UsuarioResponse> get(String usuarioId){
        return Mono.defer(() -> {
            log.info("Buscando usuario pelo id - {}", usuarioId);
            return repository.findById(usuarioId).map(Usuario::toResponse)
            .switchIfEmpty(Mono.error(new UserNotFoundException("Usuario nao encontrado pelo id informado")));
        }).subscribeOn(Schedulers.boundedElastic());
    }

    public Flux<UsuarioResponse> getAll(){
        return Flux.defer(() -> {
            log.info("Buscando todos os usuarios");
            return repository.findAll().map(Usuario::toResponse)
                    .switchIfEmpty(Mono.error(new UserNotFoundException("Nenhum usuario encontrado")));
        }).subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<UsuarioResponse> valueTransaction(String usuarioId, Transacao transacao){
        return Mono.defer( () -> {
            log.info("Buscando usuario pelo id - {}", usuarioId);
            return repository.findById(usuarioId)
                    .flatMap(user ->{
                if (transacao.tipoTransacao().equals(TipoTransacao.SAQUE)) {
                    if (user.getSaldo().doubleValue() >= transacao.valorTransacao().doubleValue()) {
                        user.setSaldo(user.getSaldo().subtract(transacao.valorTransacao()));
                        log.info("Sacando valor de usuario - {}", usuarioId);
                    } else {
                        return Mono.error(new UnauthorizedBalanceTransactionException("Saldo insuficiente"));
                    }
                } else if (transacao.tipoTransacao().equals(TipoTransacao.DEPOSITO)) {
                    user.setSaldo(user.getSaldo().add(transacao.valorTransacao()));
                    log.info("Depositando valor de usuario - {}", usuarioId);
                }else {
                    return Mono.error(new UnauthorizedBalanceTransactionException("Tipo de transacao invalida."));
                }
                        return repository.save(user).map(Usuario::toResponse);
                    });
        }).subscribeOn(Schedulers.boundedElastic());
    }

}
