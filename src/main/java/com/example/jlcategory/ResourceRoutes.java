package com.example.jlcategory;

import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

public interface ResourceRoutes {
    RouterFunction<ServerResponse> routes();
}
