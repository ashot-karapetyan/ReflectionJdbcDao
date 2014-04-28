package ru.yandex.summer.reflectionjdbc.impl;

import ru.yandex.summer.reflectionjdbc.api.dao.ReflectionJdbcDao;

import java.util.List;

/**
 * Created by Ashot on 4/29/2014.
 */
public class ReflectionJdbcDaoImpl<T> implements ReflectionJdbcDao<T>{

    @Override
    public void insert(T object) {

    }

    @Override
    public void update(T object) {

    }

    @Override
    public void deleteByKey(T key) {

    }

    @Override
    public T selectByKey(T key) {
        return null;
    }

    @Override
    public List<T> selectAll() {
        return null;
    }
}
