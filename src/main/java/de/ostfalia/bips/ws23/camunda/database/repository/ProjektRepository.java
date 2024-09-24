package de.ostfalia.bips.ws23.camunda.database.repository;

import de.ostfalia.bips.ws23.camunda.database.domain.Projekt;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjektRepository extends JpaRepository<Projekt, Integer> {
}
