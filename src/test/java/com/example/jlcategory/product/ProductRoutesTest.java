package com.example.jlcategory.product;

import com.example.jlcategory.ResourceRoutes;
import com.example.jlcategory.RootRoutes;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ProductRoutesTest {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8889);
    @LocalServerPort
    int serverPort;
    @Autowired
    ProductService productService;
    private WebTestClient client;
    @Value("${jl.host}")
    private String host;
    @Value("${jl.port}")
    private int port;
    @Value("${jl.resource}")
    private String resource;
    @Value("${jl.categoryId}")
    private String categoryId;
    @Value("${jl.subResource}")
    private String subResource;
    @Value("${jl.apiKey}")
    private String apiKey;

    @Before
    public void setUp() {
        ResourceRoutes routes = new ProductsRoutes(new ProductHandler(productService));

        this.client = WebTestClient
                .bindToRouterFunction(RootRoutes.routes(Stream.of(routes)))
                .build();
    }

    @Test
    public void shouldReturnBadRequestWhenCategoryIdIsNotProvided() {
        //Expect
        this.client.get()
                .uri("/products?reduced=true")
                .exchange()
                .expectStatus()
                .isBadRequest();
    }

    @Test
    public void shouldReturnBadRequestWhenReduceIsFalse() {
        //Expect
        this.client.get()
                .uri("/products?categoryId=600001506&reduced=false")
                .exchange()
                .expectStatus()
                .isBadRequest();
    }

    @Test
    public void shouldReturnBadRequestWhenReduceIsNotProvided() {
        //Expect
        this.client.get()
                .uri("/products?categoryId=600001506")
                .exchange()
                .expectStatus()
                .isBadRequest();
    }

    @Test
    public void shouldReturn200WithAnEmptyProductsListWhenNoReducedItemIsFound() {
        //Given
        String requestURL = String.format("/%s/%s/%s", resource, categoryId, subResource);
        stubFor(get(urlPathEqualTo(requestURL))
                .withQueryParam("key", equalTo(apiKey))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("category-01.json")));

        // When
        this.client.get()
                .uri(String.format("/products?categoryId=%s&reduced=%s", 600001506, true))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(Products.class)
                .isEqualTo(Products.builder().build());

        // Then
        verify(getRequestedFor(urlPathEqualTo(requestURL)));
    }

    @Test
    public void shouldReturn200WithANonEmptyProductsListWhenReducedItemAreFound() {
        //Given
        String requestURL = String.format("/%s/%s/%s", resource, categoryId, subResource);
        stubFor(get(urlPathEqualTo(requestURL))
                .withQueryParam("key", equalTo(apiKey))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("category-600001506.json")));

        // When
        this.client.get()
                .uri(String.format("/products?categoryId=%s&reduced=%s", 600001506, true))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(Products.class)
                .consumeWith(productsEntityExchangeResult -> {
                    assertThat("response.products should not be empty",
                            productsEntityExchangeResult
                                    .getResponseBody()
                                    .getProducts()
                                    .isEmpty(), is(false));

                    assertThat("response.products.size() should match expected",
                            productsEntityExchangeResult
                                    .getResponseBody()
                                    .getProducts()
                                    .size(), is(6));
                });

        // Then
        verify(getRequestedFor(urlPathEqualTo(requestURL)));
    }

    @Test
    public void shouldReturn200WithAProductPriceLabelWhenLabelTypeShowWasNow() {
        //Given
        String requestURL = String.format("/%s/%s/%s", resource, categoryId, subResource);
        stubFor(get(urlPathEqualTo(requestURL))
                .withQueryParam("key", equalTo(apiKey))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("category-06.json")));

        // When
        this.client.get()
                .uri(String.format("/products?categoryId=%s&reduced=%s&labelType=%s", 600001506, true, "ShowWasNow"))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(Products.class)
                .consumeWith(productsEntityExchangeResult -> {
                    assertThat("response.products should not be empty",
                            productsEntityExchangeResult
                                    .getResponseBody()
                                    .getProducts()
                                    .isEmpty(), is(false));

                    assertThat("response.products.size() should match expected",
                            productsEntityExchangeResult
                                    .getResponseBody()
                                    .getProducts()
                                    .size(), is(1));

                    assertThat("response.product[0].priceLabel should match expected",
                            productsEntityExchangeResult
                                    .getResponseBody()
                                    .getProducts()
                                    .get(0)
                                    .getPriceLabel(), is("Was £85, now £59"));
                });

        // Then
        verify(getRequestedFor(urlPathEqualTo(requestURL)));
    }

    @Test
    public void shouldReturn200WithAProductPriceLabelWhenLabelTypeIsNotProvided() {
        //Given
        String requestURL = String.format("/%s/%s/%s", resource, categoryId, subResource);
        stubFor(get(urlPathEqualTo(requestURL))
                .withQueryParam("key", equalTo(apiKey))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("category-06.json")));

        // When
        this.client.get()
                .uri(String.format("/products?categoryId=%s&reduced=%s", 600001506, true))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(Products.class)
                .consumeWith(productsEntityExchangeResult -> {
                    assertThat("response.products should not be empty",
                            productsEntityExchangeResult
                                    .getResponseBody()
                                    .getProducts()
                                    .isEmpty(), is(false));

                    assertThat("response.products.size() should match expected",
                            productsEntityExchangeResult
                                    .getResponseBody()
                                    .getProducts()
                                    .size(), is(1));

                    assertThat("response.product[0].priceLabel should match expected",
                            productsEntityExchangeResult
                                    .getResponseBody()
                                    .getProducts()
                                    .get(0)
                                    .getPriceLabel(), is("Was £85, now £59"));
                });

        // Then
        verify(getRequestedFor(urlPathEqualTo(requestURL)));
    }

    @Test
    public void shouldReturn200WithAProductPriceLabelWhenLabelTypeIsShowWasThenNow() {
        //Given
        String requestURL = String.format("/%s/%s/%s", resource, categoryId, subResource);
        stubFor(get(urlPathEqualTo(requestURL))
                .withQueryParam("key", equalTo(apiKey))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("category-07.json")));

        // When
        this.client.get()
                .uri(String.format("/products?categoryId=%s&reduced=%s&labelType=%s", 600001506, true, "ShowWasThenNow"))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(Products.class)
                .consumeWith(productsEntityExchangeResult -> {
                    assertThat("response.products should not be empty",
                            productsEntityExchangeResult
                                    .getResponseBody()
                                    .getProducts()
                                    .isEmpty(), is(false));

                    assertThat("response.products.size() should match expected",
                            productsEntityExchangeResult
                                    .getResponseBody()
                                    .getProducts()
                                    .size(), is(3));

                    assertThat("result.product[0].priceLabel should match expected",
                            extractAttributes(
                                    productsEntityExchangeResult
                                            .getResponseBody()
                                            .getProducts(),
                                    Products.Product::getPriceLabel),
                            containsInAnyOrder("Was £85, then £68, now £59", "Was £99, then £63, now £59", "Was £140, now £99"));
                });

        // Then
        verify(getRequestedFor(urlPathEqualTo(requestURL)));
    }

    @Test
    public void shouldReturn200WithAProductPriceLabelWhenLabelTypeIsShowPercDscount() {
        //Given
        String requestURL = String.format("/%s/%s/%s", resource, categoryId, subResource);
        stubFor(get(urlPathEqualTo(requestURL))
                .withQueryParam("key", equalTo(apiKey))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("category-06.json")));

        // When
        this.client.get()
                .uri(String.format("/products?categoryId=%s&reduced=%s&labelType=%s", 600001506, true, "ShowPercDscount"))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(Products.class)
                .consumeWith(productsEntityExchangeResult -> {
                    assertThat("response.products should not be empty",
                            productsEntityExchangeResult
                                    .getResponseBody()
                                    .getProducts()
                                    .isEmpty(), is(false));
                    assertThat("response.products.size() should match expected",
                            productsEntityExchangeResult
                                    .getResponseBody()
                                    .getProducts()
                                    .size(), is(1));

                    assertThat("result.product[0].priceLabel should match expected",
                            productsEntityExchangeResult
                                    .getResponseBody()
                                    .getProducts()
                                    .get(0)
                                    .getPriceLabel(), is("30% off - now £59"));
                });

        // Then
        verify(getRequestedFor(urlPathEqualTo(requestURL)));
    }

    private <T> List<String> extractAttributes(List<T> pojos,
                                               Function<T, String> extractor) {
        return pojos.stream()
                .map(extractor)
                .collect(Collectors.toList());
    }

}
