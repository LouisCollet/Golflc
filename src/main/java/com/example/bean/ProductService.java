package com.example.bean;

import static interfaces.Log.LOG;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.List;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import org.mockito.MockitoAnnotations;
@ApplicationScoped
public class ProductService {

    private final List<Product> products = new ArrayList<>();

    public ProductService() {
        // données mock
        LOG.debug("entering ProductService");
       // List<String> mockList = mock(List.class); // crash !!
        
//        @Mock
//	List<String> mockList;
        
        
        products.add(new Product(1L, "Laptop", 999.99));
        products.add(new Product(2L, "Phone", 499.99));
        products.add(new Product(3L, "Tablet", 299.99));
           LOG.debug("products = " + products.toString());
    }

    public List<Product> findAll() {
        return products;
    }

    public void save(Product p) {
        if (p.getId() == null) {
            p.setId((long) (products.size() + 1));
            products.add(p);
        } else {
            // simulate update
            products.removeIf(prod -> prod.getId().equals(p.getId()));
            products.add(p);
        }
    }
}