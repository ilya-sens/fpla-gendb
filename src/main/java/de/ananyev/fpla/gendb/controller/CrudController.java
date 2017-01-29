package de.ananyev.fpla.gendb.controller;

import de.ananyev.fpla.gendb.model.ColumnDefinition;
import de.ananyev.fpla.lib.model.SearchCriteria;
import de.ananyev.fpla.gendb.model.TableDefinition;
import de.ananyev.fpla.gendb.model.enumeration.ColumnType;
import de.ananyev.fpla.gendb.repository.ColumnDefinitionRepository;
import de.ananyev.fpla.gendb.repository.TableDefinitionRepository;
import de.ananyev.fpla.gendb.util.exception.TableNotFoundException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import java.util.*;

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
			ArrayList<String> rowStrings = this.generateRowStrings(row, tableDefinition, true);
			String sql = String.format("insert into %s set ", tableDefinition.getTableName()) + String.join(", ", rowStrings);
			this.jdbcTemplate.execute(sql);
		});
	}

	@GetMapping
	public List<Map<String, Object>> getAll(@PathVariable Long tableId, @RequestParam("blabla") String searchCriteria) {
		TableDefinition tableDefinition = this.tableDefinitionRepository.findOne(tableId);
		String sql = String.format("select * from %s", tableDefinition.getTableName());
		List<Map<String, Object>> result = this.jdbcTemplate.queryForList(sql);
		return result;
	}

	@GetMapping("/{id}")
	public Map<String, Object> getRow(@PathVariable Long tableId, @PathVariable Long id) {
		TableDefinition tableDefinition = this.tableDefinitionRepository.findOne(tableId);
		String sql = String.format("select * from %s where ID = %d", tableDefinition.getTableName(), id);
		Map<String, Object> result = this.jdbcTemplate.queryForMap(sql);
		return result;
	}

	@PutMapping("/{id}")
	public void update(
			@PathVariable Long tableId,
			@PathVariable Long id,
			@RequestBody Map<String, String> keyValue
	) throws TableNotFoundException {
		TableDefinition tableDefinition = this.tableDefinitionRepository.findOne(tableId);
		ArrayList<String> rowStrings = this.generateRowStrings(keyValue, tableDefinition, false);
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

	private ArrayList<String> generateRowStrings(Map<String, String> keyValue, TableDefinition tableDefinition, boolean skipEmpty) {
		ArrayList<String> rowStrings = new ArrayList<>();
		keyValue.keySet().forEach((columnName) -> {
			if (!skipEmpty || !keyValue.get(columnName).equals("")) {
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
			}
		});
		return rowStrings;
	}

	@PostMapping("/search")
	public List<Map<String, Object>> findAll(@PathVariable Long tableId, @RequestBody SearchCriteria searchCriteria) {
		TableDefinition tableDefinition = this.tableDefinitionRepository.findOne(tableId);
		String sql = String.format("select * from %s", tableDefinition.getTableName());
		ArrayList<String> rowStrings = this.generateRowStringForSearch(searchCriteria.getSpecification(), tableDefinition);
		if (!rowStrings.isEmpty()) {
			sql += " where " + String.join(" and ", rowStrings);
		}
		if (searchCriteria.getOrderBy() != null && !searchCriteria.getOrderBy().isEmpty()) {
			sql += String.format(" order by %s", searchCriteria.getOrderBy())
					+ (searchCriteria.isDesc() ? " desc" : "");
		}
		// Limit: if search criteria is random, we use the limit after we found all results -> find all matching to
		// criteria results and filter randomly limit amount
		if (!searchCriteria.isRandom() && (searchCriteria.getLimit() != null && searchCriteria.getLimit() != 0)) {
			sql += String.format(" limit %d", searchCriteria.getLimit());
		}
		if (searchCriteria.getOffset() != null && searchCriteria.getOffset() != 0) {
			sql += String.format(" offset %d", searchCriteria.getOffset());
		}
		List<Map<String, Object>> result = this.jdbcTemplate.queryForList(sql);
		if (searchCriteria.isRandom()) {
			Collections.shuffle(result);
			if (result.size() > searchCriteria.getLimit() && searchCriteria.getLimit() != null
					&& searchCriteria.getLimit() != 0) {
				result = result.subList(0, searchCriteria.getLimit());
			}
		}
		return result;
	}

	private ArrayList<String> generateRowStringForSearch(
			Map<String, String> searchCriteriaDefinition,
			TableDefinition tableDefinition
	) {
		ArrayList<String> rowStrings = new ArrayList<>();
		List<ColumnDefinition> columnDefinitions = tableDefinition.getColumnDefinitions();
		searchCriteriaDefinition.forEach((columnName, columnValue) -> {
			ColumnDefinition columnDefinition = columnDefinitions.stream().filter(
					curColumnDefinition -> curColumnDefinition.getName().equals(columnName)).findFirst().orElse(null);
			if (columnDefinition != null) {
				rowStrings.addAll(parseValue(columnDefinition.getType(), columnName, columnValue));
			}
		});
		return rowStrings;
	}

	/**
	 * Examples:
	 * bool: "true", "false", "!true"
	 * number: "10", "<10", ">=10|<20|!15", "!10"
	 * text: "hello world", "*ello*", "!hello world|!byebye world", "!*world"
	 * date: "2017-01-01 12:43:00", ">=2017-01-01 12:43:00|<2017-01-05 12:43:00"
	 *
	 * @param columnType
	 * @param columnName
	 * @param columnValue
	 * @return
	 */
	private ArrayList<String> parseValue(ColumnType columnType, String columnName, String columnValue) {
		ArrayList<String> result = new ArrayList<>();
		switch (columnType) {
			case bool:
				if (columnValue.startsWith("!")) {
					result.add(String.format("%s != %s", columnName, columnValue.substring(1)));
				} else {
					result.add(String.format("%s = %s", columnName, columnValue));
				}
				break;
			case date:
				Arrays.asList(columnValue.split("\\|")).forEach(value -> {
					if (value.matches("^(>=|<=|>|<).*")) {
						char secChar = value.charAt(1);
						String operator = "" + value.charAt(0) + (secChar == '=' ? secChar : "");
						String filteredColumnValue = value.replaceAll("^(>=|<=|>|<)", "");
						result.add(String.format("%s %s '%s'", columnName, operator, filteredColumnValue));
					} else {
						result.add(String.format("%s = '%s'", columnName, columnValue));
					}
				});
				break;
			case number:
				Arrays.asList(columnValue.split("\\|")).forEach(value -> {
					if (value.startsWith("!")) {
						int columnValueInteger = Integer.parseInt(value.replace("!", ""));
						result.add(String.format("%s != %d", columnName, columnValueInteger));
					} else if (value.matches("^(>=|<=|>|<).*")) {
						char secChar = value.charAt(1);
						String operator = "" + value.charAt(0) + (secChar == '=' ? secChar : "");
						int columnValueInteger = Integer.parseInt(value.replaceAll("^(>=|<=|>|<)", ""));
						result.add(String.format("%s %s %d", columnName, operator, columnValueInteger));
					} else {
						int columnValueInteger = Integer.parseInt(value);
						result.add(String.format("%s = %d", columnName, columnValueInteger));
					}
				});
				break;
			case text:
				Arrays.asList(columnValue.split("\\|")).forEach(value -> {
					String operator = "like";
					if (value.startsWith("!")) {
						operator = "not " + operator;
						value = value.substring(1);
					}
					result.add(String.format("%s %s '%s'", columnName, operator, value.replaceAll("\\*", "%")));
				});
				break;
		}
		return result;
	}
}
