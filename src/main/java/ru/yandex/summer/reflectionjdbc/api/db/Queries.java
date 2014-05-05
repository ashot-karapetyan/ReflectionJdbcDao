package ru.yandex.summer.reflectionjdbc.api.db;

/**
 * Base interface for accessing queries by query id
 * 
 * @author Arsen Alexanyan
 */
public interface Queries {
	/**
	 * Gets query by provided query id
	 * 
	 * @param id
	 *            Provided query id
	 */
	public String getQuery(String id);
}
