package de.ananyev.fpla.gendb.repository;

import de.ananyev.fpla.gendb.model.ColumnDefinition;
import de.ananyev.fpla.gendb.model.TableDefinition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by Ilya Ananyev on 30.12.16.
 */
public interface ColumnDefinitionRepository extends JpaRepository<ColumnDefinition, Long> {
    public ColumnDefinition findOneByTableDefinitionAndName(TableDefinition tableDefinition, String name);

    @Transactional
    public Long deleteByTableDefinition(TableDefinition tableDefinition);
}
