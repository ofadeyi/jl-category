package com.example.jlcategory.product;

import com.example.jlcategory.category.JLService;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.server.ServerRequest;
import reactor.test.StepVerifier;

import javax.json.Json;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class ProductHandlerTest {

    ProductService productService;
    JLService categoryService;

    @Before
    public void setup() {
        categoryService = mock(JLService.class);
        productService = new ProductService(categoryService, Map.of("Blue", "0000FF", "Yellow", "FFFF00"),
                Map.of("GBP", "£", "USD", "$", "EUR", "€"));
    }

    @Test
    public void shouldReturnBadRequestWhenNoCategoryIdIsProvided() {
        // Given
        ProductHandler handler = new ProductHandler(productService);
        ServerRequest request = mock(ServerRequest.class);
        when(request.queryParam("categoryId"))
                .thenReturn(Optional.empty());
        // When
        StepVerifier.create(handler.retrieve(request))
                .expectNextMatches(response -> response.statusCode().value() == HttpStatus.BAD_REQUEST.value())
                .verifyComplete();
    }

    @Test
    public void shouldReturnBadRequestWhenReduceIsFalse() {
        // Given
        ProductHandler handler = new ProductHandler(productService);
        ServerRequest request = mock(ServerRequest.class);
        when(request.queryParam("categoryId"))
                .thenReturn(Optional.of("600001506"));
        when(request.queryParam("reduced"))
                .thenReturn(Optional.empty());
        // When
        StepVerifier.create(handler.retrieve(request))
                .expectNextMatches(response -> response.statusCode().value() == HttpStatus.BAD_REQUEST.value())
                .verifyComplete();
    }

    @Test
    public void shouldReturnReducedProductsList() throws IOException {
        // Given
        ProductHandler handler = new ProductHandler(productService);
        ServerRequest request = mock(ServerRequest.class);
        when(request.queryParam("categoryId"))
                .thenReturn(Optional.of("600001506"));
        when(request.queryParam("reduced"))
                .thenReturn(Optional.of("true"));

        when(categoryService.retrieveCategoryProducts(anyString()))
                .thenReturn(
                        Json.createReader(
                                new ClassPathResource("__files/category-600001506.json").getInputStream())
                                .readObject()
                                .getJsonArray("products"));

        // When
        StepVerifier.create(handler.retrieve(request))
                .expectNextMatches(response -> response.statusCode().value() == HttpStatus.OK.value())
                .verifyComplete();

        // Then
        verify(categoryService, times(1))
                .retrieveCategoryProducts(anyString());
    }
}
