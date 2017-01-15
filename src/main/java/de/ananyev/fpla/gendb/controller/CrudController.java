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
 * Created by Ilya Ananyev on 01.01.17.
 */
@RestController
@RequestMapping("/crud/{tableId}")
public class CrudController {
	@Inject
	private JdbcTemplate jdbcTemplate;

	@Inject
	private ColumnDefinitionRepository columnDefinitionRepository;

	@Inject
	private TableDefinitionRepository tableDefinitionRepository;

	// insert into <tableName> set <columnName1> = <value1>, <columnName2> = <value2>;
	@PostMapping
	public void insert(@PathVariable Long tableId, @RequestBody List<Map<String, String>> keyValueList)
			throws TableNotFoundException {
		TableDefinition tableDefinition = this.tableDefinitionRepository.findOne(tableId);
		keyValueList.forEach((row) -> {
			ArrayList<String> rowStrings = this.generateRowStrings(row, tableDefinition);
			String sql = String.format("insert into %s set ", tableDefinition.getTableName()) + String.join(", ", rowStrings);
			this.jdbcTemplate.execute(sql);
		});
	}

	@GetMapping
	public List<Map<String, Object>> getAll(@PathVariable Long tableId) {
		TableDefinition tableDefinition = this.tableDefinitionRepository.findOne(tableId);
		String sql = String.format("select * from %s", tableDefinition.getTableName());
		List<Map<String, Object>> result = this.jdbcTemplate.queryForList(sql);
		return result;
	}

	@PutMapping("/{id}")
	public void update(
			@PathVariable Long tableId,
			@PathVariable Long id,
			@RequestBody Map<String, String> keyValue
	) throws TableNotFoundException {
		TableDefinition tableDefinition = this.tableDefinitionRepository.findOne(tableId);
		ArrayList<String> rowStrings = this.generateRowStrings(keyValue, tableDefinition);
		String sql = String.format("update %s set ", tableDefinition.getTableName()) + String.join(", ", rowStrings)
				+ String.format(" where ID = %d", id);
		this.jdbcTemplate.execute(sql);
	}

	@DeleteMapping("/{id}")
	public void delete(@PathVariable Long tableId, @PathVariable Long id) {
		TableDefinition tableDefinition = this.tableDefinitionRepository.findOne(tableId);
		String sql = String.format("delete from %s where ID = %d", tableDefinition.getTableName(), id);
		this.jdbcTemplate.execute(sql);
	}

	private ArrayList<String> generateRowStrings(Map<String, String> keyValue, TableDefinition tableDefinition) {
		ArrayList<String> rowStrings = new ArrayList<>();
		keyValue.keySet().forEach((columnName) -> {
			ColumnDefinition columnDefinition = this.columnDefinitionRepository
					.findOneByTableDefinitionAndName(tableDefinition, columnName);
			switch (columnDefinition.getType()) {
				case bool:
					boolean columnValueBoolean = Boolean.parseBoolean(keyValue.get(columnName));
					rowStrings.add(String.format("%s = %b", columnName, columnValueBoolean));
					break;
				case date:
					rowStrings.add(String.format("%s = '%s'", columnName, keyValue.get(columnName)));
					break;
				case number:
					int columnValueInteger = Integer.parseInt(keyValue.get(columnName));
					rowStrings.add(String.format("%s = %d", columnName, columnValueInteger));
					break;
				case text:
					rowStrings.add(String.format("%s = '%s'", columnName, keyValue.get(columnName)));
					break;
			}
		});
		return rowStrings;
	}
}
