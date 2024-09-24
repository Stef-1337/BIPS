package de.ostfalia.bips.ws23.camunda.database.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import org.hibernate.Hibernate;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class ProjektHasLieferantHasAntwortId implements Serializable {
    private static final long serialVersionUID = -6538800676696597955L;
    @Column(name = "id_projekt", nullable = false)
    private Integer idProjekt;

    @Column(name = "id_lieferant", nullable = false)
    private Integer idLieferant;

    @Column(name = "id_frage", nullable = false)
    private Integer idFrage;

    @Column(name = "id_antwort", nullable = false)
    private Integer idAntwort;

    public Integer getIdProjekt() {
        return idProjekt;
    }

    public void setIdProjekt(Integer idProjekt) {
        this.idProjekt = idProjekt;
    }

    public Integer getIdLieferant() {
        return idLieferant;
    }

    public void setIdLieferant(Integer idLieferant) {
        this.idLieferant = idLieferant;
    }

    public Integer getIdFrage() {
        return idFrage;
    }

    public void setIdFrage(Integer idFrage) {
        this.idFrage = idFrage;
    }

    public Integer getIdAntwort() {
        return idAntwort;
    }

    public void setIdAntwort(Integer idAntwort) {
        this.idAntwort = idAntwort;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        ProjektHasLieferantHasAntwortId entity = (ProjektHasLieferantHasAntwortId) o;
        return Objects.equals(this.idLieferant, entity.idLieferant) &&
                Objects.equals(this.idProjekt, entity.idProjekt) &&
                Objects.equals(this.idFrage, entity.idFrage) &&
                Objects.equals(this.idAntwort, entity.idAntwort);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idLieferant, idProjekt, idFrage, idAntwort);
    }

}