package com.example.jlcategory;


import com.example.jlcategory.category.JLService;
import com.example.jlcategory.product.ProductHandler;
import com.example.jlcategory.product.ProductService;
import com.example.jlcategory.product.ProductsRoutes;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.reactive.function.server.RouterFunction;

import java.util.Map;
import java.util.stream.Stream;

@Configuration
@EnableWebFlux
public class MainConfig implements WebFluxConfigurer {

    @Value("${jl.host}")
    private String host;
    @Value("${jl.resource}")
    private String resource;
    @Value("${jl.subResource}")
    private String subResource;
    @Value("${jl.apiKey}")
    private String apiKey;

    @Bean
    public RouterFunction<?> routerFunction() {
        return RootRoutes.routes(Stream.of(new ProductsRoutes(productHandler())));
    }

    @Bean("rgb-mapper")
    public Map<String, String> rgbMapper() {
        return Map.of("Red", "FF0000", "Lime", "00FF00", "Blue", "0000FF",
                "Yellow", "FFFF00", "Grey", "808080", "Purple", "800080",
                "Green", "008000");
    }

    @Bean("currency-mapper")
    public Map<String, String> currencyMapper() {
        return Map.of("GBP", "£", "USD", "$", "EUR", "€");
    }

    @Bean
    public ProductService productService() {
        return new ProductService(jlService(), rgbMapper(), currencyMapper());
    }

    @Bean
    public ProductHandler productHandler() {
        return new ProductHandler(productService());
    }

    @Bean
    public JLService jlService() {
        return new JLService(host, resource, subResource, apiKey);
    }

}
