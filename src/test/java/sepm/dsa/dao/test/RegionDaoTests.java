package sepm.dsa.dao.test;

import junit.framework.TestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import sepm.dsa.dao.RegionDao;
import sepm.dsa.model.Region;

/**
 * Created by Michael on 13.05.2014.
 */
@RunWith(JUnit4.class)
public class RegionDaoTests {

    private RegionDao regionDao;


    @Before
    public void setUp() {
        ApplicationContext ctx = new ClassPathXmlApplicationContext("applicationContext.xml");
        regionDao = (RegionDao) ctx.getBean("regionDao");
        if (regionDao == null) {
            throw new IllegalStateException("regionDao could not be fetched");
        }
    }

    @After
    public void tearDown() {

    }

//   TODO Currently fails on 'HibernateException: No Session found for current thread'
//    @Test
    public void add_shouldPersistEntity() {

        Region region = new Region();
        region.setName("Region1");
        region.setColor("65A3EF");
        int id = regionDao.add(region);
        Region persistedRegion = regionDao.get(id);
        TestCase.assertTrue(persistedRegion != null);

        regionDao.remove(region);
    }

}
