package ru.yandex.summer.reflectionjdbc.impl;

import ru.yandex.summer.reflectionjdbc.api.dao.ReflectionJdbcDao;

import javax.sql.DataSource;

/**
 * Created by Ashot on 4/29/2014.
 */
public class DAOFactory {

    public static DAOFactory factory;

    private DataSource dataSource;

    private DAOFactory(DataSource dataSource){
        this.dataSource = dataSource;
    }

    public static void createFactory(DataSource dataSource){
        factory = new DAOFactory(dataSource);
    }

    public <T> ReflectionJdbcDao<T> getReflectionJdbcDao(){
        return new ReflectionJdbcDaoImpl<T>();
    }

    public DataSource getDataSource() {
        return dataSource;
    }
}
