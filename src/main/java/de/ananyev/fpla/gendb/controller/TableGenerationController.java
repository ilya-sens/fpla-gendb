package de.ananyev.fpla.gendb.controller;

import de.ananyev.fpla.gendb.model.ColumnDefinition;
import de.ananyev.fpla.gendb.model.TableDefinition;
import de.ananyev.fpla.gendb.model.enumeration.ColumnType;
import de.ananyev.fpla.gendb.repository.ColumnDefinitionRepository;
import de.ananyev.fpla.gendb.repository.TableDefinitionRepository;
import de.ananyev.fpla.gendb.util.SqlExecuterUtil;
import de.ananyev.fpla.gendb.util.exception.TableNotFoundException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import java.util.*;

/**
 * Table generation: CRUD for generic tables.
 * Created by Ilya Ananyev on 24.12.16.
 */
@RestController
@RequestMapping("/table")
public class TableGenerationController {
    @Inject
    private JdbcTemplate jdbcTemplate;

    @Inject
    private ColumnDefinitionRepository columnDefinitionRepository;

    @Inject
    private TableDefinitionRepository tableDefinitionRepository;

    @PostMapping
    public TableDefinition createTable(@RequestBody TableDefinition tableDefinition) {
        this.tableDefinitionRepository.findOneByTableName(tableDefinition.getTableName()).ifPresent((it) -> {
                    removeTable(it.getId());
                }
        );
        this.jdbcTemplate.execute(String.format("drop table if exists %s", tableDefinition.getTableName()));
        // prepare
        ArrayList<String> rowStrings = new ArrayList<>();
        rowStrings.add("id int auto_increment primary key");
        tableDefinition.getColumnDefinitions().forEach(it -> {
            rowStrings.add(String.format("%s %s", it.getName(), it.getType()));
        });

        // execute
        String sql = String.format("create table %s (", tableDefinition.getTableName())
                + String.join(", ", rowStrings) + ")";
        SqlExecuterUtil.execute(this.jdbcTemplate, sql).longValue();

        // save
        this.tableDefinitionRepository.save(tableDefinition);
        ColumnDefinition idColumnDefinition = new ColumnDefinition();
        idColumnDefinition.setName("ID");
        idColumnDefinition.setType(ColumnType.number);
        tableDefinition.getColumnDefinitions().add(0, idColumnDefinition);
        tableDefinition.getColumnDefinitions().forEach(it -> {
            it.setTableDefinition(tableDefinition);
            this.columnDefinitionRepository.save(it);
        });

        return tableDefinition;
    }

    @PutMapping()
    public TableDefinition updateTable(@RequestBody TableDefinition inputTableDefinition)
            throws TableNotFoundException {
        TableDefinition existingTableDefinition = this.tableDefinitionRepository
                .findOne(inputTableDefinition.getId());
        if (existingTableDefinition == null) {
            throw new TableNotFoundException();
        }

        // update table if need
        if (!existingTableDefinition.getTableName().equals(inputTableDefinition.getTableName())) {
            this.jdbcTemplate.execute(String.format("alter table %s rename to %s", existingTableDefinition.getTableName(),
                    inputTableDefinition.getTableName()));
            TableDefinition updatedTableDefinition = this.tableDefinitionRepository.save(inputTableDefinition);
        }

        List<ColumnDefinition> inputColumnDefinitions = inputTableDefinition.getColumnDefinitions(),
                existingColumnDefinitions = existingTableDefinition.getColumnDefinitions();
        // check if column definitions are equal
        if (!Arrays.deepEquals(inputColumnDefinitions.toArray(), existingColumnDefinitions.toArray())) {
            // remove columns definitions that are not exist in input column definitions
            existingColumnDefinitions.forEach((existingColumnDefinition) -> {
                boolean isDeleted = inputColumnDefinitions.stream().noneMatch(inputColumnDefinition ->
                        Objects.equals(inputColumnDefinition.getId(), existingColumnDefinition.getId()));
                if (isDeleted) {
                    this.columnDefinitionRepository.delete(existingColumnDefinition.getId());
                }
            });

            // check which column definitions need to be updated
            inputTableDefinition.getColumnDefinitions().forEach((inputColumnDefinition) -> {
                Optional<ColumnDefinition> optionalColumnDefinition = existingColumnDefinitions.stream()
                        .filter(existingColumnDefinition -> !inputColumnDefinition.getName().equals("ID")
                                && Objects.equals(inputColumnDefinition.getId(), existingColumnDefinition.getId()))
                        .findAny();
                // ignore the found column if exists and definition equals incoming one
                if (optionalColumnDefinition.isPresent()) {
                    ColumnDefinition foundColumnDefinition = optionalColumnDefinition.get();
                    if (!foundColumnDefinition.getName().equals(inputColumnDefinition.getName())) {
                        this.jdbcTemplate.execute(String.format("alter table %s alter column %s rename to %s",
                                inputTableDefinition.getTableName(), foundColumnDefinition.getName(),
                                inputColumnDefinition.getName()));
                        this.columnDefinitionRepository.save(inputColumnDefinition);
                    }
                    if (!foundColumnDefinition.getType().equals(inputColumnDefinition.getType())) {
                        this.jdbcTemplate.execute(String.format("alter table %s alter column %s %s",
                                inputTableDefinition.getTableName(), inputColumnDefinition.getName(),
                                inputColumnDefinition.getType()));
                        this.columnDefinitionRepository.save(inputColumnDefinition);
                    }
                } else {
                    this.columnDefinitionRepository.save(inputColumnDefinition);
                }
            });
        }

        return existingTableDefinition;
    }

    @GetMapping("/{id}")
    public TableDefinition get(@PathVariable Long id) {
        return this.tableDefinitionRepository.findOne(id);
    }

    @GetMapping
    public List<TableDefinition> listTables() {
        return this.tableDefinitionRepository.findAll();
    }

    @DeleteMapping("/{id}")
    public void removeTable(@PathVariable Long id) {
        TableDefinition tableDefinition = this.tableDefinitionRepository.findOne(id);
        this.columnDefinitionRepository.deleteByTableDefinition(tableDefinition);
        this.tableDefinitionRepository.delete(tableDefinition);
        this.jdbcTemplate.execute(String.format("drop table if exists %s", tableDefinition.getTableName()));
    }

    @GetMapping("/columnTypes")
    public ColumnType[] getColumnTypes() {
        return ColumnType.values();
    }
}