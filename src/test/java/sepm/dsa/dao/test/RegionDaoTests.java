package sepm.dsa.dao.test;

//import com.github.springtestdbunit.DbUnitTestExecutionListener;
import junit.framework.TestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import sepm.dsa.dao.RegionDao;
import sepm.dsa.model.RainfallChance;
import sepm.dsa.model.Region;
import sepm.dsa.model.Temperature;

/**
 * Created by Michael on 13.05.2014.
 */
@RunWith(JUnit4.class)
//@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class,
//        DbUnitTestExecutionListener.class })
@ContextConfiguration(locations = {"classpath:testContext.xml"})
public class RegionDaoTests {

    @Autowired
    private RegionDao regionDao;


    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {

    }

//   TODO Currently fails on 'HibernateException: No Session found for current thread'
    @Test
    public void add_shouldPersistEntity() {

        Region region = new Region();
        region.setName("Region1");
        region.setColor("65A3EF");
        region.setComment("comment");
        region.setRainfallChance(RainfallChance.MONSUN);
        region.setTemperature(Temperature.LOW);
        int id = regionDao.add(region);
        Region persistedRegion = regionDao.get(id);
        TestCase.assertTrue(persistedRegion != null);

        regionDao.remove(region);
    }

}
