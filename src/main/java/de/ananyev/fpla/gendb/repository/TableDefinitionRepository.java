package de.ananyev.fpla.gendb.repository;

import de.ananyev.fpla.gendb.model.TableDefinition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Created by Ilya Ananyev on 30.12.16.
 */
public interface TableDefinitionRepository extends JpaRepository<TableDefinition, Long> {
	Optional<TableDefinition> findOneByTableName(String tableName);
}
