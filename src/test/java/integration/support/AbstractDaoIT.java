package integration.support;

import dao.GenericDAO;
import static exceptions.LCException.handleGenericException;
import static interfaces.Log.LOG;
import org.junit.jupiter.api.BeforeEach;

import java.lang.reflect.Field;

public abstract class AbstractDaoIT {

    protected GenericDAO dao;

    @BeforeEach
    void setupDao() throws Exception {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            dao = new GenericDAO();
            Field dsField = GenericDAO.class.getDeclaredField("dataSource");
            dsField.setAccessible(true);
            dsField.set(dao, new JdbcTestDataSource());
        } catch (Exception e) {
            handleGenericException(e, methodName);
        }
    } // end method

    protected void injectDao(Object service) throws Exception {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            Field daoField = service.getClass().getDeclaredField("dao");
            daoField.setAccessible(true);
            daoField.set(service, dao);
        } catch (Exception e) {
            handleGenericException(e, methodName);
        }
    } // end method

} // end class
