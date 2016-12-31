package de.ananyev.fpla.gendb.model.enumeration;

/**
 * Created by Ilya Ananyev on 30.12.16.
 */
public enum ColumnType {
	bool, text, number, date;

	public String toString() {
		switch (this) {
			case bool:
				return "boolean";
			case text:
				return "varchar";
			case number:
				return "int";
			case date:
				return "datetime";
			default:
				return "varchar";
		}
	}
}

