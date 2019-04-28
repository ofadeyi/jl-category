package com.example.jlcategory.product;

import com.example.jlcategory.category.JLService;
import com.google.common.base.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class ProductService {

    private final JLService categoryService;
    private final Map<String, String> rgbMapper;
    private final Map<String, String> currencyMapper;

    @Autowired
    public ProductService(JLService categoryService,
                          @Qualifier("rgb-mapper") Map<String, String> rgbMapper,
                          @Qualifier("currency-mapper") Map<String, String> currencyMapper) {
        this.categoryService = categoryService;
        this.rgbMapper = rgbMapper;
        this.currencyMapper = currencyMapper;
    }

    public Products findReducedProducts(String categoryId, String labelType) {
        List<Products.Product> products = categoryService.retrieveCategoryProducts(categoryId)
                .stream()
                .filter(this::hasPreviousPrice)
                .map(jsonValue -> toProduct(jsonValue, labelType))
                .sorted(Comparator.comparingDouble(Products.Product::getReduction)
                        .reversed())
                .collect(Collectors.toList());

        return Products.builder()
                .products(products)
                .build();
    }

    private boolean hasPreviousPrice(JsonValue jsonValue) {
        JsonObject product = (JsonObject) jsonValue;
        return product.getJsonObject("price")
                .containsKey("was") &&
                !product.getJsonObject("price")
                        .getString("was").isEmpty();
    }

    private Products.Product toProduct(JsonValue jsonValue, String labelType) {
        JsonObject product = (JsonObject) jsonValue;
        JsonObject price = product.getJsonObject("price");
        BigDecimal reduction = BigDecimal.valueOf(Double.parseDouble(price.getString("was")))
                .subtract(BigDecimal.valueOf(Double.parseDouble(extractNowPrice(price))));
        return Products.Product.builder()
                .id(product.getString("productId"))
                .title(product.getString("title"))
                .colorSwatches(toColorSwatches(product.getJsonArray("colorSwatches")))
                .nowPrice(formatPrice(extractNowPrice(price), price.getString("currency", "GBP")))
                .reduction(reduction.doubleValue())
                .priceLabel(buildPriceLabel(price, labelType, reduction))
                .build();
    }

    private List<Products.ColorSwatch> toColorSwatches(JsonArray colorSwatches) {
        return colorSwatches.stream()
                .map(jsonValue -> (JsonObject) jsonValue)
                .map(colorSwatch ->
                        Products.ColorSwatch.builder()
                                .color(colorSwatch.getString("color"))
                                .skuId(colorSwatch.getString("skuId"))
                                .rgb(rgbMapper.getOrDefault(colorSwatch.getString("basicColor"), ""))
                                .build())
                .collect(Collectors.toList());
    }

    private String formatPrice(String nowPrice, String currency) {
        BigDecimal price = BigDecimal.valueOf(Double.parseDouble(nowPrice));
        if (price.doubleValue() < 10.0) {
            return String.format("%s%s", currencyMapper.getOrDefault(currency, ""), nowPrice);
        }
        return String.format("%s%d", currencyMapper.getOrDefault(currency, ""), price.intValue());
    }

    private String buildPriceLabel(JsonObject price, String labelType, BigDecimal reduction) {
        String priceLabel = "";
        String currency = price.getString("currency", "GBP");
        switch (LabelType.fromLabel(labelType)) {
            case WasNow: {
                priceLabel = String.format("Was %s, now %s",
                        formatPrice(price.getString("was"), currency),
                        formatPrice(extractNowPrice(price), currency));
                break;
            }
            case WasThenNow: {
                if (price.getString("then1").isEmpty() && price.getString("then2").isEmpty()) {
                    priceLabel = buildPriceLabel(price, LabelType.WasNow.label(), reduction);
                } else {
                    String thenPrice = Objects.requireNonNullElseGet(
                            Strings.emptyToNull(price.getString("then2"))
                            , () -> price.getString("then1"));
                    priceLabel = String.format("Was %s, then %s, now %s",
                            formatPrice(price.getString("was"), currency),
                            formatPrice(thenPrice, currency),
                            formatPrice(extractNowPrice(price), currency));

                }
                break;
            }
            case PercentageDiscount: {
                BigDecimal percentageReduction = calculatePercentageReduction(
                        BigDecimal.valueOf(Double.parseDouble(price.getString("was"))),reduction);

                priceLabel = String.format("%d%% off - now %s", percentageReduction.intValue(),
                        formatPrice(extractNowPrice(price), currency));

            }
        }
        return priceLabel;
    }

    private BigDecimal calculatePercentageReduction(BigDecimal original, BigDecimal reduction) {
        return reduction
                .divide(original, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }

    private String extractNowPrice(JsonObject price) {
        try {
            return price.getString("now");
        } catch (ClassCastException ccEx) {
            return price.getJsonObject("now").getString("from");
        }
    }
}
