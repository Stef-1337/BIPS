package de.ostfalia.bips.ws23.camunda.database.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import org.hibernate.Hibernate;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class ProjektHasAntwortId implements Serializable {
    private static final long serialVersionUID = -4962243006855431195L;
    @Column(name = "id_projekt", nullable = false)
    private Integer idProjekt;

    @Column(name = "id_antwort", nullable = false)
    private Integer idAntwort;

    @Column(name = "id_frage", nullable = false)
    private Integer idFrage;

    public Integer getIdProjekt() {
        return idProjekt;
    }

    public void setIdProjekt(Integer idProjekt) {
        this.idProjekt = idProjekt;
    }

    public Integer getIdAntwort() {
        return idAntwort;
    }

    public void setIdAntwort(Integer idAntwort) {
        this.idAntwort = idAntwort;
    }

    public Integer getIdFrage() {
        return idFrage;
    }

    public void setIdFrage(Integer idFrage) {
        this.idFrage = idFrage;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        ProjektHasAntwortId entity = (ProjektHasAntwortId) o;
        return Objects.equals(this.idProjekt, entity.idProjekt) &&
                Objects.equals(this.idFrage, entity.idFrage) &&
                Objects.equals(this.idAntwort, entity.idAntwort);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idProjekt, idFrage, idAntwort);
    }

}