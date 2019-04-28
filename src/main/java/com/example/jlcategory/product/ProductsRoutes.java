package com.example.jlcategory.product;

import com.example.jlcategory.ResourceRoutes;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

public class ProductsRoutes implements ResourceRoutes {
    private final ProductHandler handler;

    public ProductsRoutes(ProductHandler productHandler) {
        this.handler = productHandler;
    }

    @Override
    public RouterFunction<ServerResponse> routes() {
        return route(GET("/products"), handler::retrieve);
    }
}
