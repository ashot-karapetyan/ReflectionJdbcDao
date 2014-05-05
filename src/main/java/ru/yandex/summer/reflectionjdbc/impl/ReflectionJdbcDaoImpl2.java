package ru.yandex.summer.reflectionjdbc.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.yandex.summer.reflectionjdbc.api.annotation.AllowNull;
import ru.yandex.summer.reflectionjdbc.api.annotation.Column;
import ru.yandex.summer.reflectionjdbc.api.annotation.KeyColumn;
import ru.yandex.summer.reflectionjdbc.api.annotation.MappedTable;
import ru.yandex.summer.reflectionjdbc.api.dao.ReflectionJdbcDao;
import ru.yandex.summer.reflectionjdbc.api.db.Queries;
import ru.yandex.summer.reflectionjdbc.api.db.QueriesProvider;
import ru.yandex.summer.reflectionjdbc.api.exception.NullNotAllowed;
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
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Implementation of ReflectionJdbcDao.
 * 
 * 
 */
public class ReflectionJdbcDaoImpl2<T>
		implements ReflectionJdbcDao<T> {

	public static final String QUERIES_TEMPLATES_PATH = "/ru/yandex/summer/reflectionjdbc/queries.xml";
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	private Queries queries = null;

	protected ReflectionJdbcDaoImpl2() {
		QueriesProvider qp = new QueriesProvider(DAOFactory.getInstance().getDBType());
		this.queries = qp.getQueries(QUERIES_TEMPLATES_PATH);
	}

	@Override
	public void insert(T object) {
		try {
			insertObject(object);
		}
		catch (SQLException e) {
			this.logger.error(
					String.format("Error occurs during insertion of object of %s class", object.getClass().getName()),
					e);
			throw new ReflectionJdbcSQLException(String.format("Error occurs during insertion of object of %s class",
					object.getClass().getName()), e);
		}

	}

	@Override
	public void update(T object) {
		try {
			updateObject(object);
		}
		catch (SQLException e) {
			this.logger.error(
					String.format("Error occurs during insertion of object of %s class", object.getClass().getName()),
					e);
			throw new ReflectionJdbcSQLException(String.format("Error occurs during update of object of %s class",
					object.getClass().getName()), e);
		}
	}

	@Override
	public void deleteByKey(T object) {
		try {
			deleteObject(object);
		}
		catch (SQLException e) {
			this.logger.error(
					String.format("Error occurs during insertion of object of %s class", object.getClass().getName()),
					e);
			throw new ReflectionJdbcSQLException(String.format("Error occurs during deletion of object of %s class",
					object.getClass().getName()), e);
		}
	}

	@Override
	public T selectByKey(T object) {
		try {
			return executeSelect(object);
		}
		catch (SQLException e) {
			this.logger.error(
					String.format("Error occurs during insertion of object of %s class", object.getClass().getName()),
					e);
			throw new ReflectionJdbcSQLException(String.format("Error occurs during load of object of %s class",
					object.getClass().getName()), e);
		}
	}

	@Override
	public List<T> selectAll(Class<T> clazz) {
		try {
			return executeSelectAll(clazz);
		}
		catch (SQLException e) {
			this.logger.error("Error occurs during load", e);
			throw new ReflectionJdbcSQLException("Error occurs during load", e);
		}
	}

	/**
	 * Generates SQL script from insert template (queries.xml file).
	 * 
	 * @param object
	 *            object to insert
	 * @return result of sql command.
	 * @throws SQLException
	 */
	private int insertObject(T object)
			throws SQLException {

		List<Field> mappedField = extractMappedFields(object);
		String table = getMappedTableName(object);

		StringBuilder columns = new StringBuilder();
		boolean isFirst = true;
		for (Field field : mappedField) {
			if (!isFirst) {
				columns.append(",");
			}
			String mappedColumn = getMappedColumnName(field, true);
			columns.append(mappedColumn);
			isFirst = false;
		}

		StringBuilder values = new StringBuilder();
		isFirst = true;
		for (Field field : mappedField) {
			if (!isFirst) {
				values.append(",");
			}
//			values.append(getValueSqlConverted(object, field).toString());
			isFirst = false;
			values.append("?");
		}

		String query = this.queries.getQuery("insert");
		String sql = query.replace("@@tableName", table)
				.replace("@@columns", columns.toString())
				.replace("@@values", values.toString());
		int result = executeUpdate(sql,mappedField, object);
		return result;
	}

	/**
	 * 
	 * @param object
	 * @return
	 * @throws SQLException
	 */
	private int updateObject(T object)
			throws SQLException {

		final List<Field> keyFields = extractKeyFields(object);
		final List<Field> mappedField = extractMappedFields(object);
		String table = getMappedTableName(object);

		StringBuilder values = new StringBuilder();
		for (Field field : mappedField) {
			String mappedColumn = getMappedColumnName(field, true);
			values.append(mappedColumn);
			values.append("=");
//			values.append(getValueSqlConverted(object, field).toString());
			values.append("?");
		}

		StringBuilder condition = new StringBuilder();
		for (Field field : keyFields) {
			String mappedColumn = getMappedColumnName(field, true);
			condition.append(mappedColumn);
			condition.append("=");
//			condition.append(getValueSqlConverted(object, field).toString());
			condition.append("?");
		}

		String query = this.queries.getQuery("update");
		String sql = query.replace("@@tableName", table)
				.replace("@@values", values.toString())
				.replace("@@condition", condition.toString());
		final int result = executeUpdate(sql, new ArrayList<Field>(){{addAll(mappedField);addAll(keyFields);}}, object);
		return result;
	}

	/**
	 * 
	 * @param object
	 * @return
	 * @throws SQLException
	 */
	private int deleteObject(T object)
			throws SQLException {

		List<Field> keyFields = extractKeyFields(object);
		String table = getMappedTableName(object);
		StringBuilder condition = new StringBuilder();
		for (Field field : keyFields) {
			String mappedColumn = getMappedColumnName(field, true);
			condition.append(mappedColumn);
			condition.append("=");
//			condition.append(getValueSqlConverted(object, field).toString());
			condition.append("?");
		}

		String query = this.queries.getQuery("delete");
		String sql = query.replace("@@tableName", table).replace("@@condition", condition.toString());
		int result = executeUpdate(sql,keyFields, object);
		return result;

	}

	private int executeUpdate(String sql, List<Field> mappedField, T object )
			throws SQLException {
		DataSource dataSource = DAOFactory.getInstance().getDataSource();
		int result;
		try (Connection conn = dataSource.getConnection()) {
			try (PreparedStatement statement = conn.prepareStatement(sql)) {
				int paramIndex = 1;
				for(Field field : mappedField){

					if(field.getType() == Date.class){
						long date = ((Date)getValue(object,field)).getTime();
						statement.setTimestamp(paramIndex,new Timestamp(date));
					}else{
						statement.setObject(paramIndex,getValue(object,field));
					}

					paramIndex++;
				}
				result = statement.executeUpdate();
			}
		}
		return result;
	}

	private void prepareStatement(PreparedStatement statement,List<Field> mappedFields){

	}

	/**
	 * 
	 * @param object
	 * @return
	 * @throws SQLException
	 */
	private T executeSelect(T object)
			throws SQLException {

		List<Field> keyFields = extractKeyFields(object);
		List<Field> mappedField = extractMappedFields(object);
		String table = getMappedTableName(object);
		StringBuilder condition = new StringBuilder();
		for (Field field : keyFields) {
			String mappedColumn = getMappedColumnName(field, true);
			condition.append(mappedColumn);
			condition.append("=");
			condition.append("?");
			condition.append(getValueSqlConverted(object, field).toString());
		}

		String query = this.queries.getQuery("selectOne");
		String sql = query.replace("@@tableName", table).replace("@@condition", condition.toString());
		DataSource dataSource = DAOFactory.getInstance().getDataSource();
		try (Connection conn = dataSource.getConnection()) {
			try (PreparedStatement statement = conn.prepareStatement(sql)) {

				try (ResultSet resultSet = statement.executeQuery()) {
					if (resultSet.next()) {
						for (Field field : mappedField) {
							String mappedColumn = getMappedColumnName(field, false);
							Object value = convertToJava(field, mappedColumn, resultSet);
							setValue(object, field, value);
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
	private List<T> executeSelectAll(Class<T> clazz)
			throws SQLException {

		List<T> result = new ArrayList<>();
		List<Field> mappedField = extractMappedFields(clazz);
		mappedField.addAll(extractKeyFields(clazz));
		String table = clazz.getAnnotation(MappedTable.class).table();

		String query = this.queries.getQuery("selectAll");
		String sql = query.replace("@@tableName", table);

		DataSource dataSource = DAOFactory.getInstance().getDataSource();
		try (Connection conn = dataSource.getConnection()) {
			try (PreparedStatement statement = conn.prepareStatement(sql)) {
				try (ResultSet resultSet = statement.executeQuery()) {
					while (resultSet.next()) {
						T object = clazz.newInstance();
						for (Field field : mappedField) {
							String mappedColumn = getMappedColumnName(field, false);
							Object value = convertToJava(field, mappedColumn, resultSet);
							setValue(object, field, value);
						}
						result.add(object);
					}
				}
				catch (InstantiationException | IllegalAccessException e) {
					throw new RuntimeException(e);
				}
			}
		}
		return result;
	}

	private Object convertToJava(Field field, String mappedColumn, ResultSet resultSet)
			throws SQLException {

		Object result = null;
		if (field.getType().equals(Date.class)) {
			result = new Date(((Timestamp)resultSet.getObject(mappedColumn)).getTime());
		}
		else if (field.getType().equals(boolean.class)) {
			result = resultSet.getBoolean(mappedColumn);
		}
		else {
			try {
				String value = resultSet.getString(mappedColumn);
				if (value == null) {
					result = null;
				}
				else {
					result = field.getType().getConstructor(String.class).newInstance(value);
				}
			}
			catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
				e.printStackTrace();
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
		return extractFieldsByAnnotations(object, Sets.newHashSet(Column.class));
	}

	/**
	 * 
	 * @param clazz
	 * @return
	 */
	private List<Field> extractMappedFields(Class<?> clazz) {
		return extractFieldsByAnnotations(clazz, Sets.newHashSet(Column.class));
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
	private List<Field> extractFieldsByAnnotations(T object,
			Set<? extends Class<? extends Annotation>> requiredAnnotations) {
		return extractFieldsByAnnotations(object.getClass(), requiredAnnotations);
	}

	/**
	 * 
	 * @param clazz
	 * @param requiredAnnotations
	 * @return
	 */
	private List<Field> extractFieldsByAnnotations(Class<?> clazz,
			Set<? extends Class<? extends Annotation>> requiredAnnotations) {
		List<Field> annotatedFields = new ArrayList<>();
		Field[] fields = clazz.getDeclaredFields();
		for (Field field : fields) {
			Set<Class<? extends Annotation>> annotations = new HashSet<>();
			for (Annotation annotation : field.getDeclaredAnnotations()) {
				annotations.add(annotation.annotationType());
			}
			if (!Sets.intersection(annotations, requiredAnnotations).isEmpty()) {
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
	private String getMappedColumnName(Field field, boolean applyBracket) {
		String columnName = null;
		for (Annotation annotation : field.getDeclaredAnnotations()) {
			if (annotation.annotationType().equals(Column.class)) {
				columnName = field.getAnnotation(Column.class).mappedColumn();
				if (applyBracket) {
					columnName = String.format("[%s]", columnName);
				}

			}
		}
		return columnName;
	}

	/**
	 * 
	 * @param object
	 * @param field
	 * @param value
	 */
	private void setValue(T object, Field field, Object value) {
		try {
			checkAllowNull(field, value);
			new PropertyDescriptor(field.getName(), object.getClass()).getWriteMethod().invoke(object, value);
		}
		catch (InvocationTargetException | IntrospectionException | IllegalAccessException e) {
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
		try {

			Object value = new PropertyDescriptor(field.getName(), object.getClass()).getReadMethod().invoke(object);
			checkAllowNull(field, value);
			return value;
		}
		catch (InvocationTargetException | IntrospectionException | IllegalAccessException e) {
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
	 * Reads "table" value of {@code MappedTable} annotation.
	 * 
	 * @param object
	 * @return
	 */
	private String getMappedTableName(T object) {
		return object.getClass().getAnnotation(MappedTable.class).table();
	}

	/**
	 * Convert {@code .toString()} value of object to SQL specific string. For String values, function appends {@code '}
	 * characters from start and end.
	 * 
	 * @param value
	 * @return
	 */
	private String convertToSqlValue(Object value) {
		if (value == null) {
			return "null";
		}
		String convertedValue = value.toString();
		Class<?> clazz = value.getClass();
		if (clazz.equals(String.class)) {
			convertedValue = String.format("\'%s\'", value.toString());
		}
		else if (clazz.equals(Date.class)) {
//			convertedValue = String.format("\'%s\'", new java.sql.Date(((Date) value).getTime()).toString());
			convertedValue = String.format("\'%s\'", new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS a").format(value));
		}
		else if (clazz.equals(Boolean.class)) {
			convertedValue = String.valueOf(((Boolean) value) ? 1 : 0);
		}

		return convertedValue;
	}

	private void checkAllowNull(Field field, Object value) {
		boolean allowNull = false;
		if (field.getAnnotation(AllowNull.class) != null) {
			allowNull = true;
		}
		if (!allowNull && value == null) {
			throw new NullNotAllowed(String.format("Field %s not allow null value.", field.getName()));
		}
	}

}
