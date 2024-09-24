package de.ostfalia.bips.ws23.camunda.database.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import org.hibernate.Hibernate;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class FragebogenHasFrageId implements Serializable {
    private static final long serialVersionUID = -3345589071747843268L;
    @Column(name = "id_fragebogen", nullable = false)
    private Integer idFragebogen;

    @Column(name = "id_frage", nullable = false)
    private Integer idFrage;

    public Integer getIdFragebogen() {
        return idFragebogen;
    }

    public void setIdFragebogen(Integer idFragebogen) {
        this.idFragebogen = idFragebogen;
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
        FragebogenHasFrageId entity = (FragebogenHasFrageId) o;
        return Objects.equals(this.idFrage, entity.idFrage) &&
                Objects.equals(this.idFragebogen, entity.idFragebogen);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idFrage, idFragebogen);
    }

}