package ru.yandex.summer.reflectionjdbc.api.db;

/**
 * Enumeration for database types
 *
 */
public enum DBType {
	MSSQL("mssql"),
	MYSQL("mysql");
//	ORACLE("oracle"),

	private String title = null;

	DBType(String title) {
		this.title = title;
	}

	/**
	 * Gets title of the database type
	 * 
	 * @return title of the database type
	 */
	public String getTitle() {
		return title;
	}
}
