package de.ostfalia.bips.ws23.camunda.database.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "kategoriegewicht_in_projekt", schema = "supplierselection", indexes = {
        @Index(name = "fk_Kategorie_has_Projekt_Kategorie1_idx", columnList = "id_kategorie"),
        @Index(name = "fk_Kategorie_has_Projekt_Projekt1_idx", columnList = "id_projekt")
})
public class KategoriegewichtInProjekt {

    public static KategoriegewichtInProjekt erstelleKategoriegewicht(Kategorie kategorie, int idProjekt, float gewicht) {

        KategoriegewichtInProjektId id = new KategoriegewichtInProjektId();
        id.setIdKategorie(kategorie.getId());
        id.setIdProjekt(idProjekt);

        KategoriegewichtInProjekt kategoriegewicht = new KategoriegewichtInProjekt();
        kategoriegewicht.setId(id);
        kategoriegewicht.setGewicht(gewicht);
        kategoriegewicht.setIdKategorie(kategorie);

        return kategoriegewicht;
    }
    @EmbeddedId
    private KategoriegewichtInProjektId id;

    @MapsId("idKategorie")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_kategorie", nullable = false)
    private Kategorie idKategorie;

    @Column(name = "gewicht", nullable = false)
    private Float gewicht;

    public KategoriegewichtInProjektId getId() {
        return id;
    }

    public void setId(KategoriegewichtInProjektId id) {
        this.id = id;
    }

    public Kategorie getIdKategorie() {
        return idKategorie;
    }

    public void setIdKategorie(Kategorie idKategorie) {
        this.idKategorie = idKategorie;
    }

    public Float getGewicht() {
        return gewicht;
    }

    public void setGewicht(Float gewicht) {
        this.gewicht = gewicht;
    }

}