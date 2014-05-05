package ru.yandex.summer.reflectionjdbc.test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by ashot.karapetyan on 5/2/14.
 */
public class UnitTestBase {

	protected DataSource mockDataSource(){
		DataSource dataSource = mock(DataSource.class);
		Connection connection = mock(Connection.class);
		PreparedStatement statement = mock(PreparedStatement.class);
		ResultSet resultSet = mock(ResultSet.class);
		try {
			when(dataSource.getConnection()).thenReturn(connection);
			when(connection.prepareStatement(anyString())).thenReturn(statement);
			when(statement.executeUpdate()).thenReturn(1);
			when(statement.executeQuery()).thenReturn(resultSet);
			when(resultSet.next()).thenReturn(false);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return dataSource;

	}
}
