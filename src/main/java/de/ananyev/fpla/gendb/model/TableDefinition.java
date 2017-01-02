package de.ananyev.fpla.gendb.model;

import javax.persistence.*;
import java.util.List;

/**
 * Created by Ilya Ananyev on 26.12.16.
 */
@Entity
@Table(name = "tableDefinition")
public class TableDefinition {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	private String tableName;

	@OneToMany(mappedBy = "tableDefinition")
	private List<ColumnDefinition> columnDefinitions;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getTableName() {
		return tableName.toUpperCase();
	}

	public void setTableName(String tableName) {
		this.tableName = tableName.toUpperCase();
	}

	public List<ColumnDefinition> getColumnDefinitions() {
		return columnDefinitions;
	}

	public void setColumnDefinitions(List<ColumnDefinition> columnDefinitions) {
		this.columnDefinitions = columnDefinitions;
	}
}
