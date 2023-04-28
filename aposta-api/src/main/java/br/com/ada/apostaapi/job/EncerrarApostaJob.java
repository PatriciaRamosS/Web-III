package br.com.ada.apostaapi.job;

import br.com.ada.apostaapi.client.JogoClient;
import br.com.ada.apostaapi.enums.Status;
import br.com.ada.apostaapi.services.ApostaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class EncerrarApostaJob implements InitializingBean {
    private final ApostaService service;
    private final JogoClient jogoClient;

    @Override
    public void afterPropertiesSet() throws Exception {
        var executors = Executors.newSingleThreadScheduledExecutor();
        executors.scheduleWithFixedDelay(() -> {
            Flux.defer(() -> service.getAllByStatusJob("EM_ANDAMENTO"))
                    .subscribeOn(Schedulers.boundedElastic())
                    .flatMap(aposta -> jogoClient.buscarJogoPorId(aposta.getJogoId())
                        .filter(jogo -> jogo.status().equals(Status.ENCERRADO))
                        .flatMap(jg -> service.conclude(aposta, jg)))
                    .doOnNext(ApostaId -> log.info("Aposta validada - {}", ApostaId))
                    .doOnComplete(() -> log.info("Todos as apostas EM_ANDAMENTO de jogos ENCERRADOS finalizadas com sucesso!"))
                    .subscribe();
        }, 1, 45 , TimeUnit.SECONDS);
    }
}
