package com.example.jlcategory.product;

import com.fasterxml.jackson.annotation.*;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.List;
import java.util.Objects;

@Builder
@ToString
@Getter
@EqualsAndHashCode
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Products {
    private final List<Product> products;

    @JsonCreator
    public Products(@JsonProperty("products") List<Product> products) {
        this.products = Objects.requireNonNullElseGet(products, List::of);
    }

    @Builder
    @ToString
    @Getter
    @EqualsAndHashCode
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties("reduction")
    public static final class Product {

        @JsonProperty("productId")
        private final String id;
        private final String title;
        private final List<ColorSwatch> colorSwatches;
        private final String nowPrice;
        private final String priceLabel;
        private final double reduction;

        @JsonCreator
        public Product(@JsonProperty("productId") String productId,
                       @JsonProperty("title") String title,
                       @JsonProperty("colorSwatches") List<ColorSwatch> colorSwatches,
                       @JsonProperty("nowPrice") String nowPrice,
                       @JsonProperty("priceLabel") String priceLabel,
                       @JsonProperty("reduction") double reduction) {
            this.id = productId;
            this.title = title;
            this.colorSwatches = Objects.requireNonNullElseGet(colorSwatches, List::of);
            this.nowPrice = nowPrice;
            this.priceLabel = priceLabel;
            this.reduction = reduction;

        }
    }

    @Builder
    @ToString
    @Getter
    @EqualsAndHashCode
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static final class ColorSwatch {

        private final String color;
        @JsonProperty("rgbColor")
        private final String rgb;
        @JsonProperty("skuid")
        private final String skuId;

        @JsonCreator
        public ColorSwatch(@JsonProperty("color") String color,
                           @JsonProperty("rgbColor") String rgbColor,
                           @JsonProperty("skuid") String skuid) {
            this.color = color;
            this.rgb = rgbColor;
            this.skuId = skuid;
        }
    }
}
