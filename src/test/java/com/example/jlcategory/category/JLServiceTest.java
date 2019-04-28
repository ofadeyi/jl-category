package com.example.jlcategory.category;


import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringRunner;

import javax.json.JsonArray;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class JLServiceTest {

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

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8889);

    @Test
    public void shouldReturn404NotFound() {
        //Given
        String requestURL = String.format("/%s/bad-category-id/%s", resource, subResource);
        stubFor(get(urlPathEqualTo(requestURL))
                .withQueryParam("key", equalTo(apiKey))
                .willReturn(notFound()));

        JLService service = new JLService(host, resource, subResource, apiKey);

        // When
        JsonArray categoryProducts = service.retrieveCategoryProducts("bad-category-id");

        // Then
        assertThat("categoryProducts should be empty", categoryProducts.isEmpty(), is(true));
        verify(getRequestedFor(urlPathEqualTo(requestURL)));
    }

    @Test
    public void shouldReturn200WhenAValidCategoryIdIsProvided() {
        //Given
        String requestURL = String.format("/%s/%s/%s", resource, categoryId, subResource);
        stubFor(get(urlPathEqualTo(requestURL))
                .withQueryParam("key", equalTo(apiKey))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("category-600001506.json")));

        JLService service = new JLService(host, resource, subResource, apiKey);

        // When
        JsonArray categoryProducts = service.retrieveCategoryProducts(categoryId);

        // Then
        assertThat("categoryProducts should not be empty", categoryProducts.isEmpty(), is(false));
        assertThat("categoryProducts.size() should be matching", categoryProducts.size(), is(50));
        verify(getRequestedFor(urlPathEqualTo(requestURL)));
    }
}
