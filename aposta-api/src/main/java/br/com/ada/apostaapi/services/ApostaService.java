package br.com.ada.apostaapi.services;

import br.com.ada.apostaapi.client.JogoClient;
import br.com.ada.apostaapi.client.UsuarioClient;
import br.com.ada.apostaapi.client.dto.ApostaAuxDTO;
import br.com.ada.apostaapi.client.dto.JogoDTO;
import br.com.ada.apostaapi.client.dto.TransacaoDTO;
import br.com.ada.apostaapi.enums.Premiacao;
import br.com.ada.apostaapi.enums.Status;
import br.com.ada.apostaapi.enums.TipoTransacao;
import br.com.ada.apostaapi.exceptions.BetNotFoundException;
import br.com.ada.apostaapi.exceptions.FinishedGameException;
import br.com.ada.apostaapi.exceptions.InvalidTeamException;
import br.com.ada.apostaapi.exceptions.UnauthorizedBalanceTransactionException;
import br.com.ada.apostaapi.model.*;
import br.com.ada.apostaapi.requests.ApostaRequest;
import br.com.ada.apostaapi.repositories.ApostaRepository;
import br.com.ada.apostaapi.responses.ApostaResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.math.BigDecimal;
import java.time.Instant;

@RequiredArgsConstructor
@Service
@Slf4j
public class ApostaService {
    private final ApostaRepository repository;
    private final JogoClient jogoClient;
    private final UsuarioClient usuarioClient;


    private Mono<ApostaAuxDTO> calcularCoeficiente(String id, String time) {
        return Mono.defer(() -> jogoClient.buscarJogoPorId(id)
                .map(jogoDTO -> {
                    if (!jogoDTO.status().equals(Status.ENCERRADO)) {
                        double coeficiente;
                        switch (jogoDTO.status()) {
                            case NAO_INICIADO -> coeficiente = (jogoDTO.mandante().equalsIgnoreCase(time)) ? 1.25 : 1.75;

                            case EM_ANDAMENTO -> {
                                if (jogoDTO.mandante().equalsIgnoreCase(time)) {
                                    if (jogoDTO.saldoGols() > 0) coeficiente = 1.5 - (jogoDTO.saldoGols() * 0.1);
                                    else if (jogoDTO.saldoGols() < 0) coeficiente = jogoDTO.saldoGols() * -1.0;
                                    else coeficiente = 1.5;
                                } else {
                                    if (jogoDTO.saldoGols() > 0) coeficiente = jogoDTO.saldoGols();
                                    else if (jogoDTO.saldoGols() < 0) coeficiente = 1.5 + (jogoDTO.saldoGols() * 0.1);
                                    else coeficiente = 2.0;
                                    ;
                                }
                            }
                            default -> coeficiente = 1.5;
                        }
                        return new ApostaAuxDTO(coeficiente, jogoDTO.mandante(), jogoDTO.visitante());
                    } else throw new FinishedGameException("Jogo ja encerado, impossivel fazer aposta");
                }));
    }

    public Mono<ApostaResponse> save(ApostaRequest apostaRequest) {
        return Mono.defer(() -> calcularCoeficiente(apostaRequest.jogoId(), apostaRequest.timeApostado())
                        .flatMap(apostaAuxDTO -> usuarioClient.buscarUsuarioPorId(apostaRequest.userId())
                                .flatMap(usuarioDTO -> {
                                    if (usuarioDTO.saldo().doubleValue() >= apostaRequest.valorAposta().doubleValue()) {
                                        if (apostaRequest.timeApostado().equalsIgnoreCase(apostaAuxDTO.mandante()) || apostaRequest.timeApostado().equalsIgnoreCase(apostaAuxDTO.visitante())) {
                                            var aposta = Aposta.builder()
                                                    .usuarioId(apostaRequest.userId())
                                                    .jogoId(apostaRequest.jogoId())
                                                    .valorApostado(apostaRequest.valorAposta())
                                                    .valorPremiacao(apostaRequest.valorAposta().multiply(BigDecimal.valueOf(apostaAuxDTO.coeficiente())))
                                                    .coeficiente(apostaAuxDTO.coeficiente())
                                                    .timeApostado(apostaRequest.timeApostado())
                                                    .status(Status.NAO_INICIADO)
                                                    .criacao(Instant.now())
                                                    .premiacao(Premiacao.INDISPONIVEL)
                                                    .build();
                                            log.info("Salvando aposta - {}", aposta);
                                            return Mono.defer(() -> repository.save(aposta).map(Aposta::toResponse))
                                                    .flatMap(apostaSalva -> usuarioClient.transacao(String.valueOf(usuarioDTO.usuarioId()), new TransacaoDTO(aposta.getValorApostado(), TipoTransacao.SAQUE))
                                                            .thenReturn(apostaSalva));
                                        } else return Mono.error(new InvalidTeamException("Time invalido para aposta"));

                                    } else
                                        return Mono.error(new UnauthorizedBalanceTransactionException("Saldo insuficiente para aposta"));

                                })))
                .subscribeOn(Schedulers.boundedElastic());
    }


    public Mono<ApostaResponse> get(String apostaId) {
        return Mono.defer(() -> {
                    log.info("Buscando jogo - {}", apostaId);
                    return repository.findById(apostaId).map(Aposta::toResponse);
                }).switchIfEmpty(Mono.error(new BetNotFoundException("Aposta nao encontrada com o id informado")))
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Flux<ApostaResponse> getAll() {
        return Flux.defer(() -> {
                    log.info("Buscando todos as apostas");
                    return repository.findAll().map(Aposta::toResponse);
                }).switchIfEmpty(Flux.error(new BetNotFoundException("Nenhuma aposta encontrada")))
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Flux<Aposta> getAllByStatusJob(String status) {
        return Flux.defer(() -> {
                    log.info("Buscando todos as Apostas - {}", status);
                    return repository.findAll().filter(aposta -> aposta.getStatus().toString().equalsIgnoreCase(status));
                })
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Flux<ApostaResponse> getAllByStatus(String status) {
        return Flux.defer(() -> {
                    log.info("Buscando todos as Apostas - {}", status);
                    return repository.findAll().filter(aposta -> aposta.getStatus().toString().equalsIgnoreCase(status))
                            .map(Aposta::toResponse);
                })
                .switchIfEmpty(Flux.error(new BetNotFoundException("Nenhuma aposta encontrada com o status informado")))
                .subscribeOn(Schedulers.boundedElastic());
    }


    public Mono<Aposta> authorize(Aposta aposta) {

        return Mono.defer(() -> {
            log.info("Iniciando autorizacao de aposta");
            aposta.setStatus(Status.EM_ANDAMENTO);
            aposta.setModificacao(Instant.now());
            return repository.save(aposta);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<Aposta> conclude(Aposta aposta, JogoDTO jogoDTO) {
        return Mono.defer(() -> {
            log.info("Iniciando encerramento de aposta");
            aposta.setStatus(Status.ENCERRADO);
            aposta.setModificacao(Instant.now());
            if (aposta.getTimeApostado().equalsIgnoreCase(jogoDTO.vencedor()) && aposta.getPremiacao().equals(Premiacao.INDISPONIVEL)) {
                aposta.setPremiacao(Premiacao.DISPONIVEL);
            }
            return repository.save(aposta);
        }).subscribeOn(Schedulers.boundedElastic());
    }


    public Flux<Aposta> getAllByAvaliablePrize(String premiacao) {
        return Flux.defer(() -> {
            log.info("Buscando todos as Apostas com premiacao - {}", premiacao);
            return repository.findAll()
                    .filter(aposta -> aposta.getPremiacao().toString().equalsIgnoreCase(premiacao));
        }).subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<Aposta> setPrizeRegatada(Aposta aposta){
        return Mono.defer(() -> {
            aposta.setPremiacao(Premiacao.RESGATADA);
            return repository.save(aposta);
        }).subscribeOn(Schedulers.boundedElastic());

    }
}


