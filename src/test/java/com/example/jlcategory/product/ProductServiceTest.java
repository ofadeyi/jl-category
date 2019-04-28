package com.example.jlcategory.product;

import com.example.jlcategory.category.JLService;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import javax.json.Json;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class ProductServiceTest {
    ProductService cut;
    JLService categoryService;

    @Before
    public void setup() {
        categoryService = mock(JLService.class);
        cut = new ProductService(categoryService, Map.of("Blue", "0000FF", "Yellow", "FFFF00"),
                Map.of("GBP", "£", "USD", "$", "EUR", "€"));
    }

    @After
    public void tearDown() {
        cut = null;
        categoryService = null;
    }

    @Test
    public void shouldReturnAnEmptyProductsListWhenNoProductHasPreviousPrice() throws IOException {
        // Given
        when(categoryService.retrieveCategoryProducts(anyString()))
                .thenReturn(
                        Json.createReader(
                                new ClassPathResource("__files/category-01.json").getInputStream())
                                .readObject()
                                .getJsonArray("products"));

        // When
        Products result = cut.findReducedProducts("categoryId", null);

        // Then
        assertThat("result should be empty", result.getProducts().isEmpty(), is(true));
        verify(categoryService, times(1))
                .retrieveCategoryProducts(anyString());
    }

    @Test
    public void shouldReturnANonEmptyProductsListWhenSomeProductHavePreviousPrice() throws IOException {
        // Given
        when(categoryService.retrieveCategoryProducts(anyString()))
                .thenReturn(
                        Json.createReader(
                                new ClassPathResource("__files/category-02.json").getInputStream())
                                .readObject()
                                .getJsonArray("products"));

        // When
        Products result = cut.findReducedProducts("categoryId", null);

        // Then
        assertThat("result should not be empty", result.getProducts().isEmpty(), is(false));
        assertThat("result.products.size() should match expected", result.getProducts().size(), is(2));
        verify(categoryService, times(1))
                .retrieveCategoryProducts(anyString());
    }

    @Test
    public void shouldReturnAProductCorrectlyMappedWhenProductHasPreviousPrice() throws IOException {
        // Given
        when(categoryService.retrieveCategoryProducts(anyString()))
                .thenReturn(
                        Json.createReader(
                                new ClassPathResource("__files/category-03.json").getInputStream())
                                .readObject()
                                .getJsonArray("products"));

        // When
        Products result = cut.findReducedProducts("categoryId", null);

        // Then
        assertThat("result should not be empty", result.getProducts().isEmpty(), is(false));
        assertThat("result.products.size() should match expected", result.getProducts().size(), is(1));
        assertThat("result.product[0].id should match expected",
                result.getProducts().get(0).getId(), is("3421340"));
        assertThat("result.product[0].title should match expected",
                result.getProducts().get(0).getTitle(), is("Phase Eight Beatrix Floral Printed Dress, Cream/Red"));
        assertThat("result.product[0].colorSwatches should be empty",
                result.getProducts().get(0).getColorSwatches().isEmpty(), is(true));

        verify(categoryService, times(1))
                .retrieveCategoryProducts(anyString());
    }

    @Test
    public void shouldReturnAProductWithColorSwatchesCorrectlyMappedWhenProductHasPreviousPrice() throws IOException {
        // Given
        when(categoryService.retrieveCategoryProducts(anyString()))
                .thenReturn(
                        Json.createReader(
                                new ClassPathResource("__files/category-04.json").getInputStream())
                                .readObject()
                                .getJsonArray("products"));

        // When
        Products result = cut.findReducedProducts("categoryId", null);

        // Then
        assertThat("result should not be empty", result.getProducts().isEmpty(), is(false));
        assertThat("result.products.size() should match expected", result.getProducts().size(), is(1));

        assertThat("result.product[0].colorSwatches.size() should match expected",
                result.getProducts().get(0).getColorSwatches().size(), is(2));

        assertThat("result.product[0].colorSwatches.color should match expected",
                extractAttributes(result.getProducts().get(0).getColorSwatches(),
                        Products.ColorSwatch::getColor), containsInAnyOrder("Navy", "Mimosa Yellow"));

        assertThat("result.product[0].colorSwatches.skuId should match expected",
                extractAttributes(result.getProducts().get(0).getColorSwatches(),
                        Products.ColorSwatch::getSkuId), containsInAnyOrder("237334029", "237334043"));

        assertThat("result.product[0].colorSwatches.color should match expected",
                extractAttributes(result.getProducts().get(0).getColorSwatches(),
                        Products.ColorSwatch::getRgb), containsInAnyOrder("0000FF", "FFFF00"));

        verify(categoryService, times(1))
                .retrieveCategoryProducts(anyString());
    }

    @Test
    public void shouldReturnAProductWithNowPriceCorrectlyMappedWhenProductHasPreviousPrice() throws IOException {
        // Given
        when(categoryService.retrieveCategoryProducts(anyString()))
                .thenReturn(
                        Json.createReader(
                                new ClassPathResource("__files/category-05.json").getInputStream())
                                .readObject()
                                .getJsonArray("products"));

        // When
        Products result = cut.findReducedProducts("categoryId", null);

        // Then
        assertThat("result should not be empty", result.getProducts().isEmpty(), is(false));
        assertThat("result.products.size() should match expected", result.getProducts().size(), is(3));

        assertThat("result.product[0].nowPrice should match expected",
                extractAttributes(result.getProducts(),
                        Products.Product::getNowPrice), containsInAnyOrder("£59", "£9.99", "£74"));

        verify(categoryService, times(1))
                .retrieveCategoryProducts(anyString());
    }

    @Test
    public void shouldReturnAProductWithPriceLabelCorrectlyMappedWhenLabelTypeIsShowWasNow() throws IOException {
        // Given
        when(categoryService.retrieveCategoryProducts(anyString()))
                .thenReturn(
                        Json.createReader(
                                new ClassPathResource("__files/category-06.json").getInputStream())
                                .readObject()
                                .getJsonArray("products"));

        // When
        Products result = cut.findReducedProducts("categoryId", "ShowWasNow");

        // Then
        assertThat("result should not be empty", result.getProducts().isEmpty(), is(false));
        assertThat("result.products.size() should match expected", result.getProducts().size(), is(1));

        assertThat("result.product[0].priceLabel should match expected",
                result.getProducts().get(0).getPriceLabel(), is("Was £85, now £59"));

        verify(categoryService, times(1))
                .retrieveCategoryProducts(anyString());
    }

    @Test
    public void shouldReturnAProductWithPriceLabelCorrectlyMappedWhenLabelTypeIsNotProvided() throws IOException {
        // Given
        when(categoryService.retrieveCategoryProducts(anyString()))
                .thenReturn(
                        Json.createReader(
                                new ClassPathResource("__files/category-06.json").getInputStream())
                                .readObject()
                                .getJsonArray("products"));

        // When
        Products result = cut.findReducedProducts("categoryId", null);

        // Then
        assertThat("result should not be empty", result.getProducts().isEmpty(), is(false));
        assertThat("result.products.size() should match expected", result.getProducts().size(), is(1));

        assertThat("result.product[0].priceLabel should match expected",
                result.getProducts().get(0).getPriceLabel(), is("Was £85, now £59"));

        verify(categoryService, times(1))
                .retrieveCategoryProducts(anyString());
    }

    @Test
    public void shouldReturnAProductWithPriceLabelCorrectlyMappedWhenLabelTypeIsShowWasThenNow() throws IOException {
        // Given
        when(categoryService.retrieveCategoryProducts(anyString()))
                .thenReturn(
                        Json.createReader(
                                new ClassPathResource("__files/category-07.json").getInputStream())
                                .readObject()
                                .getJsonArray("products"));

        // When
        Products result = cut.findReducedProducts("categoryId", "ShowWasThenNow");

        // Then
        assertThat("result should not be empty", result.getProducts().isEmpty(), is(false));
        assertThat("result.products.size() should match expected", result.getProducts().size(), is(3));

        assertThat("result.product[0].priceLabel should match expected",
                extractAttributes(result.getProducts(),
                        Products.Product::getPriceLabel), containsInAnyOrder("Was £85, then £68, now £59",
                        "Was £99, then £63, now £59", "Was £140, now £99"));

        verify(categoryService, times(1))
                .retrieveCategoryProducts(anyString());
    }

    @Test
    public void shouldReturnAProductWithPriceLabelCorrectlyMappedWhenLabelTypeIsShowPercDscount() throws IOException {
        // Given
        when(categoryService.retrieveCategoryProducts(anyString()))
                .thenReturn(
                        Json.createReader(
                                new ClassPathResource("__files/category-06.json").getInputStream())
                                .readObject()
                                .getJsonArray("products"));

        // When
        Products result = cut.findReducedProducts("categoryId", "ShowPercDscount");

        // Then
        assertThat("result should not be empty", result.getProducts().isEmpty(), is(false));
        assertThat("result.products.size() should match expected", result.getProducts().size(), is(1));

        assertThat("result.product[0].priceLabel should match expected",
                result.getProducts().get(0).getPriceLabel(), is("30% off - now £59"));

        verify(categoryService, times(1))
                .retrieveCategoryProducts(anyString());
    }

    @Test
    public void shouldReturnReducedProductListInOrderOfReduction() throws IOException {
        // Given
        when(categoryService.retrieveCategoryProducts(anyString()))
                .thenReturn(
                        Json.createReader(
                                new ClassPathResource("__files/category-07.json").getInputStream())
                                .readObject()
                                .getJsonArray("products"));

        // When
        Products result = cut.findReducedProducts("categoryId", "ShowWasThenNow");

        // Then
        assertThat("result should not be empty", result.getProducts().isEmpty(), is(false));
        assertThat("result.products.size() should match expected", result.getProducts().size(), is(3));

        assertThat("result.product[0].productId should match expected",
                extractAttributes(result.getProducts(),
                        Products.Product::getId), Matchers.contains("3421340", "3391561", "3341058"));

        verify(categoryService, times(1))
                .retrieveCategoryProducts(anyString());
    }

    @Test
    public void shouldReturnReducedProductsListWhenAvailable() throws IOException {
        // Given
        when(categoryService.retrieveCategoryProducts(anyString()))
                .thenReturn(
                        Json.createReader(
                                new ClassPathResource("__files/category-600001506.json").getInputStream())
                                .readObject()
                                .getJsonArray("products"));

        // When
        Products result = cut.findReducedProducts("categoryId", null);

        // Then
        assertThat("result should not be empty", result.getProducts().isEmpty(), is(false));
        assertThat("result.products.size() should match expected", result.getProducts().size(), is(6));
        verify(categoryService, times(1))
                .retrieveCategoryProducts(anyString());
    }

    private <T> List<String> extractAttributes(List<T> pojos,
                                               Function<T, String> extractor) {
        return pojos.stream()
                .map(extractor)
                .collect(Collectors.toList());
    }

}
