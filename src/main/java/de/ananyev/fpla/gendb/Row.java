package de.ananyev.fpla.gendb;

/**
 * Created by Ilya Ananyev on 26.12.16.
 */
public class Row {
	private String name;
	private RowType type;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public RowType getType() {
		return type;
	}

	public void setType(RowType type) {
		this.type = type;
	}

	enum RowType {
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
}
