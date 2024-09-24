package de.ostfalia.bips.ws23.camunda.database.repository;

import de.ostfalia.bips.ws23.camunda.database.domain.ProjektHasLieferantHasAntwort;
import de.ostfalia.bips.ws23.camunda.database.domain.ProjektHasLieferantHasAntwortId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.relational.core.sql.In;

import java.util.List;
import java.util.Optional;

public interface ProjektHasLieferantHasAntwortRepository extends JpaRepository<ProjektHasLieferantHasAntwort,
        ProjektHasLieferantHasAntwortId> {

    /**
     * Ueberprueft ob eine bestimmte Antwort eines Lieferanten in einem Projekt als KO-Kriterium gilt.
     * @param idProjekt
     * @param idLieferant
     * @param idAntwort
     * @return ist Antwort ein KO-Kriterium? (0 -> "nein"; 1 -> "ja")
     */
    @Query("SELECT pha.istKoKriterium FROM ProjektHasAntwort pha " +
            "JOIN Antwort a ON pha.id.idAntwort = a.id.idAntwort " +
            "JOIN ProjektHasLieferantHasAntwort phlha ON phlha.id.idAntwort = a.id.idAntwort " +
            "WHERE phlha.id.idProjekt = :idProjekt AND pha.id.idProjekt = :idProjekt " +
            "AND phlha.id.idLieferant = :idLieferant " +
            "AND phlha.id.idAntwort = :idAntwort")
    Optional<Byte> istAntwortKoKriterium(Integer idProjekt, Integer idLieferant, Integer idAntwort);

    /**
     * Holt alle Antworten eines spezifischen Lieferanten in einem bestimmten Projekt.
     * @param idProjekt
     * @param idLieferant
     * @return Liste der vorhandenen Antworten eines Lieferanten in einem Projekt.
     */
    @Query("SELECT a FROM ProjektHasLieferantHasAntwort a " +
            "WHERE a.id.idProjekt = :idProjekt AND a.id.idLieferant = :idLieferant ")
    List<ProjektHasLieferantHasAntwort> findByIdProjektAndIdLieferant(int idProjekt, int idLieferant);

    /**
     * Zaehlt wie viele Antworten ein Lieferant zu einem bestimmten Fragebogen in einem Projekt gegeben hat.
     * @param fragebogenId
     * @param projektId
     * @param lieferantId
     * @return Anzahl der gegebenen Antworten (int)
     */
    @Query("SELECT COUNT(p) FROM ProjektHasLieferantHasAntwort p " +
            "JOIN FragebogenHasFrage f ON p.id.idFrage = f.id.idFrage " +
            "WHERE f.id.idFragebogen = :fragebogenId " +
            " AND p.id.idProjekt = :projektId AND p.id.idLieferant = :lieferantId")
    int findBereitsBeantwortet(Integer fragebogenId, Integer projektId,  Integer lieferantId);

    /**
     * Berechnet den Durchschnitt der Punkte, die ein Lieferant in einer bestimmten Kategorie eines Projektes
     * erhalten hat.
     * @param idLieferant
     * @param idProjekt
     * @param idKategorie
     * @return Durchschnittswert (Float)
     */
    @Query("SELECT AVG(a.punkte) FROM Antwort a " +
            "JOIN Frage f ON a.id.idFrage = f.id " +
            "JOIN Kategorie k ON f.kategorie.id = k.id " +
            "JOIN ProjektHasLieferantHasAntwort phlha ON phlha.id.idAntwort = a.id.idAntwort " +
            "JOIN ProjektHasLieferant phl ON phl.id.idLieferant = phlha.id.idLieferant " +
            "WHERE phl.id.idLieferant = :idLieferant " +
            "AND phlha.id.idProjekt = :idProjekt AND phl.id.idProjekt = :idProjekt AND k.id = :idKategorie ")
    Float findAVGByCategory(Integer idLieferant, Integer idProjekt, Integer idKategorie);

    /**
     * Berechnet die Gesamtpunktzahl, die ein Lieferant in einer bestimmten Kategorie eines Projektes erhalten hat.
     * @param idLieferant
     * @param idProjekt
     * @param idKategorie
     * @return Gesamtpunktzahl (Float)
     */
    @Query("SELECT AVG(a.punkte) FROM Antwort a " +
            "JOIN a.frage f " +
            "JOIN f.kategorie k " +
            "JOIN ProjektHasLieferantHasAntwort phlha ON a.id.idAntwort = phlha.id.idAntwort " +
            "WHERE phlha.projektHasLieferant.id.idLieferant = :idLieferant " +
            "AND phlha.projektHasLieferant.id.idProjekt = :idProjekt " +
            "AND k.id = :idKategorie")
    Float findSumOfPointsByCategory(int idLieferant, int idProjekt, int idKategorie);

    /**
     * Sucht die ID einer Antwort, die ein bestimmter Lieferant zu einer spezifischen Frage
     * in einem Projekt gegeben hat.
     * @param idProjekt
     * @param idLieferant
     * @param idFrage
     * @return ID der Antwort (Integer)
     */
    @Query("SELECT a.id.idAntwort FROM ProjektHasLieferantHasAntwort a WHERE a.id.idProjekt = :idProjekt " +
            "AND a.id.idLieferant = :idLieferant AND a.id.idFrage = :idFrage")
    Integer findAntwortIdByFrageId(Integer idProjekt, Integer idLieferant, Integer idFrage);
}
