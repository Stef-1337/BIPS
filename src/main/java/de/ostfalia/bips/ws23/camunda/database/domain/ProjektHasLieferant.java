package de.ostfalia.bips.ws23.camunda.database.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "projekt_has_lieferant", schema = "supplierselection", indexes = {
        @Index(name = "fk_Projekt_has_Lieferant_Projekt1_idx", columnList = "id_projekt"),
        @Index(name = "fk_Projekt_has_Lieferant_Lieferant1_idx", columnList = "id_lieferant")
})
public class ProjektHasLieferant {
    @EmbeddedId
    private ProjektHasLieferantId id;

    @MapsId("idProjekt")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_projekt", nullable = false)
    private Projekt idProjekt;

    @MapsId("idLieferant")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_lieferant", nullable = false)
    private Lieferant idLieferant;

    @Column(name = "score")
    private Float score;

    @Column(name = "`rank`")
    private Integer rank;

    public ProjektHasLieferantId getId() {
        return id;
    }

    public void setId(ProjektHasLieferantId id) {
        this.id = id;
    }

    public Projekt getIdProjekt() {
        return idProjekt;
    }

    public void setIdProjekt(Projekt idProjekt) {
        this.idProjekt = idProjekt;
    }

    public Lieferant getIdLieferant() {
        return idLieferant;
    }

    public void setIdLieferant(Lieferant idLieferant) {
        this.idLieferant = idLieferant;
    }

    public Float getScore() {
        return score;
    }

    public void setScore(Float score) {
        this.score = score;
    }

    public Integer getRank() {
        return rank;
    }

    public void setRank(Integer rank) {
        this.rank = rank;
    }

}