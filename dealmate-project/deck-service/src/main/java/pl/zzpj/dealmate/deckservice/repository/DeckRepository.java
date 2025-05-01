package pl.zzpj.dealmate.deckservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.zzpj.dealmate.deckservice.model.DeckEntity;

@Repository
public interface DeckRepository extends JpaRepository<DeckEntity, Long> {
}
