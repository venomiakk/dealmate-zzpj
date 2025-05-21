package pl.zzpj.dealmate.deckservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.zzpj.dealmate.deckservice.model.DeckEntity;
import pl.zzpj.dealmate.deckservice.model.PileEntity;

import java.util.Optional;


@Repository
public interface PileRepository extends JpaRepository<PileEntity, Long> {
    Optional<PileEntity> findByPileNameAndDeck(String pileName, DeckEntity deck);
}
