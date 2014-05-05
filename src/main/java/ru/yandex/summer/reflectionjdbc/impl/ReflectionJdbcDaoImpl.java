package ru.yandex.summer.reflectionjdbc.impl;

import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.yandex.summer.reflectionjdbc.api.annotation.Column;
import ru.yandex.summer.reflectionjdbc.api.annotation.KeyColumn;
import ru.yandex.summer.reflectionjdbc.api.annotation.MappedTable;
import ru.yandex.summer.reflectionjdbc.api.dao.ReflectionJdbcDao;
import ru.yandex.summer.reflectionjdbc.api.db.Queries;
import ru.yandex.summer.reflectionjdbc.api.db.QueriesProvider;
import ru.yandex.summer.reflectionjdbc.api.exception.ReflectionJdbcSQLException;

import javax.sql.DataSource;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Implementation of ReflectionJdbcDao.
 *
 *
 */
public  class  ReflectionJdbcDaoImpl<T> implements ReflectionJdbcDao<T>{

	private Logger logger = LoggerFactory.getLogger(this.getClass());
	private Queries queries = null;

	public ReflectionJdbcDaoImpl(){
//		QueriesProvider qp = new QueriesProvider(DAOFactory.getInstance().getDBType());
//		this.queries = qp.getQueries("");
			//TODO: assertions
//		Class<T> clazz = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
//		assert  clazz.getAnnotation(MappedTable.class) != null;
	}

    @Override
    public  void insert(T object) {
	    try{
			executeInsert(object);
	    }catch (SQLException e ){
		    this.logger.error(String.format("Error occurs during insertion of object of %s class", object.getClass().getName()), e);
		    throw new ReflectionJdbcSQLException(String.format("Error occurs during insertion of object of %s class", object.getClass().getName()), e);
	    }

    }



	@Override
    public void update(T object) {
		try{
			executeUpdate(object);
		}catch (SQLException e ){
			this.logger.error(String.format("Error occurs during insertion of object of %s class", object.getClass().getName()), e);
			throw new ReflectionJdbcSQLException(String.format("Error occurs during update of object of %s class", object.getClass().getName()), e);
		}
    }




	@Override
    public void deleteByKey(T object) {
		try{
			executeDelete(object);
		}catch (SQLException e ){
			this.logger.error(String.format("Error occurs during insertion of object of %s class", object.getClass().getName()), e);
			throw new ReflectionJdbcSQLException(String.format("Error occurs during deletion of object of %s class", object.getClass().getName()), e);
		}
    }



	@Override
    public T selectByKey(T object) {
		try{
			return executeSelect(object);
		}catch (SQLException e ){
			this.logger.error(String.format("Error occurs during insertion of object of %s class", object.getClass().getName()), e);
			throw new ReflectionJdbcSQLException(String.format("Error occurs during load of object of %s class", object.getClass().getName()), e);
		}
    }



	@Override
    public List<T> selectAll(Class<T> clazz) {
	    try{
		    return executeSelectAll(clazz);
	    }catch (SQLException e ){
		    this.logger.error("Error occurs during load", e);
		    throw new ReflectionJdbcSQLException("Error occurs during load", e);
	    }
    }


	/**
	 * Generates SQL script from insert template (queries.xml  file).
	 * @param object object to insert
	 * @return result of sql command.
	 * @throws SQLException
	 */
	private int executeInsert(T object) throws SQLException {

		List<Field> mappedField = extractMappedFields(object);
		String table = getMappedTableName(object);

		StringBuilder columns = new StringBuilder();
		boolean isFirst = true;
		for(Field field : mappedField){
			if(!isFirst){
				columns.append(",")	;
			}
			String mappedColumn = getMappedColumnName(field);
			columns.append(mappedColumn);
			isFirst = false;
		}

		StringBuilder values = new StringBuilder();
		isFirst = true;
		for(Field field : mappedField){
			if(!isFirst){
				values.append(",")	;
			}
			values.append(getValueSqlConverted(object,field).toString());
			isFirst = false;
		}
		String query = this.queries.getQuery("insert");
		String sql = query.replace("@@tableName",table).replace("@@columns",columns.toString()).
				replace("@@values", values.toString());
		DataSource dataSource = DAOFactory.getInstance().getDataSource();
		int result;
		try (Connection conn = dataSource.getConnection()) {
			try (PreparedStatement statement = conn.prepareStatement(sql)) {
				result = statement.executeUpdate();
			}
		}
		return result;
	}

	/**
	 *
	 * @param object
	 * @return
	 * @throws SQLException
	 */
	private int executeUpdate(T object) throws SQLException {

		List<Field> keyFields = extractKeyFields(object);
		List<Field> mappedField = extractMappedFields(object);
		String table = getMappedTableName(object);

		StringBuilder values = new StringBuilder();
		for(Field field : mappedField){
			String mappedColumn = getMappedColumnName(field);
			values.append(mappedColumn);
			values.append("=");
			values.append(getValueSqlConverted(object,field).toString());
		}

		StringBuilder condition = new StringBuilder();
		for(Field field : keyFields){
			String mappedColumn = getMappedColumnName(field);
			condition.append(mappedColumn);
			condition.append("=");
			condition.append(getValueSqlConverted(object,field).toString());
		}

		String query = this.queries.getQuery("update");
		String sql = query.replace("@@tableName",table).replace("@@values", values.toString()).
				replace("@@condition", condition.toString());
		DataSource dataSource = DAOFactory.getInstance().getDataSource();
		int result;
		try (Connection conn = dataSource.getConnection()) {
			try (PreparedStatement statement = conn.prepareStatement(sql)) {
				result = statement.executeUpdate();
			}
		}
		return result;
	}

	/**
	 *
	 * @param object
	 * @return
	 * @throws SQLException
	 */
	private int executeDelete(T object) throws SQLException {

		List<Field> keyFields = extractKeyFields(object);
		String table = getMappedTableName(object);
		StringBuilder condition = new StringBuilder();
		for(Field field : keyFields){
			String mappedColumn = getMappedColumnName(field);
			condition.append(mappedColumn);
			condition.append("=");
			condition.append(getValueSqlConverted(object,field).toString());
		}

		String query = this.queries.getQuery("delete");
		String sql = query.replace("@@tableName", table).replace("@@condition", condition.toString());
		DataSource dataSource = DAOFactory.getInstance().getDataSource();
		int result;
		try (Connection conn = dataSource.getConnection()) {
			try (PreparedStatement statement = conn.prepareStatement(sql)) {
				result = statement.executeUpdate();
			}
		}
		return result;

	}

	/**
	 *
	 * @param object
	 * @return
	 * @throws SQLException
	 */
	private T executeSelect(T object) throws SQLException {

		List<Field> keyFields = extractKeyFields(object);
		List<Field> mappedField = extractMappedFields(object);
		String table = getMappedTableName(object);
		StringBuilder condition = new StringBuilder();
		for(Field field : keyFields){
			String mappedColumn = getMappedColumnName(field);
			condition.append(mappedColumn);
			condition.append("=");
			condition.append(getValueSqlConverted(object,field).toString());
		}

		String query = this.queries.getQuery("selectOne");
		String sql = query.replace("@@tableName",table).replace("@@condition", condition.toString());
		DataSource dataSource = DAOFactory.getInstance().getDataSource();
		try (Connection conn = dataSource.getConnection()) {
			try (PreparedStatement statement = conn.prepareStatement(sql)) {
				try(ResultSet resultSet =  statement.executeQuery()){
					if(resultSet.next()){
						for(Field field : mappedField){
							String mappedColumn = getMappedColumnName(field);
							Object value = resultSet.getObject(mappedColumn);
							setValue(object, field,  value);
						}
					}


				}
			}
		}
		return object;
	}

	/**
	 *
	 * @return
	 * @throws SQLException
	 */
	private List<T> executeSelectAll(Class<T> clazz)  throws SQLException {

		List<T> result = new ArrayList<>();
		List<Field> mappedField = extractMappedFields(clazz);
		mappedField.addAll(extractKeyFields(clazz));
		String table = clazz.getAnnotation(MappedTable.class).table();

		String query = this.queries.getQuery("selectAll");
		String sql = query.replace("@@tableName", table);

		DataSource dataSource = DAOFactory.getInstance().getDataSource();
		try (Connection conn = dataSource.getConnection()) {
			try (PreparedStatement statement = conn.prepareStatement(sql)) {
				try(ResultSet resultSet =  statement.executeQuery()){
					while(resultSet.next()){
						T object = clazz.newInstance();
						for(Field field : mappedField){
							String mappedColumn = getMappedColumnName(field);
							Object value = resultSet.getObject(mappedColumn);
							setValue(object, field,  value);
						}
						result.add(object);
					}
				} catch (InstantiationException | IllegalAccessException e) {
					throw new RuntimeException(e);
				}
			}
		}
		return result;
	}

	/**
	 *
	 * @param object
	 * @return
	 */
	private List<Field> extractMappedFields(T object) {
		return extractFieldsByAnnotations(object, Sets.newHashSet(KeyColumn.class, Column.class));
	}

	/**
	 *
	 * @param clazz
	 * @return
	 */
	private List<Field> extractMappedFields(Class<?> clazz) {
		return extractFieldsByAnnotations(clazz, Sets.newHashSet(KeyColumn.class, Column.class));
	}

	/**
	 *
	 * @param object
	 * @return
	 */
	private List<Field> extractKeyFields(T object) {
		return extractFieldsByAnnotations(object, Sets.newHashSet(KeyColumn.class));
	}

	/**
	 *
	 * @param clazz
	 * @return
	 */
	private List<Field> extractKeyFields(Class<?> clazz) {
		return extractFieldsByAnnotations(clazz, Sets.newHashSet(KeyColumn.class));
	}

	/**
	 *
	 * @param object
	 * @param requiredAnnotations
	 * @return
	 */
	private List<Field> extractFieldsByAnnotations(T object, Set<? extends  Class<? extends Annotation> > requiredAnnotations) {
		return extractFieldsByAnnotations(object.getClass(), requiredAnnotations);
	}

	/**
	 *
	 * @param clazz
	 * @param requiredAnnotations
	 * @return
	 */
	private List<Field> extractFieldsByAnnotations(Class<?> clazz, Set<? extends  Class<? extends Annotation> > requiredAnnotations) {
		List<Field> annotatedFields = new ArrayList<>();
		Field[] fields =  clazz.getDeclaredFields();
		for(Field field: fields){
			Set<Class<? extends Annotation>> annotations = new HashSet<>();
			for(Annotation annotation : field.getDeclaredAnnotations()){
				annotations.add(annotation.annotationType());
			}
			if(!Sets.intersection(annotations,   requiredAnnotations).isEmpty()){
				annotatedFields.add(field);
			}
		}
		return annotatedFields;
	}

	/**
	 *
	 * @param field
	 * @return
	 */
	private String getMappedColumnName(Field field) {
		String columnName = null ;
		for(Annotation annotation : field.getDeclaredAnnotations()){
			if(annotation.annotationType().equals(Column.class)){
				columnName = field.getAnnotation(Column.class).mappedColumn();
			}
		}
		return  columnName;
	}

	/**
	 *
	 * @param object
	 * @param field
	 * @param value
	 */
	private void setValue(T object, Field field, Object value){
		try{
			new PropertyDescriptor(field.getName(), object.getClass()).getWriteMethod().invoke(object,value);
		} catch (InvocationTargetException | IntrospectionException | IllegalAccessException e) {
			throw new RuntimeException(e);

		}
	}

	/**
	 *
	 * @param object
	 * @param field
	 * @return
	 */
	private Object getValue(T object, Field field) {
		try{
			return  new PropertyDescriptor(field.getName(), object.getClass()).getReadMethod().invoke(object);
		} catch (InvocationTargetException | IntrospectionException | IllegalAccessException e) {
			throw new RuntimeException(e);

		}
	}

	/**
	 *
	 * @param object
	 * @param field
	 * @return
	 */
	private Object getValueSqlConverted(T object, Field field) {
		return convertToSqlValue(getValue(object, field));
	}

	/**
	 *
	 * @param object
	 * @return
	 */
	private String getMappedTableName(T object) {
		return object.getClass().getAnnotation(MappedTable.class).table();
	}

	/**
	 * Convert
	 * @param value
	 * @return
	 */
	private String convertToSqlValue(Object value){
		String convertedValue = value.toString();
		Class<?> clazz = value.getClass();
		if(clazz.equals(String.class)){
			convertedValue = String.format("\"%s\"",value.toString());
		}

		return convertedValue;
	}

}
