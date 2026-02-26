package test.prepareStatement;

import jakarta.annotation.Resource;
import jakarta.enterprise.context.ApplicationScoped;
import javax.sql.DataSource;

@ApplicationScoped
public class DataSourceProvider {

//    @Resource(lookup = "java:/jdbc/MyDS")
    private DataSource dataSource;

    public DataSource get() {
        return dataSource;
    }
}
