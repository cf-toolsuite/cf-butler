package io.pivotal.cfapp.task;

import org.springframework.context.ApplicationEvent;

import io.pivotal.cfapp.domain.product.Products;

public class ProductsRetrievedEvent extends ApplicationEvent {

    private static final long serialVersionUID = 1L;

    private Products products;

    public ProductsRetrievedEvent(Object source) {
        super(source);
    }

    public ProductsRetrievedEvent products(Products products) {
        this.products = products;
        return this;
    }

    public Products getProducts() {
        return products;
    }

}
