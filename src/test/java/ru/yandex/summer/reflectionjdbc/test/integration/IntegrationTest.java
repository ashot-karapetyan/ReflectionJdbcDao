package ru.yandex.summer.reflectionjdbc.test.integration;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ru.yandex.summer.reflectionjdbc.api.dao.ReflectionJdbcDao;
import ru.yandex.summer.reflectionjdbc.api.db.DBType;
import ru.yandex.summer.reflectionjdbc.impl.DAOFactory;
import ru.yandex.summer.reflectionjdbc.test.TestBean;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
/**
 * Created by ashot.karapetyan on 5/2/14.
 */
public class IntegrationTest extends IntegrationTestBase {
	private DataSource dataSource;
	private DAOFactory factory = null;
	private ReflectionJdbcDao<TestBean> dao = null;


	@Before
	public void setUp(){
		this.dataSource =  createDataSource();
		DAOFactory.createFactory(this.dataSource, DBType.MSSQL);
		this.factory = DAOFactory.getInstance();
		this.dao =  this.factory.getReflectionJdbcDao();

		cleanDB();

	}

	@After
	public void cleanUp(){
		cleanDB();
	}

	private void cleanDB() {
		try (Connection conn = dataSource.getConnection()) {
			try (PreparedStatement statement = conn.prepareStatement("Delete From Projects")) {
				 statement.executeUpdate();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testInsert(){
		dao.insert(new TestBean(5, "bb", new Date(), true, 1000.0));
		assertEquals(1, dao.selectAll(TestBean.class).size());
	}

	@Test
	public void testUpdate() throws Exception {
		dao.insert(new TestBean(5, "bbdd", new Date(), true, 1000.0));
		assertEquals("bbdd", dao.selectByKey(new TestBean(5)).getName());
	}

	@Test
	public void testDeleteByKey() throws Exception {

        List<TestBean> allData = Arrays.asList(
                new TestBean(5, "bb", new Date(), true, 1000.0),
                new TestBean(6, "bb", new Date(), true, 1000.0),
                new TestBean(7, "bb", new Date(), true, 1000.0),
                new TestBean(8, "bb", new Date(), true, 1000.0)
        );
        for (TestBean bean : allData) {
            dao.insert(bean);
        }
        dao.deleteByKey(new TestBean(5));
        List<TestBean> actualResult = dao.selectAll(TestBean.class);
        List<TestBean> expectedValues = new ArrayList<>(allData);
        expectedValues.remove(0);
        assertEquals(expectedValues, actualResult);
    }

	@Test
	public void testSelectByKey() throws Exception {
		TestBean bean = new TestBean(5, "bb", new Date(), true, 1000.0);
		dao.insert(bean);
		dao.insert(new TestBean(6, "bb", new Date(), true, 1000.0));
		dao.insert(new TestBean(7, "bb", new Date(), true, 1000.0));
		dao.insert(new TestBean(8, "bb", new Date(), true, 1000.0));
		assertEquals(bean, dao.selectByKey(new TestBean(5)));
	}

	@Test
	public void testSelectAll() throws Exception {

        List<TestBean> allData = Arrays.asList(
                new TestBean(5, "bb", new Date(), true, 1000.0),
                new TestBean(6, "dd", new Date(), true, 1000.0),
                new TestBean(7, "ee", new Date(), true, 1000.0),
                new TestBean(8, "fff", new Date(), true, 1000.0)
        );
        for (TestBean bean : allData) {
            dao.insert(bean);
        }
        assertEquals(allData, dao.selectAll(TestBean.class));
    }



}
