package com.example.jlcategory.product;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import static org.springframework.web.reactive.function.server.ServerResponse.badRequest;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

public class ProductHandler {
    private final ProductService service;

    @Autowired
    public ProductHandler(ProductService service) {
        this.service = service;
    }

    public Mono<ServerResponse> retrieve(ServerRequest request) {
        return request.queryParam("categoryId")
                .map(categoryId -> findCategoryProducts(request, categoryId))
                .orElse(badRequest().build());
    }

    private Mono<ServerResponse> findCategoryProducts(ServerRequest request, String categoryId) {
        return request.queryParam("reduced")
                .map(Boolean::valueOf)
                .filter(reduced -> reduced)
                .map(_reduced -> findReducedCategoryProducts(categoryId, request.queryParam("labelType").orElse("")))
                .orElse(badRequest().build());
    }

    private Mono<ServerResponse> findReducedCategoryProducts(String categoryId, String labelType) {
        return ok()
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .body(Mono.just(service.findReducedProducts(categoryId, labelType)), Products.class);
//                .body(fromObject(service.findReducedProducts(categoryId, labelType)));
    }


}
