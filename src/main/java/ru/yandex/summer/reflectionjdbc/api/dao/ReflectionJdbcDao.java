package ru.yandex.summer.reflectionjdbc.api.dao;

import java.util.List;


/**
 * DAO for saving and loading Objects like JavaBeans.
 * Class ob object must have {@link ru.yandex.summer.reflectionjdbc.api.annotation.MappedTable} annotation.
 * Each field, which we want save and load must has {@link ru.yandex.summer.reflectionjdbc.api.annotation.Column} annotation.
 * Key columns
 *
 * @param <T>
 */
public interface ReflectionJdbcDao<T> {

    /**
     * Inserts object to DB.
     *
     * @param object object to insert.
     */
    public void insert(T object);

    /**
     * Updates object by key fields.
     * Update will be done by key fields of object.
     * Key fields must have {@link ru.yandex.summer.reflectionjdbc.api.annotation.KeyColumn} annotation.
     * @param object
     */
    public void update(T object);

    /**
     *Deletes object, which is identical by key fields with given object
     * @param key
     */
    public void deleteByKey(T key);

    /**
     * Select object by key fields of given instance.
     * @param key
     * @return
     */
    public T selectByKey(T key);

    /**
     * Loads all instances of given class.
     * @param type
     * @return
     */
    public List<T> selectAll(Class<T> type);
}
