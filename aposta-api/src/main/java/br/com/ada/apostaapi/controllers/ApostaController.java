package br.com.ada.apostaapi.controllers;
import br.com.ada.apostaapi.requests.ApostaRequest;
import br.com.ada.apostaapi.responses.ApostaResponse;
import br.com.ada.apostaapi.services.ApostaService;
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
@RequestMapping("/apostas")
public class ApostaController {
    private final ApostaService service;


    @PostMapping
    public Mono<ResponseEntity<Mono<ApostaResponse>>> novaAposta(@RequestBody ApostaRequest apostaRequest){
        return Mono.defer(() ->
                service.save(apostaRequest).subscribeOn(Schedulers.parallel())
                        .map(apostaResponse -> ResponseEntity.ok(Mono.just(apostaResponse)))
                        .doOnError(err -> log.error("Error ao salvar aposta - {}", err.getMessage()))
                        .doOnNext(it -> log.info("Aposta salva com sucesso - {}", it)));
    }
    @GetMapping("/{id}")
    public Mono<ResponseEntity<Mono<ApostaResponse>>> getAposta(@PathVariable("id") String apostaId){
        return Mono.defer(() -> service.get(apostaId).subscribeOn(Schedulers.parallel())
                .map(apostaResponse -> ResponseEntity.ok(Mono.just(apostaResponse)))
                .doOnError(err -> log.error("Error ao buscar aposta - {}", err.getMessage()))
                .doOnNext(it -> log.info("Aposta recuperado com sucesso - {}", it)));
    }
    @GetMapping
    public Mono<ResponseEntity<Flux<ApostaResponse>>> getTodasAposta(){
        return Flux.defer(service::getAll).subscribeOn(Schedulers.parallel()).collectList()
                .map(apostasResponse -> ResponseEntity.ok().body(Flux.fromIterable(apostasResponse)))
                .doOnError(err -> log.error("Error ao buscar apostas - {}", err.getMessage()))
                .doOnNext(it -> log.info("Apostas recuperado com sucesso - {}", it));
    }
    @GetMapping(params = "status")
    public Mono<ResponseEntity<Flux<ApostaResponse>>> getTodasApostaPorStatus(@RequestParam("status")String status){
        return Flux.defer(() -> service.getAllByStatus(status)).subscribeOn(Schedulers.parallel()).collectList()
                .map(apostasResponse -> ResponseEntity.ok().body(Flux.fromIterable(apostasResponse)))
                .doOnError(err -> log.error("Error ao buscar apostas - {}", err.getMessage()))
                .doOnNext(it -> log.info("Apostas recuperado com sucesso - {}", it));
    }
}
