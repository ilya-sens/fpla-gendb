package de.ananyev.fpla.gendb;

import java.util.ArrayList;

/**
 * Created by Ilya Ananyev on 26.12.16.
 */
public class Table {
	private String tableName;
	private ArrayList<Row> rows;

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public ArrayList<Row> getRows() {
		return rows;
	}

	public void setRows(ArrayList<Row> rows) {
		this.rows = rows;
	}
}
