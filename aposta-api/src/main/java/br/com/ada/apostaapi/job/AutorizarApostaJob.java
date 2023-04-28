package br.com.ada.apostaapi.job;

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
public class AutorizarApostaJob implements InitializingBean {
    private final ApostaService service;
    @Override
    public void afterPropertiesSet() throws Exception {
        var executors = Executors.newSingleThreadScheduledExecutor();
        executors.scheduleWithFixedDelay(() -> {
            Flux.defer(() -> service.getAllByStatusJob("NAO_INICIADO"))
                    .subscribeOn(Schedulers.boundedElastic())
                    .flatMap(service::authorize)
                    .doOnNext(ApostaId -> log.info("Aposta autorizada - {}", ApostaId))
                    .doOnComplete(() -> log.info("Todos as apostas em NAO_INICIADAS atualizados com sucesso!"))
                    .subscribe();
        }, 1, 45 , TimeUnit.SECONDS);
    }
}
