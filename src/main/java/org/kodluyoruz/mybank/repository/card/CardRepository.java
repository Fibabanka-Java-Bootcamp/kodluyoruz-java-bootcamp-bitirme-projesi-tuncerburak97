package org.kodluyoruz.mybank.repository.card;

import org.kodluyoruz.mybank.entity.card.Card;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
@Primary
public interface CardRepository extends JpaRepository<Card,Long> {
}
