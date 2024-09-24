package de.ostfalia.bips.ws23.camunda.database.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "kategorie", schema = "supplierselection")
public class Kategorie {
    @Id
    @Column(name = "id_kategorie", nullable = false)
    private Integer id;

    @Column(name = "beschreibung", nullable = false, length = 45)
    private String beschreibung;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getBeschreibung() {
        return beschreibung;
    }

    public void setBeschreibung(String beschreibung) {
        this.beschreibung = beschreibung;
    }

}