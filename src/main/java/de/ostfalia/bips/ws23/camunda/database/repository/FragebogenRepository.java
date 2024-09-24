package de.ostfalia.bips.ws23.camunda.database.repository;

import de.ostfalia.bips.ws23.camunda.database.domain.Fragebogen;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FragebogenRepository extends JpaRepository<Fragebogen, Integer> {

}
