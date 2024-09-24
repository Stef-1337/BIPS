package de.ostfalia.bips.ws23.camunda.database.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "frage", schema = "supplierselection", indexes = {
        @Index(name = "fk_Frage_Kategorie1_idx", columnList = "id_kategorie")
})
public class Frage {
    @Id
    @Column(name = "id_frage", nullable = false)
    private Integer id;

    @Column(name = "fragetext", nullable = false, length = 450)
    private String fragetext;

    @ManyToOne
    @JoinColumn(name = "id_kategorie", insertable = false, updatable = false)
    private Kategorie kategorie;

    public Kategorie getKategorie() {
        return kategorie;
    }

    public void setKategorie(Kategorie kategorie) {
        this.kategorie = kategorie;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getFragetext() {
        return fragetext;
    }

    public void setFragetext(String fragetext) {
        this.fragetext = fragetext;
    }

}