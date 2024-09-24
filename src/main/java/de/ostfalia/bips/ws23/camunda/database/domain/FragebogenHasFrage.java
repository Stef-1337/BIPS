package de.ostfalia.bips.ws23.camunda.database.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "fragebogen_has_frage", schema = "supplierselection", indexes = {
        @Index(name = "fk_Fragebogen_has_Frage_Fragebogen_idx", columnList = "id_fragebogen"),
        @Index(name = "fk_Fragebogen_has_Frage_Frage1_idx", columnList = "id_frage")
})
public class FragebogenHasFrage {
    @EmbeddedId
    private FragebogenHasFrageId id;

    @MapsId("idFragebogen")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_fragebogen", nullable = false)
    private Fragebogen idFragebogen;

    @MapsId("idFrage")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_frage", nullable = false)
    private Frage idFrage;

    public FragebogenHasFrageId getId() {
        return id;
    }

    public void setId(FragebogenHasFrageId id) {
        this.id = id;
    }

    public Fragebogen getIdFragebogen() {
        return idFragebogen;
    }

    public void setIdFragebogen(Fragebogen idFragebogen) {
        this.idFragebogen = idFragebogen;
    }

    public Frage getIdFrage() {
        return idFrage;
    }

    public void setIdFrage(Frage idFrage) {
        this.idFrage = idFrage;
    }

}