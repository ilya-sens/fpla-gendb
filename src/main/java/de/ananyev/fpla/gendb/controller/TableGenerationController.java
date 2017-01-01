package de.ananyev.fpla.gendb.controller;

import de.ananyev.fpla.gendb.model.ColumnDefinition;
import de.ananyev.fpla.gendb.model.TableDefinition;
import de.ananyev.fpla.gendb.repository.ColumnDefinitionRepository;
import de.ananyev.fpla.gendb.repository.TableDefinitionRepository;
import de.ananyev.fpla.gendb.util.exception.TableNotFoundException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
		removeTable(tableDefinition.getTableName());
		// prepare
		ArrayList<String> rowStrings = new ArrayList<>();
		tableDefinition.getColumnDefinitions().forEach(it -> {
			rowStrings.add(String.format("%s %s", it.getName(), it.getType()));
		});

		// execute
		String sql = String.format("create table %s (", tableDefinition.getTableName())
				+ String.join(", ", rowStrings) +")";
		this.jdbcTemplate.execute(sql);

		// save
		this.tableDefinitionRepository.save(tableDefinition);
		tableDefinition.getColumnDefinitions().forEach(it -> {
			it.setTableDefinition(tableDefinition);
			this.columnDefinitionRepository.save(it);
		});
	}

	@GetMapping("/{tableName}")
	public List getFrom(@PathVariable String tableName) {
		return this.jdbcTemplate.queryForList(String.format("select * from %s", tableName));
	}


	@DeleteMapping("/{tableName}")
	public void removeTable(@PathVariable String tableName) {
		this.jdbcTemplate.execute(String.format("drop table if exists %s", tableName));
		this.tableDefinitionRepository.findOneByTableName(tableName)
				.ifPresent(columnDefinition -> this.tableDefinitionRepository.delete(columnDefinition));
	}
}