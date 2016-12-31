package de.ananyev.fpla.gendb;

import de.ananyev.fpla.gendb.model.ColumnDefinition;
import de.ananyev.fpla.gendb.model.TableDefinition;
import de.ananyev.fpla.gendb.repository.ColumnDefinitionRepository;
import de.ananyev.fpla.gendb.repository.TableDefinitionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Created by Ilya Ananyev on 24.12.16.
 */
@RestController
@RequestMapping("/table")
public class GenDBController {
	@Inject
	JdbcTemplate jdbcTemplate;

	@Inject
	private ColumnDefinitionRepository columnDefinitionRepository;

	@Inject
	private TableDefinitionRepository tableDefinitionRepository;

	@PostMapping
	public void createTable(@RequestBody TableDefinition tableDefinition) {
		removeTable(tableDefinition.getTableName());
		ArrayList<String> rowsStrings = new ArrayList<>();
		ArrayList<ColumnDefinition> columnDefinitions = new ArrayList<>();
		String sql = String.format("create table %s (", tableDefinition.getTableName());
		tableDefinition.getColumnDefinitions().forEach(it -> {
			rowsStrings.add(String.format("%s %s", it.getName(), it.getType()));
		});
		sql += String.join(", ", rowsStrings) + ")";

		this.jdbcTemplate.execute(sql);

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

	@PostMapping("/insert/{tableName}")
	public void insert(@PathVariable String tableName, @RequestBody List<Map<String, String>> keyValueList) {
		/* ToDo implement */
	}

	@DeleteMapping("/{tableName}")
	public void removeTable(@PathVariable String tableName) {
		this.jdbcTemplate.execute(String.format("drop table if exists %s", tableName));
		this.tableDefinitionRepository.findOneByTableName(tableName)
				.ifPresent(columnDefinition -> this.tableDefinitionRepository.delete(columnDefinition));
	}
}