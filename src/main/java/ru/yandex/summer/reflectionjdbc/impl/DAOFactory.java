package ru.yandex.summer.reflectionjdbc.impl;

import ru.yandex.summer.reflectionjdbc.api.dao.ReflectionJdbcDao;
import ru.yandex.summer.reflectionjdbc.api.db.DBType;

import javax.sql.DataSource;

/**
 * Created by Ashot on 4/29/2014.
 */
public class DAOFactory {

    public static DAOFactory factory;

    private DataSource dataSource;
    private DBType dBType;

    private DAOFactory(DataSource dataSource, DBType dBType) {
        this.dataSource = dataSource;
        this.dBType = dBType;
    }

    public static void createFactory(DataSource dataSource, DBType dBType) {
        factory = new DAOFactory(dataSource, dBType);
    }

    public static DAOFactory getInstance() {
        return factory;
    }

    public DBType getDBType() {
        return this.dBType;
    }

    public <T> ReflectionJdbcDao<T> getReflectionJdbcDao() {
        return new ReflectionJdbcDaoImpl<T>();
    }

    public DataSource getDataSource() {
        return this.dataSource;
    }
}
