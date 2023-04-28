package br.com.ada.jogosapi.controllers;

import br.com.ada.jogosapi.requests.JogoRequest;
import br.com.ada.jogosapi.responses.JogoResponse;
import br.com.ada.jogosapi.services.JogoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@RestController
@RequiredArgsConstructor
@RequestMapping("/jogos")
@Slf4j
public class JogoController {
    private final JogoService service;

    @PostMapping
    public Mono<ResponseEntity<Mono<JogoResponse>>> novaPartida(@RequestBody JogoRequest jogoRequest){
        return Mono.defer(() ->
            service.save(jogoRequest).subscribeOn(Schedulers.parallel())
                    .map(jogoResponse -> ResponseEntity.ok(Mono.just(jogoResponse)))
                    .doOnError(err -> log.error("Error ao salvar jogo - {}", err.getMessage()))
                    .doOnNext(it -> log.info("Jogo salvo com sucesso - {}", it)));

    }
    @PutMapping("/{id}")
    public Mono<ResponseEntity<Mono<JogoResponse>>> alterarPartida(@RequestBody JogoRequest jogoRequest, @PathVariable("id") String jogoId){
        return Mono.defer(() ->
                service.update(jogoRequest, jogoId).subscribeOn(Schedulers.parallel())
                        .map(jogoResponse -> ResponseEntity.ok(Mono.just(jogoResponse)))
                        .doOnError(err -> log.error("Error ao alterar jogo - {}", err.getMessage()))
                        .doOnNext(it -> log.info("Jogo alterado com sucesso - {}", it)));

    }
    @GetMapping
    public Mono<ResponseEntity<Flux<JogoResponse>>> getTodosOsJogos() {
        return Mono.defer(() ->
                service.getAll()
                        .subscribeOn(Schedulers.parallel())
                        .collectList()
                        .map(jogoResponses -> ResponseEntity.ok().body(Flux.fromIterable(jogoResponses)))
                        .doOnError(err -> log.error("Erro ao buscar todos os jogos - {}", err.getMessage()))
                        .doOnNext(it -> log.info("Jogos buscados com sucesso"))
        );
    }
    @GetMapping(params = "status")
    public Mono<ResponseEntity<Flux<JogoResponse>>> getTodosOsJogosEmAndamento(@RequestParam("status") String status){
        return Mono.defer(() ->
                        service.getAllPerStatus(status)
                .subscribeOn(Schedulers.parallel())
                .collectList()
                .map(jogoResponses -> ResponseEntity.ok().body(Flux.fromIterable(jogoResponses)))
                .doOnError(err -> log.error("Erro ao buscar jogos pelo status- {}", err.getMessage()))
                .doOnNext(it -> log.info("Jogos buscados pelo status com sucesso")));
    }
    @GetMapping("/{id}")
    public Mono<ResponseEntity<Mono<JogoResponse>>> getPartida(@PathVariable("id") String jogoId){
        return Mono.defer(() -> service.get(jogoId).subscribeOn(Schedulers.parallel())
                .map(jogoResponse -> ResponseEntity.ok(Mono.just(jogoResponse)))
                .doOnError(err -> log.error("Error ao buscar jogo - {}", err.getMessage()))
                .doOnNext(it -> log.info("Jogo recuperado com sucesso - {}", it)));
    }
    @PatchMapping(value = "/{id}", params = "time")
    public Mono<ResponseEntity<Mono<JogoResponse>>> marcarGol(@PathVariable("id") String jogoId, @RequestParam("time")String time){
        return Mono.defer(() -> service.scoreGoal(jogoId, time))
                .map(jogoResponse -> ResponseEntity.ok(Mono.just(jogoResponse)))
                .doOnError(err -> log.error("Error ao alterar placar - {}", err.getMessage()))
                .doOnNext(it -> log.info("Placar alterado - {}", it));
    }
    @PatchMapping(value = "/{id}", params = "status")
    public Mono<ResponseEntity<Mono<JogoResponse>>> alterarStatus(@PathVariable("id") String jogoId, @RequestParam("status")String status){
        return Mono.defer(() -> service.updateStatus(jogoId, status))
                .map(jogoResponse -> ResponseEntity.ok(Mono.just(jogoResponse)))
                .doOnError(err -> log.error("Error ao atualizar status - {}", err.getMessage()))
                .doOnNext(it -> log.info("Status alterado - {}", it));
    }
}
