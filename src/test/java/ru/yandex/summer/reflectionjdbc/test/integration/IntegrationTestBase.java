package ru.yandex.summer.reflectionjdbc.test.integration;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import ru.yandex.summer.reflectionjdbc.api.db.DBType;

import javax.sql.DataSource;
import java.beans.PropertyVetoException;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import static org.junit.Assert.*;
/**
 * Created by ashot.karapetyan on 5/2/14.
 */
public class IntegrationTestBase {

	private static Map<DBType,String> DRIVERS  = new HashMap<>();
	private static Map<DBType,String> CONNECTION_PATTERS  = new HashMap<>();

	protected  DBType dbType;
	protected  String userName;
	protected  String password;
	protected  String dbHost;
	protected  String dbName;

	public IntegrationTestBase() {
		this.dbType = DBType.MSSQL;
		this.userName = "idmdevuser";
		this.password = "idmdevuser";
		this.dbHost = "sis2s034";
		this.dbName = "tst_IDM-Kb-Mexico-Prod_20140502";
	}

	public IntegrationTestBase(DBType dbType, String userName, String password, String dbHost, String dbName) {
		this.dbType = dbType;
		this.userName = userName;
		this.password = password;
		this.dbHost = dbHost;
		this.dbName = dbName;
	}

	static {
		DRIVERS.put(DBType.MSSQL, "net.sourceforge.jtds.jdbc.Driver");
		DRIVERS.put(DBType.MYSQL, "com.mysql.jdbc.Driver");

		CONNECTION_PATTERS.put(DBType.MSSQL, "jdbc:jtds:sqlserver://%1s/%2s");
		CONNECTION_PATTERS.put(DBType.MYSQL," jdbc:mysql://%1s/%2s");
	}
	protected DataSource createDataSource(){
		ComboPooledDataSource dataSource  = null;

		try{
			dataSource = new ComboPooledDataSource();
			dataSource.setDriverClass(DRIVERS.get(this.dbType));
			dataSource.setJdbcUrl(String.format(CONNECTION_PATTERS.get(this.dbType), dbHost, dbName));
			dataSource.setUser(this.userName);
			dataSource.setPassword(this.password);
		}

		catch(PropertyVetoException ex2){
			throw new RuntimeException(ex2);
		}

		return dataSource;
	}
}
