package sepm.dsa.service;

import org.hibernate.validator.HibernateValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import sepm.dsa.dao.ProductCategoryDao;
import sepm.dsa.exceptions.DSAValidationException;
import sepm.dsa.model.Product;
import sepm.dsa.model.ProductCategory;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.List;
import java.util.Set;

@Transactional(readOnly = true)
public class ProductCategoryServiceImpl implements ProductCategoryService {
    private static final Logger log = LoggerFactory.getLogger(RegionServiceImpl.class);
    private Validator validator = Validation.byProvider(HibernateValidator.class).configure().buildValidatorFactory().getValidator();
    private ProductCategoryDao productCategoryDao;
    @Autowired
    private ProductService productService;

    @Override
    public ProductCategory get(Integer id) {
        log.debug("calling get(" + id + ")");
        ProductCategory result = productCategoryDao.get(id);
        log.trace("returning " + result);
        return result;
    }

    @Override
    @Transactional(readOnly = false)
    public int add(ProductCategory p) {
        log.debug("calling add(" + p + ")");
        validate(p);
        return productCategoryDao.add(p);
    }

    @Override
    @Transactional(readOnly = false)
    public void update(ProductCategory p) {
        log.debug("calling update(" + p + ")");
        validate(p);
        productCategoryDao.update(p);
    }

    @Override
    @Transactional(readOnly = false)
    public void remove(ProductCategory p) {
        log.debug("calling remove(" + p + ")");
        p = get(p.getId());
        productCategoryDao.remove(p);
        for (Product product : p.getProducts()) {
            product.getCategories().remove(p);
//            productService.update(product);
        }
//        p.getProducts().clear();
    }

    @Override
    public List<ProductCategory> getAll() {
        log.debug("calling getAll()");
        List<ProductCategory> result = productCategoryDao.getAll();
        log.trace("returning " + result);
        return result;
    }

    /**
     * Validates a product
     *
     * @param product
     * @throws sepm.dsa.exceptions.DSAValidationException if product is not valid
     */
    private void validate(ProductCategory product) throws DSAValidationException {
        Set<ConstraintViolation<ProductCategory>> violations = validator.validate(product);
        if (violations.size() > 0) {
            throw new DSAValidationException("Produktkategorie ist nicht valide.", violations);
        }
    }

    public ProductCategoryDao getProductCategoryDao() {
        return productCategoryDao;
    }

    public void setProductCategoryDao(ProductCategoryDao productCategoryDao) {
        this.productCategoryDao = productCategoryDao;
    }
}
