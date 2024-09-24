package de.ostfalia.bips.ws23.camunda.database.repository;

import de.ostfalia.bips.ws23.camunda.database.domain.Frage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FrageRepository extends JpaRepository<Frage, Integer> {

}
