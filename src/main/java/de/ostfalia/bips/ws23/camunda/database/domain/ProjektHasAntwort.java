package de.ostfalia.bips.ws23.camunda.database.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "projekt_has_antwort", schema = "supplierselection", indexes = {
        @Index(name = "fk_Projekt_has_Antwort_Projekt1_idx", columnList = "id_projekt"),
        @Index(name = "fk_Projekt_has_Antwort_Antwort1_idx", columnList = "id_antwort, id_frage")
})
public class ProjektHasAntwort {
    @EmbeddedId
    private ProjektHasAntwortId id;

    @MapsId("idProjekt")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_projekt", nullable = false)
    private Projekt idProjekt;

    @MapsId("id")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumns({
            @JoinColumn(name = "id_antwort", referencedColumnName = "id_antwort", nullable = false),
            @JoinColumn(name = "id_frage", referencedColumnName = "id_frage", nullable = false)
    })
    private Antwort antwort;

    @Column(name = "ist_ko_kriterium", nullable = false)
    private Byte istKoKriterium;

    public ProjektHasAntwortId getId() {
        return id;
    }

    public void setId(ProjektHasAntwortId id) {
        this.id = id;
    }

    public Projekt getIdProjekt() {
        return idProjekt;
    }

    public void setIdProjekt(Projekt idProjekt) {
        this.idProjekt = idProjekt;
    }

    public Antwort getAntwort() {
        return antwort;
    }

    public void setAntwort(Antwort antwort) {
        this.antwort = antwort;
    }

    public Byte getIstKoKriterium() {
        return istKoKriterium;
    }

    public void setIstKoKriterium(Byte istKoKriterium) {
        this.istKoKriterium = istKoKriterium;
    }

}