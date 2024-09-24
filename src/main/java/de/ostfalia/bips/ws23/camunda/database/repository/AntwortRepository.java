package de.ostfalia.bips.ws23.camunda.database.repository;

import de.ostfalia.bips.ws23.camunda.database.domain.Antwort;
import de.ostfalia.bips.ws23.camunda.database.domain.AntwortId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AntwortRepository extends JpaRepository<Antwort, AntwortId> {

    /**
     * Holt eine Liste von Antwort-Objekten.
      * @param fragenIds
     * @return Liste von Antwort-Objekten, die zu den angegebenenen Frage-IDs gehoeren.
     */
    @Query("SELECT a FROM Antwort a WHERE a.id.idFrage IN :fragenIds")
    List<Antwort> findByFragenIds(List<Integer> fragenIds);

    /**
     * Holt eine Liste von Antwort-Objekten fuer eine spezifische Fragen-ID.
     * @param idFrage
     * @return Liste von Antwort-Objekten, die zur angegebenen Frage-ID gehoeren.
     */
    @Query("SELECT a FROM Antwort a WHERE a.id.idFrage = :idFrage")
    List<Antwort> findByFrageId(int idFrage);
}
