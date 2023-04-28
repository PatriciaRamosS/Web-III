package br.com.ada.usuarioapi.controllers;

import br.com.ada.usuarioapi.domain.Transacao;
import br.com.ada.usuarioapi.requests.UsuarioRequest;
import br.com.ada.usuarioapi.responses.UsuarioResponse;
import br.com.ada.usuarioapi.services.UsuarioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/usuario")
public class UsuarioController {
    private final UsuarioService service;

    @PostMapping
    public Mono<ResponseEntity<Mono<UsuarioResponse>>> novoUsuario(@RequestBody UsuarioRequest usuarioRequest){
        return Mono.defer(() -> service.save(usuarioRequest).subscribeOn(Schedulers.parallel())
                .map(usuarioResponse -> ResponseEntity.ok(Mono.just(usuarioResponse)))
                .doOnError(err -> log.error("Error ao salvar usuario - {}", err.getMessage()))
                .doOnNext(it -> log.info("usuario salvo com sucesso - {}", it)));
    }
    @GetMapping("/{id}")
    public Mono<ResponseEntity<Mono<UsuarioResponse>>> buscarUsuarioPorId(@PathVariable("id") String usuarioId){
        return Mono.defer(() -> service.get(usuarioId).subscribeOn(Schedulers.parallel())
                .map(usuarioResponse -> ResponseEntity.ok(Mono.just(usuarioResponse)))
                .doOnError(err -> log.error("Error ao buscar usuario - {}", err.getMessage()))
                .doOnNext(it -> log.info("Usuario encontrado com sucesso - {}", it)));
    }
    @GetMapping
    public Mono<ResponseEntity<Flux<UsuarioResponse>>> todosUsuarios(){
        return Mono.defer(() ->
                        service.getAll().subscribeOn(Schedulers.parallel()).collectList()
                                .map(usuarioResponse -> ResponseEntity.ok().body(Flux.fromIterable(usuarioResponse)))
                .doOnError(err -> log.error("Error ao buscar todos os usuarios - {}", err.getMessage()))
                .doOnNext(it -> log.info("Todos os ssuarios recuperado com sucesso - {}", it)));
    }

    @PatchMapping("/{id}/transacao")
    public Mono<ResponseEntity<Mono<UsuarioResponse>>> sacarSaldo(@PathVariable("id")String usuarioId, @RequestBody Transacao transacao){
        return Mono.defer(() -> service.valueTransaction(usuarioId, transacao)).subscribeOn(Schedulers.parallel())
                .map(usuarioResponse -> ResponseEntity.ok(Mono.just(usuarioResponse)))
                .doOnError(err -> log.error("Error na transção - {}", err.getMessage()))
                .doOnNext(it -> log.info("Transação realizada com sucesso - {}", it));
    }
}
