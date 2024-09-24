package de.ostfalia.bips.ws23.camunda.database.repository;

import de.ostfalia.bips.ws23.camunda.database.domain.ProjektHasLieferant;
import de.ostfalia.bips.ws23.camunda.database.domain.ProjektHasLieferantId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProjektHasLieferantRepository extends JpaRepository<ProjektHasLieferant, ProjektHasLieferantId> {

    /**
     * Ermittelt den Rang eines bestimmten Lieferanten in einem bestimmten Projekt, basierend auf dem score.
     * @param idProjekt
     * @param idLieferant
     * @return Der Rang (Integer) des Lieferanten im angegebenen Projekt.
     */
    @Query(value = "SELECT r.rang FROM (SELECT p.id_lieferant, RANK() OVER (ORDER BY p.score DESC) AS rang " +
            "FROM projekt_has_lieferant p WHERE p.id_projekt = :idProjekt) AS r " +
            "WHERE r.id_lieferant = :idLieferant", nativeQuery = true)
    Integer findRank(int idProjekt, int idLieferant);

    /**
     * Zaehlt die Anzahl der Lieferanten in einem bestimmten Projekt.
     * @param idProjekt
     * @return Anzahl der Lieferanten (Integer) im angegebenen Projekt.
     */
    @Query("SELECT COUNT(phl) FROM ProjektHasLieferant phl WHERE phl.id.idProjekt = :idProjekt")
    Integer countLieferantenImProjekt(int idProjekt);

    /**
     * Ueberprueft, ob eine bestimmte Kombination aus Projekt-ID und Lieferant-ID in der Datenbank existiert.
     * @param projektId
     * @param lieferantId
     * @return boolean "true", wenn existiert, andernfalls "false".
     */
    @Query("SELECT COUNT(p) > 0 FROM ProjektHasLieferant p" +
            " WHERE p.id.idProjekt = :projektId AND p.id.idLieferant = :lieferantId")
    boolean existsByProjektIdAndLieferantId(Integer projektId, Integer lieferantId);

    @Query("SELECT phl FROM ProjektHasLieferant phl WHERE phl.id.idProjekt = :idProjekt")
    List<ProjektHasLieferant> findAllLieferantenByProjektId(Integer idProjekt);
}
