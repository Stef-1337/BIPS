package de.ostfalia.bips.ws23.camunda.database.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import org.hibernate.Hibernate;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class ProjektHasLieferantId implements Serializable {
    private static final long serialVersionUID = -3516671756636616869L;
    @Column(name = "id_projekt", nullable = false)
    private Integer idProjekt;

    @Column(name = "id_lieferant", nullable = false)
    private Integer idLieferant;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        ProjektHasLieferantId entity = (ProjektHasLieferantId) o;
        return Objects.equals(this.idLieferant, entity.idLieferant) &&
                Objects.equals(this.idProjekt, entity.idProjekt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idLieferant, idProjekt);
    }

}