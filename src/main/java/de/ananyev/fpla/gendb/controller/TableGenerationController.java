package de.ananyev.fpla.gendb.controller;

import de.ananyev.fpla.gendb.model.ColumnDefinition;
import de.ananyev.fpla.gendb.model.TableDefinition;
import de.ananyev.fpla.gendb.model.enumeration.ColumnType;
import de.ananyev.fpla.gendb.repository.ColumnDefinitionRepository;
import de.ananyev.fpla.gendb.repository.TableDefinitionRepository;
import de.ananyev.fpla.gendb.util.exception.TableNotFoundException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
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
	public void createTable(@RequestBody TableDefinition tableDefinition) {
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
		this.jdbcTemplate.execute(sql);

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
	}

	@PutMapping()
	public void updateTable(@RequestBody TableDefinition inputTableDefinition)
			throws TableNotFoundException {
		TableDefinition tableDefinition = this.tableDefinitionRepository
				.findOne(inputTableDefinition.getId());
		if (tableDefinition == null) {
			throw new TableNotFoundException();
		}

		// update table if need
		if (!tableDefinition.getTableName().equals(inputTableDefinition.getTableName())) {
			this.tableDefinitionRepository.save(inputTableDefinition);
		}

		// check which column definitions need to be updated
		inputTableDefinition.getColumnDefinitions().forEach((inputColumnDefinition) -> {
			Optional<ColumnDefinition> optionalColumnDefinition = tableDefinition.getColumnDefinitions().stream()
					.filter(existingColumnDefinition -> !inputColumnDefinition.getName().equals("ID")
							&& Objects.equals(inputColumnDefinition.getId(), existingColumnDefinition.getId()))
					.findAny();
			// ignore the found column if exists and definition equals incoming one
			if (optionalColumnDefinition.isPresent()) {
				ColumnDefinition foundColumnDefinition = optionalColumnDefinition.get();
				if (!foundColumnDefinition.getName().equals(inputColumnDefinition.getName())
						|| !foundColumnDefinition.getType().equals(inputColumnDefinition.getType())) {
					this.columnDefinitionRepository.save(inputColumnDefinition);
				}
			} else {
				this.columnDefinitionRepository.save(inputColumnDefinition);
			}
		});
	}

	@GetMapping("/{id}")
	public TableDefinition get(@PathVariable Long id) {
		return this.tableDefinitionRepository.findOne(id);
	}

	@GetMapping
	public List listTables() {
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