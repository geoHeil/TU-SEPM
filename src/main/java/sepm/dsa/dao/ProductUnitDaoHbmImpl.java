package sepm.dsa.dao;

import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import sepm.dsa.model.ProductUnit;

import java.util.List;
import java.util.Vector;

/**
 * Created by Chris on 17.05.2014.
 */
@Repository
@Transactional(readOnly = true)
public class ProductUnitDaoHbmImpl implements ProductUnitDao {

    private static final Logger log = LoggerFactory.getLogger(RegionDaoHbmImpl.class);

    private SessionFactory sessionFactory;

    @Override
    @Transactional(readOnly = false)
    public int add(ProductUnit Unit) {
        log.debug("calling addConnection(" + Unit + ")");
        sessionFactory.getCurrentSession().save(Unit);
        return Unit.getId();
    }

    @Override
    @Transactional(readOnly = false)
    public void update(ProductUnit Unit) {
        log.debug("calling update(" + Unit + ")");
        sessionFactory.getCurrentSession().update(Unit);
    }

    @Override
    @Transactional(readOnly = false)
    public void remove(ProductUnit Unit) {
        log.debug("calling removeConnection(" + Unit + ")");
        sessionFactory.getCurrentSession().delete(Unit);
    }

    @Override
    public ProductUnit get(Integer id) {
        log.debug("calling get(" + id + ")");

        Object result = sessionFactory.getCurrentSession().get(ProductUnit.class, id);

        if (result == null) {
            return null;
        }
        log.trace("returning " + result);
        return (ProductUnit) result;
    }

    @Override
    public List<ProductUnit> getAll() {
        log.debug("calling getAll()");
        List<?> list = sessionFactory.getCurrentSession().getNamedQuery("ProductUnit.findAll").list();

        List<ProductUnit> result = new Vector<>(list.size());
        for (Object o : list) {
            result.add((ProductUnit) o);
        }

        log.trace("returning " + result);
        return result;
    }

    public void setSessionFactory(SessionFactory sessionFactory) {
        log.debug("calling setSessionFactory(" + sessionFactory + ")");
        this.sessionFactory = sessionFactory;
    }
}
