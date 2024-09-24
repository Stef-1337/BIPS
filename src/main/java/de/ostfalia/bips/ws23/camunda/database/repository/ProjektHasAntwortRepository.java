package de.ostfalia.bips.ws23.camunda.database.repository;

import de.ostfalia.bips.ws23.camunda.database.domain.ProjektHasAntwort;
import de.ostfalia.bips.ws23.camunda.database.domain.ProjektHasAntwortId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProjektHasAntwortRepository extends JpaRepository<ProjektHasAntwort, ProjektHasAntwortId> {

    /**
     * Berechnet das Maximum der Punkte fuer jede Frage in einer bestimmten Kategorie eines Projektes.
     * @param idKategorie
     * @param idProjekt
     * @return Liste von Object-Arrays (idFrage; maxPunktzahl)
     */
    @Query("SELECT f.id, MAX(a.punkte) " +
            "FROM Antwort a " +
            "JOIN a.frage f " +
            "JOIN f.kategorie k " +
            "JOIN ProjektHasAntwort pha ON a.id.idAntwort = pha.antwort.id.idAntwort " +
            "WHERE k.id = :idKategorie AND pha.id.idProjekt = :idProjekt " +
            "GROUP BY f.id")
    List<Object[]> findMaxPointsForEachQuestion(Integer idKategorie, Integer idProjekt);

    /**
     * Holt alle ProjektHasAntwort-Eintraege fuer ein spezifisches Projekt.
     * @param idProjekt
     * @return Liste ProjektHasAntwort-Objekte fuer ein spezifisches Projekt.
     */
    @Query("SELECT a FROM ProjektHasAntwort a WHERE a.id.idProjekt = :idProjekt")
    List<ProjektHasAntwort> findByProjektId(int idProjekt);

}
