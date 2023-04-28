package br.com.ada.usuarioapi.repositories;
import br.com.ada.usuarioapi.model.Usuario;
import org.springframework.data.domain.Example;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface UsarioReactiveRepository extends ReactiveMongoRepository<Usuario, String> {

    Mono<Boolean> existsByDocumento(String Documento);
}
