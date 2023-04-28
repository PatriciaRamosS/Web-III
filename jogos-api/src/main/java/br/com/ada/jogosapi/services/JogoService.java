package br.com.ada.jogosapi.services;
import br.com.ada.jogosapi.exceptions.GameNotFoundException;
import br.com.ada.jogosapi.exceptions.UnavaliableGameException;
import br.com.ada.jogosapi.model.Jogo;
import br.com.ada.jogosapi.model.Status;
import br.com.ada.jogosapi.repositories.JogoRepository;
import br.com.ada.jogosapi.requests.JogoRequest;
import br.com.ada.jogosapi.responses.JogoResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


@Service
@RequiredArgsConstructor
@Slf4j
public class JogoService {
    private final JogoRepository repository;

    public Mono<JogoResponse> save(JogoRequest jogoRequest){
        return Mono.defer(() ->{
            var jogo = Jogo.builder()
                    .mandante(jogoRequest.mandante())
                    .visitante(jogoRequest.visitante())
                    .golsPorMandante(0L)
                    .golsPorVisitante(0L)
                    .saldoGols(0L)
                    .dataHoraJogo(LocalDateTime.parse(jogoRequest.dataHoraJogo(), DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")))
                    .status(Status.NAO_INICIADO)
                    .build();
            log.info("Salvando jogo -{}", jogo);
            return repository.save(jogo).map(Jogo::toResponse);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<JogoResponse> get(String jogoId){
        return Mono.defer( () -> {
            log.info("Buscando jogo pelo id - {}", jogoId);
            return repository.findById(jogoId).map(Jogo::toResponse)
                    .switchIfEmpty(Mono.error(new GameNotFoundException("Partida nao encontrada pelo id informado")));
        }).subscribeOn(Schedulers.boundedElastic());
    }
    public Flux<JogoResponse> getAll(){
        return Flux.defer( () -> {
            log.info("Buscando todos os jogos");
            return repository.findAll().map(Jogo::toResponse)
                    .switchIfEmpty(Mono.error(new GameNotFoundException("Nenhuma partida encontrada")));
        }).subscribeOn(Schedulers.boundedElastic());
    }
    public Flux<JogoResponse> getAllPerStatus(String status){
        return Flux.defer( () -> {
            log.info("Buscando todos os jogos");
            return repository.findAll().filter(jogo -> jogo.getStatus().toString().equalsIgnoreCase(status)).map(Jogo::toResponse)
                    .switchIfEmpty(Mono.error(new GameNotFoundException("Nenhuma partida encontrada pelo status informado")));
        }).subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<JogoResponse> scoreGoal(String jogoId, String time){
        return Mono.defer(() -> {
            log.info("Iniciando alteracao de placar");
            return repository.existsByJogoId(jogoId).flatMap(exists -> {
                if (exists){
                    return repository.findById(jogoId).flatMap(jogo -> {
                        if (jogo.getStatus().equals(Status.EM_ANDAMENTO)){
                            if(time.equalsIgnoreCase("mandante")){
                                jogo.setGolsPorMandante(jogo.getGolsPorMandante()+1);
                                jogo.setSaldoGols(jogo.getSaldoGols()+1);
                            } else if (time.equalsIgnoreCase("visitante")) {
                                jogo.setGolsPorVisitante(jogo.getGolsPorVisitante()+1);
                                jogo.setSaldoGols(jogo.getSaldoGols()-1);
                            }
                            return repository.save(jogo).map(Jogo::toResponse);
                        }else{
                            return Mono.error(new UnavaliableGameException("Jogo encerrado ou nao iniciado"));
                        }
                    });
                } else {
                    return Mono.error(new GameNotFoundException("Jogo não encontrado"));
                }
            });
        }).subscribeOn(Schedulers.boundedElastic());
    }
    public Mono<JogoResponse> updateStatus(String jogoId, String status){
        return Mono.defer(() -> {
            log.info("Iniciando atualizacao de status");
            return repository.existsByJogoId(jogoId).flatMap(exists -> {
                if (exists){
                    return repository.findById(jogoId).flatMap(jogo -> {
                        if(status.equalsIgnoreCase(Status.EM_ANDAMENTO.toString()) && jogo.getStatus().equals(Status.NAO_INICIADO)){
                            jogo.setStatus(Status.valueOf(status.toUpperCase()));
                            jogo.setInicioPartida(Instant.now());
                        } else if(status.equalsIgnoreCase(Status.ENCERRADO.toString()) && jogo.getStatus().equals(Status.EM_ANDAMENTO)){
                            jogo.setStatus(Status.valueOf(status.toUpperCase()));
                            jogo.setFinalPartida(Instant.now());
                            if (jogo.getSaldoGols() > 0){
                                jogo.setVencedor(jogo.getMandante());
                            } else if (jogo.getSaldoGols() < 0){
                                jogo.setVencedor(jogo.getVisitante());
                            } else{
                                jogo.setVencedor("Empate");
                            }
                        } else{
                            return Mono.error(new UnavaliableGameException("Jogo ja encerrado"));
                        }
                        return repository.save(jogo).map(Jogo::toResponse);
                    });
                } else {
                    return Mono.error(new GameNotFoundException("Jogo não encontrado"));
                }
            });
        }).subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<JogoResponse> update(JogoRequest jogoRequest, String jogoId) {
        return Mono.defer(() ->{
            log.info("Atualizando jogo -{}", jogoRequest);
            return repository.existsByJogoId(jogoId).flatMap(exists ->
                    {
                        if (exists){
            return repository.findById(jogoId).flatMap(
                    jogo -> {
                        jogo.setMandante(jogoRequest.mandante());
                        jogo.setVisitante(jogoRequest.visitante());
                        jogo.setDataHoraJogo(LocalDateTime.parse(jogoRequest.dataHoraJogo(), DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
                        return repository.save(jogo).map(Jogo::toResponse);
                    });
                        }  else return Mono.error(new GameNotFoundException("Jogo nao encontrado pelo id informado"));
                    });
        }).subscribeOn(Schedulers.boundedElastic());
    }
}
