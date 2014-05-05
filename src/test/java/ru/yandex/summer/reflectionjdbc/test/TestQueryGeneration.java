package ru.yandex.summer.reflectionjdbc.test;

import org.junit.Before;
import org.junit.Test;
import ru.yandex.summer.reflectionjdbc.api.dao.ReflectionJdbcDao;
import ru.yandex.summer.reflectionjdbc.api.db.DBType;
import ru.yandex.summer.reflectionjdbc.api.exception.NullNotAllowed;
import ru.yandex.summer.reflectionjdbc.impl.DAOFactory;

import java.util.Date;

/**
 * Created by ashot.karapetyan on 4/29/14.
 */
public class TestQueryGeneration extends  UnitTestBase{

	private DAOFactory factory = null;
	private ReflectionJdbcDao<TestBean> dao = null;

	@Before
	public void setUp(){
		DAOFactory.createFactory(mockDataSource(), DBType.MSSQL);
		this.factory = DAOFactory.getInstance();
		this.dao =  this.factory.getReflectionJdbcDao();
	}

	@Test
	public void testInsert(){
		dao.insert(new TestBean(5, "bb",new Date(),true,1000.0));
	}

	@Test(expected = NullNotAllowed.class)
	public void testInsertWithException(){
		dao.insert(new TestBean(5, "bb"));
	}


	@Test
	public void testUpdate(){
		dao.update(new TestBean(5, "bb",new Date(),true,1000.0));
	}

	@Test
	public void testDelete(){
		dao.deleteByKey(new TestBean(5, null));
	}

	@Test
	public void testSelectOne(){
		dao.selectByKey(new TestBean(5, "bb"));
	}

	@Test
	public void testSelectAll(){
		dao.selectAll(TestBean.class);
	}
}
