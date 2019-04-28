package com.example.jlcategory.category;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import javax.json.Json;
import javax.json.JsonArray;
import java.util.Map;

public class JLService {

    private final String host;
    private final String requestUrl;

    @Autowired
    public JLService(@Value("${jl.host}") String host,
                     @Value("${jl.resource}") String resource,
                     @Value("${jl.subResource}") String subResource,
                     @Value("${jl.apiKey}") String apiKey) {
        this.host = host;
        this.requestUrl = String.format("/%s/categoryId/%s?key=%s", resource, subResource, apiKey)
                .replaceFirst("categoryId", "%s");
    }

    public JsonArray retrieveCategoryProducts(String categoryId) {
        return WebClient.create(host)
                .get()
                .uri(String.format(this.requestUrl, categoryId))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .flatMap(response -> {
                    if (response.statusCode() == HttpStatus.OK) {
                        return response.bodyToMono(Map.class);
                    } else {
                        return Mono.just(Map.of());
                    }
                })
                .map(bodyResponse -> Json.createObjectBuilder((Map<String, Object>) bodyResponse)
                        .build()
                        .getJsonArray("products"))
                .onErrorReturn(JsonArray.EMPTY_JSON_ARRAY)
                .block();
    }
}
