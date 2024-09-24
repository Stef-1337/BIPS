package de.ostfalia.bips.ws23.camunda.database.repository;

import de.ostfalia.bips.ws23.camunda.database.domain.FragebogenHasFrage;
import de.ostfalia.bips.ws23.camunda.database.domain.FragebogenHasFrageId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface FragebogenHasFrageRepository extends JpaRepository<FragebogenHasFrage, FragebogenHasFrageId> {

    /**
     * Ermittelt die IDs der Fragen, die einem bestimmten Fragebogen zugeordnet sind.
     * @param fragebogenId
     * @return Liste der IDs (Integer) der Fragen, die zum angegebenen Fragebogen gehoeren.
     */
    @Query("SELECT f.id.idFrage From FragebogenHasFrage f WHERE f.id.idFragebogen = :fragebogenId ")
    List<Integer> findFragenByFragebogenId(Integer fragebogenId);

    /**
     * Zaehlt die Anzahl der Fragen, die einem bestimmten Fragebogen zugeordnet sind.
     * @param fragebogenId
     * @return Anzahl der Fragen (int), die zum angegebenen Fragebogen gehoeren.
     */
    @Query("SELECT  COUNT(f) FROM FragebogenHasFrage f WHERE f.id.idFragebogen = :fragebogenId")
    int countByFragebogenId(Integer fragebogenId);
}
