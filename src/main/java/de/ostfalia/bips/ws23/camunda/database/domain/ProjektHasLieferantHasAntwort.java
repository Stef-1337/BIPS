package de.ostfalia.bips.ws23.camunda.database.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "projekt_has_lieferant_has_antwort", schema = "supplierselection", indexes = {
        @Index(name = "fk_Projekt_has_Lieferant_has_Antwort_Projekt_has_Lieferant1_idx", columnList = "id_projekt, id_lieferant"),
        @Index(name = "fk_Projekt_has_Lieferant_has_Antwort_Antwort1_idx", columnList = "id_frage, id_antwort")
})
public class ProjektHasLieferantHasAntwort {
    @EmbeddedId
    private ProjektHasLieferantHasAntwortId id;

    @MapsId("id")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumns({
            @JoinColumn(name = "id_projekt", referencedColumnName = "id_projekt", nullable = false),
            @JoinColumn(name = "id_lieferant", referencedColumnName = "id_lieferant", nullable = false)
    })
    private ProjektHasLieferant projektHasLieferant;

    @MapsId("id")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumns({
            @JoinColumn(name = "id_frage", referencedColumnName = "id_antwort", nullable = false),
            @JoinColumn(name = "id_antwort", referencedColumnName = "id_frage", nullable = false)
    })
    private Antwort antwort;

    public ProjektHasLieferantHasAntwortId getId() {
        return id;
    }

    public void setId(ProjektHasLieferantHasAntwortId id) {
        this.id = id;
    }

    public ProjektHasLieferant getProjektHasLieferant() {
        return projektHasLieferant;
    }

    public void setProjektHasLieferant(ProjektHasLieferant projektHasLieferant) {
        this.projektHasLieferant = projektHasLieferant;
    }

    public Antwort getAntwort() {
        return antwort;
    }

    public void setAntwort(Antwort antwort) {
        this.antwort = antwort;
    }

}