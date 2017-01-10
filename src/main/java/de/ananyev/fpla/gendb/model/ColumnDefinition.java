package de.ananyev.fpla.gendb.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import de.ananyev.fpla.gendb.model.enumeration.ColumnType;

import javax.persistence.*;

/**
 * Created by Ilya Ananyev on 26.12.16.
 */
@Entity
@Table(name = "columnDefinition")
public class ColumnDefinition {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	private String name;

	@Enumerated(value = EnumType.STRING)
	private ColumnType type;

	@ManyToOne
	@JoinColumn
	@JsonBackReference
	private TableDefinition tableDefinition;

	public String getName() {
		return name.toUpperCase();
	}

	public void setName(String name) {
		this.name = name.toUpperCase();
	}

	public ColumnType getType() {
		return type;
	}

	public void setType(ColumnType type) {
		this.type = type;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public TableDefinition getTableDefinition() {
		return tableDefinition;
	}

	public void setTableDefinition(TableDefinition tableDefinition) {
		this.tableDefinition = tableDefinition;
	}
}
