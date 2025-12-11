package com.example.bean;

import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.List;

@Named("productBean")
@SessionScoped

public class ProductBean extends BaseBean implements ProductOperations, Serializable {

    @Inject
    private ProductService service;

    private List<Product> products;
    private Product current = new Product();

    @Override
    public void loadProducts() {
        products = service.findAll();
        current = products.get(0); // new
    }

    @Override
    public void save() {
        service.save(current);
        info("Produit enregistré !");
        loadProducts();
        current = new Product();
    }

    @Override
    public void select(Product p) {
        this.current = p;
    }

    public ProductService getService() {
        return service;
    }

    public void setService(ProductService service) {
        this.service = service;
    }

    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }

    public Product getCurrent() {
        return current;
    }

    public void setCurrent(Product current) {
        this.current = current;
    }



    
}
