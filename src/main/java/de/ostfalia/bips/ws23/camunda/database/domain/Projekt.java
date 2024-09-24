package de.ostfalia.bips.ws23.camunda.database.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "projekt", schema = "supplierselection", indexes = {
        @Index(name = "fk_Projekt_Fragebogen1_idx", columnList = "id_fragebogen")
})
public class Projekt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_projekt", nullable = false)
    private Integer id;

    @Column(name = "name", nullable = false, length = 45)
    private String name;

    @Column(name = "id_komponente", nullable = false, length = 100)
    private String idKomponente;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "id_fragebogen", nullable = false)
    private Fragebogen idFragebogen;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIdKomponente() {
        return idKomponente;
    }

    public void setIdKomponente(String idKomponente) {
        this.idKomponente = idKomponente;
    }

    public Fragebogen getIdFragebogen() {
        return idFragebogen;
    }

    public void setIdFragebogen(Fragebogen idFragebogen) {
        this.idFragebogen = idFragebogen;
    }

}