package de.ostfalia.bips.ws23.camunda.database.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import org.hibernate.Hibernate;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class KategoriegewichtInProjektId implements Serializable {
    private static final long serialVersionUID = -5678177211722212805L;
    @Column(name = "id_kategorie", nullable = false)
    private Integer idKategorie;

    @Column(name = "id_projekt", nullable = false)
    private Integer idProjekt;

    public Integer getIdKategorie() {
        return idKategorie;
    }

    public void setIdKategorie(Integer idKategorie) {
        this.idKategorie = idKategorie;
    }

    public Integer getIdProjekt() {
        return idProjekt;
    }

    public void setIdProjekt(Integer idProjekt) {
        this.idProjekt = idProjekt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        KategoriegewichtInProjektId entity = (KategoriegewichtInProjektId) o;
        return Objects.equals(this.idKategorie, entity.idKategorie) &&
                Objects.equals(this.idProjekt, entity.idProjekt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idKategorie, idProjekt);
    }

}