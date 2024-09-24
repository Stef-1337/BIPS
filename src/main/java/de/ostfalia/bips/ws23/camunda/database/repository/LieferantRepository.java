package de.ostfalia.bips.ws23.camunda.database.repository;

import de.ostfalia.bips.ws23.camunda.database.domain.Lieferant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface LieferantRepository extends JpaRepository<Lieferant, Integer> {

    /**
     * Sucht einen Lieferanten anhand seines Namens.
     * @param name
     * @return Lieferant-Objekt, das mit dem angegebenen Namen uebereinstimmt.
     */
    @Query("SELECT l FROM Lieferant l WHERE l.name = :name")
    Optional<Lieferant> findByName(String name);


}
