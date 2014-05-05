package ru.yandex.summer.reflectionjdbc.api.db;


import java.util.HashMap;
import java.util.Map;

/**
 * Class for providing queries for specific file path. If queries have been loaded once, it will be remembered in memory
 * 
 * @author Ashot Karapetyan
 */
public class QueriesProvider {
	private DBType dbType = null;
	private Map<String, Queries> queries = new HashMap<String, Queries>();

	/**
	 * Constructs new queries provider instance
	 * 
	 * @param dbType
	 *            The provided database type. See {@link DBType}
	 */
	public QueriesProvider(DBType dbType) {
		this.dbType = dbType;
	}

	/**
	 * Gets {@link Queries} instance for specific file path. First call of this method for specific file path will load
	 * queries XMLHelper to the memory and subsequent calls of this method will get queries from the memory
	 * 
	 * @param filePath
	 *            The provided file path
	 * @return loaded {@link Queries} instance
	 */
	public Queries getQueries(String filePath) {
		Queries qs = null;

		if ((qs = queries.get(filePath)) == null) {
			qs = new SQLQueries(dbType, filePath);
			queries.put(filePath, qs);
		}

		return qs;
	}
}
