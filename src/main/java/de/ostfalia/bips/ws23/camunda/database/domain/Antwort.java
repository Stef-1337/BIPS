package de.ostfalia.bips.ws23.camunda.database.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "antwort", schema = "supplierselection", indexes = {
        @Index(name = "fk_Antwort_Frage1_idx", columnList = "id_frage")
})
public class Antwort {
    @EmbeddedId
    private AntwortId id;

    @Column(name = "antworttext", nullable = false, length = 250)
    private String antworttext;

    @Column(name = "punkte", nullable = false)
    private Integer punkte;

    @ManyToOne
    @JoinColumn(name = "id_frage", insertable = false, updatable = false)
    private Frage frage;

    public Frage getFrage() {
        return frage;
    }

    public void setFrage(Frage frage) {
        this.frage = frage;
    }

    public AntwortId getId() {
        return id;
    }

    public void setId(AntwortId id) {
        this.id = id;
    }

    public String getAntworttext() {
        return antworttext;
    }

    public void setAntworttext(String antworttext) {
        this.antworttext = antworttext;
    }

    public Integer getPunkte() {
        return punkte;
    }

    public void setPunkte(Integer punkte) {
        this.punkte = punkte;
    }

}