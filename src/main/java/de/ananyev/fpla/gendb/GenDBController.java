package de.ananyev.fpla.gendb;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;
import java.util.Map;

/**
 * Created by Ilya Ananyev on 24.12.16.
 */
@RestController
@RequestMapping("/table")
public class GenDBController {
	@Autowired
	JdbcTemplate jdbcTemplate;

	@PostMapping
	public void createTable(@RequestBody Table table) {
		ArrayList<String> rowsStrings = new ArrayList<>();
		String sql = String.format("create table %s (", table.getTableName());
		table.getRows().forEach( it -> {
			rowsStrings.add(String.format("%s %s", it.getName(), it.getType()));
		});
		sql += String.join(", ", rowsStrings) + ")";
		this.jdbcTemplate.execute(sql);
	}

	@GetMapping("/{tableName}")
	public List getFrom(@PathVariable String tableName) {
		return this.jdbcTemplate.queryForList(String.format("select * from %s", tableName));
	}

	@PostMapping("/insert/{tableName}")
	public void insert(@PathVariable String tableName, @RequestBody List<Map<String, String>> keyValueList) {
		/* ToDo implement */
//		keyValueList.forEach(it -> {
//			String sql = String.format("insert into %s ", tableName);
//
//			it.forEach((key, value) -> {
//				keys += key;
//				sql += "";
//			});
//
//		});
	}

	@DeleteMapping("/{tableName}")
	public void removeTable(@PathVariable String tableName) {
		this.jdbcTemplate.execute(String.format("drop table %s", tableName));
	}
}