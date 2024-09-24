package de.ostfalia.bips.ws23.camunda.database.repository;

import de.ostfalia.bips.ws23.camunda.database.domain.KategoriegewichtInProjekt;
import de.ostfalia.bips.ws23.camunda.database.domain.KategoriegewichtInProjektId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KategoriegewichtInProjektRepository extends JpaRepository<KategoriegewichtInProjekt,
        KategoriegewichtInProjektId> {
}
