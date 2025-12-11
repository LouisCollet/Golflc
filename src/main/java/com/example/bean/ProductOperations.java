package com.example.bean;

//import com.example.model.Product;
import java.util.List;

public interface ProductOperations {
    void loadProducts();
    void save();
    void select(Product p);
    List<Product> getProducts();
    Product getCurrent();
}
